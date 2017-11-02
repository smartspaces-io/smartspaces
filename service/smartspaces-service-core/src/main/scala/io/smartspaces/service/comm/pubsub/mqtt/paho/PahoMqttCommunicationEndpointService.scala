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
   * The managed scope for the service.
   *
   * TODO(keith): make a mixin
   */
  private var serviceManagedScope: ManagedScope = _

  /**
   * Map of client IDs to endpoints.
   */
  private var endpoints: Map[String, MqttCommunicationEndpoint] = Map()

  override def getName(): String = {
    MqttCommunicationEndpointService.SERVICE_NAME
  }

  override def onStartup(): Unit = {
    serviceManagedScope = StandardManagedScope.newManagedScope(
      getSpaceEnvironment.getExecutorService,
      getSpaceEnvironment.getLog)
    serviceManagedScope.startup()
  }

  override def onShutdown(): Unit = {
    serviceManagedScope.shutdown()
  }

  override def newMqttCommunicationEndpoint(
      mqttBrokerDescription: MqttBrokerDescription): MqttCommunicationEndpoint = {
    newMqttCommunicationEndpoint( mqttBrokerDescription, None, None, None)
  }

  override def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription,
    mqttClientId: Option[String],
    managedScope: Option[ManagedScope],
    log: Option[ExtendedLog]): MqttCommunicationEndpoint = {

    var finalMqttClientId: String = ""
    var finalManagedScope: ManagedScope = null
    var finalLog: ExtendedLog = getSpaceEnvironment.getLog

    if (mqttClientId.isDefined) {
      finalMqttClientId = mqttClientId.get
      finalManagedScope = managedScope.get
      finalLog = log.get
    } else if (mqttBrokerDescription.brokerClientId.isDefined) {
      finalMqttClientId = mqttBrokerDescription.brokerClientId.get
      finalManagedScope = serviceManagedScope
    } else {
      throw new SmartSpacesException(s"MQTT communication endpoint cannot be created because of no client id, broker: ${mqttBrokerDescription}")
    }

    val lookedupEndpoint = endpoints.get(finalMqttClientId)
    if (lookedupEndpoint.isDefined) {
      lookedupEndpoint.get
    } else {

      val endpoint = new PahoMqttCommunicationEndpoint(mqttBrokerDescription, finalMqttClientId, finalLog)
      finalManagedScope.addResource(endpoint)

      endpoints = endpoints + (finalMqttClientId -> endpoint)

      endpoint
    }
  }
}
