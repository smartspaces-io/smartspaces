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

import io.smartspaces.messaging.MessageSender

import java.util.Set

/**
 * A collection of MQTT publishers for a given message.
 * 
 * @param <T>
 *          the type of messages
 *
 * @author Keith M. Hughes
 */
trait MqttPublishers[T] extends MessageSender[T] {

  /**
   * Add publishers to the collection.
   * 
   * @param mqttClient
   *          the MQTT client that the topics will be published to
   * @param topicNames
   *          the topic names
   */
  def addPublishers(mqttClient: PahoMqttClient,  topicNames: Set[String]): Unit

  /**
   * Shut down all publishers.
   */
  def shutdown(): Unit
}