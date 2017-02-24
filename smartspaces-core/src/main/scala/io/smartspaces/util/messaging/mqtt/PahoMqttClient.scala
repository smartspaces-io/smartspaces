/*
 * Copyright (C) 2017 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.util.messaging.mqtt

import io.smartspaces.SmartSpacesException
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.util.net.SslUtils

import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

/**
 * A Paho MQTT client.
 *
 * @author Keith M. Hughes
 */
class PahoMqttClient(val mqttBrokerDescription: MqttBrokerDescription, val nodeName: String, log: ExtendedLog) extends IdempotentManagedResource with MqttClient {

  /**
   * The default value for auto reconnecting when communication lost.
   */
  val AUTORECONNECT_DEFAULT = false

  /**
   * The Paho MQTT client.
   */
  var mqttClient: MqttAsyncClient = null

  /**
   * The persistence for the client.
   */
  private var persistence: MqttClientPersistence = null

  /**
   * The amount of time to wait for a connection, in milliseconds.
   */
  private var connectionWaitTime: Long = 10000

  /**
   * The action listener for publishers.
   */
  private val actionListener = new IMqttActionListener() {
    override def onSuccess(token: IMqttToken): Unit = {
      log.debug("MQTT message sent successfully: (token ${token})")
    }

    override def onFailure(token: IMqttToken, throwable: Throwable): Unit = {
      log.error("MQTT message failed: (token ${token})", throwable)
    }
  }

  /**
   * The file support to use.
   */
  private val fileSupport = FileSupportImpl.INSTANCE

  override def onStartup(): Unit = {
    // TODO(keith): Make this settable and configurable
    val persistencePath = mqttBrokerDescription.persistencePath.getOrElse(MqttBrokerDescription.DEFAULT_PERSISTENCE_PATH)
    if (MqttBrokerDescription.VALUE_PERSISTENCE_PATH_MEMORY == persistencePath) {
      persistence = new MemoryPersistence()
    } else if (persistencePath.startsWith("file:")) {
      val fullPersistencePath = fileSupport.newFile(persistencePath.substring("file:".length)).getAbsolutePath
      persistence = new MqttDefaultFilePersistence(fullPersistencePath)
    } else {
      throw new SmartSpacesException(s"Don't understand MQTT persistence path ${persistencePath}")
    }

    log.debug(s"Connecting to MQTT broker ${mqttBrokerDescription}")
    var client: MqttAsyncClient = null

    try {
      client = new MqttAsyncClient(mqttBrokerDescription.brokerAddress, nodeName, persistence)
    } catch {
      case e: MqttException =>
        throw new SmartSpacesException(s"Failed to create MQTT client for broker ${mqttBrokerDescription}", e)
    }

    client.setCallback(new MqttCallback() {
      override def connectionLost(cause: Throwable): Unit = {
        log.error("Lost connection to MQTT server", cause)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
        log.debug(s"Got MQTT delivery token ${token.getResponse()}")
      }

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        // Not needed since not subscribing.
      }
    })

    val mqttConnectOptions = new MqttConnectOptions()

    mqttConnectOptions.setCleanSession(true)
    mqttConnectOptions.setAutomaticReconnect(mqttBrokerDescription.autoreconnect.getOrElse(AUTORECONNECT_DEFAULT))
    if (mqttBrokerDescription.username.isDefined) {
      mqttConnectOptions.setUserName(mqttBrokerDescription.username.get)
      mqttConnectOptions.setPassword(mqttBrokerDescription.password.get.toCharArray())
    }
    if (mqttBrokerDescription.isSsl) {
      mqttConnectOptions.setSocketFactory(configureSSLSocketFactory())
    }

    log.info("Connecting to broker: " + client.getServerURI())
    var connectHappened = new CountDownLatch(1)
    var connectStarted = true
    try {
      client.connect(mqttConnectOptions, new IMqttActionListener() {
        override def onSuccess(token: IMqttToken): Unit = {
          log.info(s"MQTT connection success to MQTT broker ${mqttBrokerDescription} (token ${token})")
          connectHappened.countDown()
        }

        override def onFailure(token: IMqttToken, cause: Throwable): Unit = {
          log.error(s"MQTT connection failed to MQTT broker ${mqttBrokerDescription} (token ${token})", cause)
          connectHappened.countDown()
        }
      })
    } catch {
      case e: Throwable =>
        log.error(s"MQTT connect failed to MQTT broker ${mqttBrokerDescription}", e)
        connectStarted = false
    }

    if (connectStarted) {
      try {
        if (connectHappened.await(connectionWaitTime, TimeUnit.MILLISECONDS)) {
          if (client.isConnected()) {
            mqttClient = client
          } else {
            throw new SmartSpacesException(s"Failed to connect to MQTT broker ${mqttBrokerDescription}")
          }
        } else {
          throw new SmartSpacesException(s"Failed to connect to MQTT broker ${mqttBrokerDescription} in ${connectionWaitTime} msecs")
        }
      } catch {
        case e: InterruptedException =>
          log.error("MQTT connect failed from interrupted wait for connection", e)
      }
    }
  }

  override def onShutdown(): Unit = {
    mqttClient.close()

    persistence.close()
  }

  /**
   * Create an SSL socket factory.
   *
   * @param credentials
   *          the security credentials
   *
   * @return the socket factory.
   *
   * @throws Exception
   *           something bad happened
   */
  private def configureSSLSocketFactory(): SSLSocketFactory = {
    if (mqttBrokerDescription.keystorePath.isDefined) {
      return SslUtils.configureSslSocketFactory(mqttBrokerDescription.keystorePath.get, mqttBrokerDescription.keystorePassword.get)
    } else {
      return SslUtils.configureSSLSocketFactory(mqttBrokerDescription.caCertPath.get, mqttBrokerDescription.clientCertPath.get, mqttBrokerDescription.clientKeyPath.get)
    }
  }

}