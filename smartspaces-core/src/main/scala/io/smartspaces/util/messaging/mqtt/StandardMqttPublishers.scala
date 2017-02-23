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

package io.smartspaces.util.messaging.mqtt

import io.smartspaces.SmartSpacesException
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.util.io.FileSupportImpl

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

import java.util.List
import java.util.Set
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet

/**
 * A collection of MQTT publishers for a given message topic.
 *
 * @param <T>
 *          the type of messages
 *
 * @author Keith M. Hughes
 */
class StandardMqttPublishers[T](nodeName: String, messageEncoder: MessageEncoder[T, Array[Byte]],
    log: ExtendedLog) extends MqttPublishers[T] {

  /**
   * The clients for sending the messages.
   */
  private val clients: List[MqttClientInformation] = new CopyOnWriteArrayList()

  /**
   * The persistence for the client.
   */
  private var persistence: MqttClientPersistence = null

    val actionListener = new IMqttActionListener() {
      override def onSuccess(token: IMqttToken): Unit = {
        log.debug("MQTT message sent successfully")
      }

      override def onFailure(token: IMqttToken, throwable: Throwable): Unit = {
        log.error("MQTT message failed")
      }
    }

  private val fileSupport = FileSupportImpl.INSTANCE

  override def addPublishers(mqttBrokerDescription: MqttBrokerDescription, topicNames: Set[String]): Unit = {
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

    log.debug(s"Adding publishers for topic names ${topicNames} to MQTT master ${mqttBrokerDescription}")

    topicNames.foreach { topicName =>

      log.debug(String.format("Adding publisher topic %s", topicName))
      var client: MqttAsyncClient = null

      try {
        // TODO(keith): Create map of MQTT masters to MqttClientinformation
        // object and have set of topics inside the client info.
        client = new MqttAsyncClient(mqttBrokerDescription.brokerAddress, nodeName, persistence)
      } catch {
        case e: MqttException =>
          log.error(String.format("Failed adding publisher topic %s", topicName), e)
      }

      log.debug(s"Added publisher topic ${topicName}")
      client.setCallback(new MqttCallback() {
        override def connectionLost(cause: Throwable): Unit = {
          log.error("Lost connection to MQTT server", cause)
        }

        override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
          log.debug("Got MQTT delivery token " + token.getResponse())
        }

        override def messageArrived(topic: String, message: MqttMessage): Unit = {
          // Not needed since not subscribing.
        }
      })

      val options = new MqttConnectOptions()
      options.setCleanSession(true)

      log.info("Connecting to broker: " + client.getServerURI())
      var connectHappened = new CountDownLatch(1)
      var connectStarted = true
      try {
        client.connect(options, new IMqttActionListener() {
          override def onSuccess(token: IMqttToken): Unit = {
            log.info("Connect Listener has success on token " + token)
            connectHappened.countDown()
          }

          override def onFailure(token: IMqttToken, cause: Throwable): Unit = {
            log.error("Connect Listener has failure on token " + token, cause)
            connectHappened.countDown()
          }
        })
      } catch {
        case e: Throwable =>
          log.error("MQTT connect failed", e)
          connectStarted = false
      }

      if (connectStarted) {
        try {
          if (connectHappened.await(10000, TimeUnit.MILLISECONDS)) {
            clients.add(new MqttClientInformation(client, topicName));
          }
        } catch {
          case e: InterruptedException =>
            log.error("MQTT connect failed from interrupted wait for connection", e)
        }
      }
    }
  }

  override def publishMessage(message: T): Unit = {
    val mqttMessage = new MqttMessage(messageEncoder.encode(message))
    mqttMessage.setQos(1)

    clients.foreach { client =>
      try {
        client.mqttClient.publish(client.topicName, mqttMessage, null, actionListener);
      } catch {
        case e: Throwable =>
          log.error("MQTT message publish failed", e)
      }
    }
  }

  override def shutdown(): Unit = {
    this.synchronized {
      clients.foreach { client =>
        try {
          client.mqttClient.disconnect()
        } catch {
          case e: MqttException =>
            log.error("MQTT could not disconnect from client", e)
        }
      }
    }
  }

  /**
   * Information about an MQTT connection.
   *
   * @author Keith M. Hughes
   */
  class MqttClientInformation(val mqttClient: MqttAsyncClient, val topicName: String) {
  }
}
