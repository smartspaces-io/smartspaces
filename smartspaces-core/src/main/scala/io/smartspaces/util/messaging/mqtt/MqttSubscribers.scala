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

import java.util.Set

import org.eclipse.paho.client.mqttv3.IMqttMessageListener

/**
 * A collection of MQTT subscribers that should all receive the same message.
 *
 * @author Keith M. Hughes
 */
trait MqttSubscribers {

  /**
   * Add a collection of subscriber topics to the collection.
   *
   * @param mqttClient
   *          the MQTT broker to be subscribed to
   * @param topicNames
   *          the topic names to be subscribed to from the master
   * @param callback
   *          the callback for all message responses
   */
  def addSubscribers(mqttClient: PahoMqttClient, topicNames: Set[String],
    callback: IMqttMessageListener): Unit

  /**
   * Shut down all subscribers.
   */
  def shutdown(): Unit
}