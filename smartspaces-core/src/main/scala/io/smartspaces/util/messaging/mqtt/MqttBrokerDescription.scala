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

/**
 * The description of an MQTT broker.
 *
 * @author Keith M. Hughes
 */
object MqttBrokerDescription {
  
  /**
   * Parse a description string into an MQTT broker description.
   */
  def parse(description: String): MqttBrokerDescription = {
    val isSsl = description.startsWith("ssl")
    val postLocation = description.lastIndexOf(":")
    
    val brokerHost = description.substring(6, postLocation)
    val brokerPort = description.substring(postLocation+1).toInt
    
    new MqttBrokerDescription(brokerHost, brokerPort, isSsl)
  }
}

/**
 * The description of an MQTT broker.
 *
 * @author Keith M. Hughes
 */
class MqttBrokerDescription(val brokerHost: String, val brokerPort: Integer, val isSsl: Boolean) {

  /**
   * The network address of the broker.
   */
  val brokerAddress = (if (isSsl) "ssl" else "tcp") + "://" + brokerHost + ":" + brokerPort

  /**
   * The username for the broker.
   */
  var userName: Option[String] = None

  /**
   * The password for the broker.
   */
  var password: Option[String] = None
  
  /**
   * {@code true} if should reautoconnect back to the broker.
   */
  var autoreconnect = false
}
