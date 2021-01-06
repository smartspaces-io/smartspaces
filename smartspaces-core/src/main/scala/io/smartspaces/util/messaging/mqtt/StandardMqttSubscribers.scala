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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener

import java.util.List
import java.util.Set
import java.util.concurrent.CopyOnWriteArrayList

import scala.collection.JavaConverters._

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

  override def addSubscribers(mqttClient: PahoMqttClient, topicNames: Set[String],
      callback: IMqttMessageListener): Unit = {

    log.debug(s"Adding subscribers for topic names ${topicNames} to MQTT master ${mqttClient.mqttBrokerDescription}")

    topicNames.asScala.foreach { topicName =>

      log.debug(s"Adding subscriber topic ${topicName}")

        mqttClient.mqttClient.subscribe(topicName, 1, callback)
    }
  }

  override def shutdown(): Unit = {
  }
}
