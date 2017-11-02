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

package io.smartspaces.sensor.test

import io.smartspaces.messaging.codec.DynamicObjectByteArrayCodec
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService
import io.smartspaces.service.comm.pubsub.mqtt.MqttPublisher
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

class StandardTestClient(private val spaceEnvironment: SmartSpacesEnvironment, private val mqttBrokerDescription: MqttBrokerDescription, private val mqttTopicName: String) extends IdempotentManagedResource {

  /**
   * The MQTT client endpoint.
   */
  private var mqttEndpoint: MqttCommunicationEndpoint = null

  private var mqttMessageWriter: MqttPublisher = null

  override def onStartup(): Unit = {
    val service: MqttCommunicationEndpointService = spaceEnvironment.getServiceRegistry()
      .getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME)
    mqttEndpoint = service.newMqttCommunicationEndpoint(mqttBrokerDescription, Option("testclient"), Option(spaceEnvironment.getContainerManagedScope), Option(spaceEnvironment.getLog))

    mqttEndpoint.startup()

    mqttMessageWriter = mqttEndpoint.createMessagePublisher(mqttTopicName, 0, false)
  }

  override def onShutdown(): Unit = {
    mqttEndpoint.shutdown()
  }

  def goToLivingRoom(): Unit = {
    publishMessage("""
{ "sensor": "/sensornode/ESP_8C8E1B", 
  "data": {   
    "marker": {     
      "type": "/sensor/marker",     
      "value": "nfc:04E17DDAC94880"}}}
""")
  }

  def goToKitchen(): Unit = {
    publishMessage("""
{ "sensor": "/sensornode/ESP_8C8E1C", 
  "data": {   
    "marker": {     
      "type": "/sensor/marker",     
      "value": "nfc:04E17DDAC94880"}}}
""")
  }

  def publishMessage(message: String): Unit = {
    spaceEnvironment.getLog.info(s"Publishing $message")
    mqttMessageWriter.sendMessage(message.getBytes(DynamicObjectByteArrayCodec.CHARSET_DEFAULT))
  }
}