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

package io.smartspaces.service.comm.pubsub.mqtt.paho;

import org.apache.commons.logging.Log;

import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint;
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;
import io.smartspaces.system.StandaloneSmartSpacesEnvironment
import io.smartspaces.time.provider.LocalTimeProvider
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener

object PahoMqttCommunicationEndpointService {
  def main(args: Array[String]): Unit = {
    val spaceEnvironment = createSpaceEnvironment
    
    val service: MqttCommunicationEndpointService = spaceEnvironment.getServiceRegistry.getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME)
    
    val mqttBrokerDescription = MqttBrokerDescription.parse("ssl://hub.inhabitech.com:8883")
    mqttBrokerDescription.caCertPath = Some("/home/keith/software/repos/robotbrains-examples/comm/mqtt/java/org.robotbrains.examples.mqtt/ca.crt")
    mqttBrokerDescription.clientCertPath = Some("/home/keith/software/repos/robotbrains-examples/comm/mqtt/java/org.robotbrains.examples.mqtt/foop.crt")
    mqttBrokerDescription.clientKeyPath = Some("/home/keith/software/repos/robotbrains-examples/comm/mqtt/java/org.robotbrains.examples.mqtt/foop.key")
    val endpoint: MqttCommunicationEndpoint = service.newMqttCommunicationEndpoint(mqttBrokerDescription, "greble", spaceEnvironment.getLog)
    
    endpoint.subscribe("$SYS/broker/bytes/#", new MqttSubscriberListener() {
      override  def handleMessage(endpoint: MqttCommunicationEndpoint, topicName: String, payload: Array[Byte]): Unit = {
        spaceEnvironment.getLog.info(s"${topicName} ${new String(payload)}")
      }
    }, 0, true)
    endpoint.startup
    spaceEnvironment.addManagedResource(endpoint)
  }
  
  def createSpaceEnvironment(): StandaloneSmartSpacesEnvironment = {
    val spaceEnvironment =
      StandaloneSmartSpacesEnvironment.newStandaloneSmartSpacesEnvironment()
    spaceEnvironment.setTimeProvider(new LocalTimeProvider())

    val pahoMqttCommunicationEndpointService =
      new PahoMqttCommunicationEndpointService()
    spaceEnvironment.getServiceRegistry().startupAndRegisterService(pahoMqttCommunicationEndpointService)

    return spaceEnvironment
  }

}
/**
 * A service for getting MQTT communication endpoints implemented with Paho.
 *
 * @author Keith M. Hughes
 */
class PahoMqttCommunicationEndpointService extends BaseSupportedService
    with MqttCommunicationEndpointService {

  override def getName(): String = {
    MqttCommunicationEndpointService.SERVICE_NAME
  }

  override def newMqttCommunicationEndpoint(
    mqttBrokerDescription: MqttBrokerDescription, mqttClientId: String, log: Log): MqttCommunicationEndpoint = {
    new PahoMqttCommunicationEndpoint(mqttBrokerDescription, mqttClientId, getSpaceEnvironment.getExecutorService, log)
  }
}
