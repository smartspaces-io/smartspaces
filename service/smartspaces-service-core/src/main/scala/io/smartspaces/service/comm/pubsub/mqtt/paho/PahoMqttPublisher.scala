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

import org.eclipse.paho.client.mqttv3.MqttTopic

/**
 * A listener for MQTT subscriber events.
 *
 * @author Keith M. Hughes
 */
class PahoMqttPublisher(override val mqttTopicName: String, override val qos: Integer, override val retain: Boolean, private val mqttTopic: MqttTopic) extends MqttPublisher {

  override def sendMessage(message: Array[Byte]): Unit = {
    mqttTopic.publish(message, qos, retain)
  }
}
