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
class StandardMqttPublishers[T](messageEncoder: MessageEncoder[T, Array[Byte]], log: ExtendedLog) extends MqttPublishers[T] {

  /**
   * The clients for sending the messages.
   */
  private val clients: List[MqttClientInformation] = new CopyOnWriteArrayList()

  /**
   * The action listener for publishers.
   */
  private val actionListener = new IMqttActionListener() {
    override def onSuccess(token: IMqttToken): Unit = {
      log.debug("MQTT message sent successfully")
    }

    override def onFailure(token: IMqttToken, throwable: Throwable): Unit = {
      log.error("MQTT message failed")
    }
  }

  override def addPublishers(mqttClient: PahoMqttClient, topicNames: Set[String]): Unit = {

    log.debug(s"Adding publishers for topic names ${topicNames} to MQTT master ${mqttClient.mqttBrokerDescription}")

    topicNames.foreach { topicName =>
      log.debug(s"Adding publisher topic ${topicName}")
      clients.add(new MqttClientInformation(mqttClient, topicName));
    }
  }

  override def sendMessage(message: T): Unit = {
    val mqttMessage = new MqttMessage(messageEncoder.encode(message))
    mqttMessage.setQos(1)

    clients.foreach { client =>
      try {
        client.mqttClient.mqttClient.publish(client.topicName, mqttMessage, null, actionListener);
      } catch {
        case e: Throwable =>
          log.error("MQTT message publish failed", e)
      }
    }
  }

  override def shutdown(): Unit = {
    // Nothing to do
  }

  /**
   * Information about an MQTT connection.
   *
   * @author Keith M. Hughes
   */
  class MqttClientInformation(val mqttClient: PahoMqttClient, val topicName: String) {
  }
}
