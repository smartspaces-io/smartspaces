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

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.service.SupportedService
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

/**
 * A service for getting MQTT communication endpoints.
 * 
 * @author Keith M. Hughes
 */
object MqttCommunicationEndpointService {

  /**
   * Name for the service.
   */
  val SERVICE_NAME = "comm.pubsub.mqtt"
}

/**
 * A service for getting MQTT communication endpoints.
 * 
 * @author Keith M. Hughes
 */
trait MqttCommunicationEndpointService extends SupportedService {

  /**
   * Construct a new endpoint.
   * 
   * @param mqttBrokerDescription
   *          the description of the MQTT broker
   * @param mqttClientId
   *          the ID for the MQTT client
   * @param log
   *          the log to use
   * 
   * @return The new endpoint
   */

   def newMqttCommunicationEndpoint(
       mqttBrokerDescription: MqttBrokerDescription,  mqttClientId: String, log: ExtendedLog): MqttCommunicationEndpoint
}
