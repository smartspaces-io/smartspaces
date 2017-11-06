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

package io.smartspaces.service.comm.pubsub.mqtt.paho

import io.smartspaces.SmartSpacesException
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.scope.ManagedScope
import io.smartspaces.scope.StandardManagedScope
import io.smartspaces.service.BaseSupportedService
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

/**
 * A service for getting MQTT communication endpoints implemented with Paho.
 *
 * @author Keith M. Hughes
 */
class PahoMqttCommunicationEndpointService extends BaseSupportedService
  with MqttCommunicationEndpointService with IdempotentManagedResource {

  /**
   * Map of client IDs to endpoints.
   */
  private var endpoints: Map[String, MqttCommunicationEndpoint] = Map()

  override def getName(): String = {
    MqttCommunicationEndpointService.SERVICE_NAME
  }

  override def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription): MqttCommunicationEndpoint = {
    if (mqttBrokerDescription.brokerClientId.isDefined) {
      newMqttCommunicationEndpoint(mqttBrokerDescription, mqttBrokerDescription.brokerClientId.get, getSpaceEnvironment.getLog)
    } else {
      throw new SmartSpacesException(s"Wanted an MQTT broker description global client ID but none defined. ${mqttBrokerDescription}")
    }
  }

  override def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription,
    mqttClientId: String,
    log: ExtendedLog): MqttCommunicationEndpoint = {

    val lookedupEndpoint = endpoints.get(mqttClientId)
    if (lookedupEndpoint.isDefined) {
      lookedupEndpoint.get
    } else {

      val endpoint = new PahoMqttCommunicationEndpoint(mqttBrokerDescription, mqttClientId, log)

      endpoints = endpoints + (mqttClientId -> endpoint)

      endpoint
    }
  }
}
