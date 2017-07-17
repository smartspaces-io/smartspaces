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

package io.smartspaces.service.comm.pubsub.mqtt

import io.smartspaces.messaging.MessageWriter

/**
 * A listener for MQTT subscriber events.
 *
 * @author Keith M. Hughes
 */
trait MqttPublisher extends MessageWriter[Array[Byte]] {
  
  /**
   * The MQTT topic.
   */
  val mqttTopicName: String
  
  /**
   * The default QoS for all messages.
   */
  val qos: Int
  
  /**
   * The default retain value for messages.
   */
  val retain: Boolean
  
  /**
   * Send a message.
   *
   * <p>
   * The message will be serialized properly for the channel.
   *
   * @param message
   *          the message to send
   * @param retain
   *          [[code true]] if the message should be retained
   */
  def writeMessage(message: Array[Byte], retain: Boolean): Unit
  
  /**
   * Is the publisher connected?
   * 
   * @return {@code true} if connected
   */
  def isConnected(): Boolean
}
