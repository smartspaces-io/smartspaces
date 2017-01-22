/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.service.comm.pubsub.mqtt.paho

import java.util.concurrent.ScheduledExecutorService

import scala.collection.immutable.List

import org.apache.commons.logging.Log
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import io.smartspaces.SmartSpacesException
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.resource.managed.ManagedResourceState
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttConnectionListener
import io.smartspaces.service.comm.pubsub.mqtt.MqttPublisher
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription
import javax.net.ssl.KeyManagerFactory
import java.security.KeyStore
import javax.net.ssl.TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.util.Properties
import java.io.InputStream
import javax.net.ssl.SSLSocketFactory
import java.io.FileInputStream
import io.smartspaces.util.net.SslUtils

/**
 * An MQTT communication endpoint implemented with Paho.
 *
 * @author Keith M. Hughes
 */
class PahoMqttCommunicationEndpoint(mqttBrokerDescription: MqttBrokerDescription, mqttClientId: String,
    executor: ScheduledExecutorService, log: Log) extends MqttCommunicationEndpoint with IdempotentManagedResource {

  /**
   * The client persistence to use.
   */
  private var persistence: MqttClientPersistence = null

  /**
   * The MQTT client.
   */
  private var mqttClient: MqttAsyncClient = null

  /**
   * The connection options to the MQTT broker.
   */
  private val mqttConnectOptions = new MqttConnectOptions()

  /**
   * The list of connection listeners.
   */
  private var connectionListeners: List[MqttConnectionListener] = List()

  /**
   * The list of subscribers.
   */
  private var subscribers: List[MqttSubscriber] = List()

  override def onStartup(): Unit = {
    try {
      persistence = new MemoryPersistence()
      mqttClient =
        new MqttAsyncClient(mqttBrokerDescription.brokerAddress, mqttClientId, persistence)

      mqttClient.setCallback(new MqttCallbackExtended() {
        override def connectComplete(reconnect: Boolean, serverURI: String): Unit = {
          log.error("MQTT connection successful to " + mqttBrokerDescription.brokerAddress)

          brokerConnectSuccessful(reconnect)
        }

        override def connectionLost(cause: Throwable): Unit = {
          log.error("Lost MQTT connection to " + mqttBrokerDescription.brokerAddress, cause)

          brokerConnectLost()
        }

        override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
          log.info("Got delivery token " + token.getResponse())
        }

        override def messageArrived(topic: String, message: MqttMessage): Unit = {
          log.info("Received top level MQTT message")
          //handleMessageArrived(topic, message)
        }
      })

      mqttConnectOptions.setCleanSession(true)
      mqttConnectOptions.setAutomaticReconnect(mqttBrokerDescription.autoreconnect)
      if (mqttBrokerDescription.userName.isDefined) {
        mqttConnectOptions.setUserName(mqttBrokerDescription.userName.get)
        mqttConnectOptions.setPassword(mqttBrokerDescription.password.get.toCharArray())
      }
      if (mqttBrokerDescription.isSsl) {
        mqttConnectOptions.setSocketFactory(configureSSLSocketFactory())
      }

      log.info("Connecting to broker: " + mqttClient.getServerURI())

      mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
        override def onSuccess(token: IMqttToken): Unit = {
          log.info("MQTT broker connect is successful on token " + token)
        }

        override def onFailure(token: IMqttToken, cause: Throwable): Unit = {
          log.error("MQTT broker connect has failure on token " + token, cause)

          brokerConnectFailure()
        }
      })
    } catch {
      case e: Throwable => throw new SmartSpacesException("Error when connecting to MQTT broker", e)
    }
  }

  override def onShutdown(): Unit = {
    if (mqttClient != null && mqttClient.isConnected()) {
      try {
        mqttClient.disconnect()
      } catch {
        case e: Throwable => log.error("Could not disconnect the MQTT client", e)
      }
      mqttClient = null
    }
  }

  override def getMqttBrokerDescription(): MqttBrokerDescription = {
    return mqttBrokerDescription
  }

  override def getMqttClientId(): String = {
    return mqttClientId
  }

  override def addConnectionListener(listener: MqttConnectionListener): MqttCommunicationEndpoint = {
    synchronized {
      this.connectionListeners = listener :: connectionListeners
    }

    this
  }

  override def subscribe(topicName: String, listener: MqttSubscriberListener, qos: Int, autoreconnect: Boolean): MqttCommunicationEndpoint = {
    val subscriber = new MqttSubscriber(topicName, listener, qos, autoreconnect)

    if (resourceState == ManagedResourceState.STARTED && mqttClient.isConnected()) {
      subscriber.subscribe()
    }

    synchronized {
      subscribers = subscriber :: subscribers
    }

    this
  }

  override def createMessagePublisher(mqttTopicName: String, qos: Integer, retain: Boolean): MqttPublisher = {
    new MqttPublisherShim(mqttTopicName, qos, retain)
  }

  override def isConnected: Boolean = {
    mqttClient != null && mqttClient.isConnected()
  }

  override def getLog(): Log = {
    log
  }

  /**
   * The broker connection was successful.
   *
   * @param reconnect
   *      {@code true} if a reconnection success
   */
  private def brokerConnectSuccessful(reconnect: Boolean): Unit = {
    // Subscribe all subscribers.
    subscribers.foreach { (subscriber) =>
      if (!reconnect || subscriber.autoreconnect) {
        subscriber.subscribe
      }
    }

    connectionListeners.foreach { listener =>
      try {
        listener.onMqttConnectionSuccessful(this, reconnect)
      } catch {
        case e: Throwable => log.error("MQTT connection listener failed on connectionSuccessful", e)
      }
    }
  }

  /**
   * The broker connection was lost.
   */
  private def brokerConnectLost(): Unit = {
    connectionListeners.foreach { listener =>
      try {
        listener.onMqttConnectionLost(this)
      } catch {
        case e: Throwable => log.error("MQTT connection listener failed on connectionLost", e)
      }
    }
  }

  /**
   * The broker connection never happened.
   */
  private def brokerConnectFailure(): Unit = {
    connectionListeners.foreach { listener =>
      try {
        listener.onMqttConnectionFailure(this)
      } catch {
        case e: Throwable => log.error("MQTT connection listener failed on connectionLost", e)
      }
    }
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
  
  /**
   * An MQTT subscriber that interfaces with the Paho client.
   *
   * @author Keith M. Hughes
   */
  private class MqttSubscriber(val subscribedTopicName: String, val listener: MqttSubscriberListener, val qos: Int, val autoreconnect: Boolean) extends IMqttMessageListener {

    /**
     * Subscribe the subscriber to the broker.
     */
    def subscribe(): Unit = {
      log.info("Subscribing to MQTT topic " + subscribedTopicName)

      try {
        mqttClient.subscribe(subscribedTopicName, qos, this)
      } catch {
        case e: MqttException => throw SmartSpacesException.newFormattedException(e, "Could not subscribe to MQTT topic %s",
          subscribedTopicName)
      }
    }

    override def messageArrived(incomingTopicName: String, message: MqttMessage): Unit = {
      try {
        listener.handleMessage(PahoMqttCommunicationEndpoint.this, incomingTopicName, message.getPayload())
      } catch {
        case e: Throwable => log.error(String.format("Error while handling MQTT message on topic %s", subscribedTopicName), e)
      }
    }
  }

  /**
   * An MQTT publisher that interfaces with the Paho client.
   *
   * @author Keith M. Hughes
   */
  private class MqttPublisherShim(override val mqttTopicName: String, override val qos: Integer, override val retain: Boolean) extends MqttPublisher {

    override def writeMessage(message: Array[Byte]): Unit = {
      mqttClient.publish(mqttTopicName, message, qos, retain)
    }
  }
}
