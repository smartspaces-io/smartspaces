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
import io.smartspaces.scope.ManagedScope
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
   * Construct a new endpoint scoped at the service level. The MQTT broker description must include a client ID.
   *
   * @param mqttBrokerDescription
   *          the description of the MQTT broker
   *
   * @return the new endpoint
   */

  def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription): MqttCommunicationEndpoint

  /**
   * Construct a new endpoint.
   *
   * If the broker description has a client ID, it will be used. Otherwise the client ID
   * here will be used.
   *
   * If the broker description has a client ID, the client will be scoped to the service.
   * otherwise the supplied scope will be used.
   *
   * @param mqttBrokerDescription
   *          the description of the MQTT broker
   * @param mqttClientId
   *          the optional ID for the MQTT client
   * @param managedScope
   *          the optional managed scope
   * @param log
   *          the optional log to use
   *
   * @return the new endpoint
   */

  def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription,
    mqttClientId: Option[String],
    managedScope: Option[ManagedScope],
    log: Option[ExtendedLog]): MqttCommunicationEndpoint
}
