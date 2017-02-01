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
import io.smartspaces.util.data.json.StandardJsonMapper

/**
 * The description of an MQTT subscriber.
 *
 * @author Keith M. Hughes
 */
object MqttSubscriberDescription {

  /**
   * Parse a description string into an MQTT broker description.
   */
  def parse(description: String): MqttSubscriberDescription = {
    val mapposition = description.indexOf("@")

    val topicName = if (mapposition == -1) description else description.substring(0, mapposition)

    val subscriberDescription = new MqttSubscriberDescription(topicName)

    if (mapposition != -1) {
      val params = StandardJsonMapper.INSTANCE.parseObject(description.substring(mapposition + 1))

      subscriberDescription.qos = Option(params.get("qos").asInstanceOf[Integer])
      subscriberDescription.autoreconnect = Option(params.get("retain").asInstanceOf[Boolean])

      subscriberDescription
    } else {
      throw new SmartSpacesException(s"MQTT publisher description has the wrong syntax: ${description}")
    }
  }
}

/**
 * A description of an MQTT subscriber.
 * 
 * @author Keith M. Hughes
 */
class MqttSubscriberDescription(val topicName: String) {
  
  /**
   * The quality of service for this publisher.
   */
  var qos: Option[Int] = None
  
  /**
   * [[code true]] if the subscriber should be autoreconnected.
   */
  var autoreconnect: Option[Boolean] = None
}