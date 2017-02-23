/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
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

import io.smartspaces.logging.ExtendedLog

import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.util.List
import java.util.Set
import java.util.concurrent.CopyOnWriteArrayList

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet

/**
 * A collection of MQTT subscribers for a given set of topics.
 *
 * @author Keith M. Hughes
 */
class StandardMqttSubscribers(nodeName: String, log: ExtendedLog) extends MqttSubscribers {

  /**
   * The clients for sending the messages.
   */
  private val clients: List[MqttClient] = new CopyOnWriteArrayList

  override def addSubscribers(mqttBroker: MqttBrokerDescription, topicNames: Set[String],
    callback: MqttCallback): Unit = {
    // TODO(keith): Make this settable and configurable
    val persistence = new MemoryPersistence()

    log.debug(String.format("Adding subscribers for topic names %s to MQTT master %s", topicNames,
      mqttBroker))

    topicNames.foreach { topicName =>

      log.debug(String.format("Adding subscriber topic %s", topicName))
      var client: MqttClient = null

      try {
        // TODO(keith): Map topics to the particular MQTT client so when
        // transmitting, we get the correct client for that topic.
        client = new MqttClient(mqttBroker.brokerAddress, nodeName, persistence)
      } catch {
        case e: MqttException =>
          log.error(String.format("Failed adding subscriber topic %s", topicName), e)
      }

      log.debug(String.format("Added subscriber topic %s", topicName))
      client.setCallback(callback)

      val options = new MqttConnectOptions()
      options.setCleanSession(true)
      // options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1)

      log.info("Connecting to broker: " + client.getServerURI())

      try {
        client.connect(options)

        client.subscribe(topicName)

        clients.add(client)
      } catch {
        case e: Throwable =>
          log.error("MQTT connect failed", e)
      }
    }
  }

  override def shutdown(): Unit = {
    clients.foreach { client =>
      try {
        client.disconnect()
      } catch {
        case e: MqttException =>
          log.error("MQTT could not disconnect from client", e)
      }
    }
  }
}
