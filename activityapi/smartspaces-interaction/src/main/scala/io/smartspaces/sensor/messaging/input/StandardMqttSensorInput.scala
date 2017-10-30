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

package io.smartspaces.sensor.messaging.input

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.messaging.codec.DynamicObjectByteArrayCodec
import io.smartspaces.sensor.processing.SensorProcessor
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

/**
 * A sensor input for sensor message over MQTT.
 * 
 * <p>
 * This sensor input allocates an MQTT client.
 *
 * @author Keith M. Hughes
 */
class StandardMqttSensorInput(
    private val mqttBrokerDescription: MqttBrokerDescription, 
    private val mqttClientId: String,
    private val spaceEnvironment: SmartSpacesEnvironment, 
    private val log: ExtendedLog) extends MqttSensorInput {

  /**
   * The sensor processor the sensor input is running under.
   */
  private var sensorProcessor: SensorProcessor = null

  /**
   * The MQTT client endpoint.
   */
  private var mqttEndpoint: MqttCommunicationEndpoint = null
  
  /**
   * The listener for MQTT messages.
   */
  private var mqttListener: StandardMqttListenerSensorInput = _

  /**
   * The codec for translating messages.
   */
  private val codec = new DynamicObjectByteArrayCodec()

  override def startup(): Unit = {
    val service: MqttCommunicationEndpointService = spaceEnvironment.getServiceRegistry()
      .getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME)
    mqttEndpoint = service.newMqttCommunicationEndpoint(mqttBrokerDescription, mqttClientId, log)

    mqttEndpoint.startup()
    
    mqttListener = new StandardMqttListenerSensorInput(spaceEnvironment.getTimeProvider, log)
  }

  override def shutdown(): Unit = {
    if (mqttEndpoint != null) {
      mqttEndpoint.shutdown()
    }
  }

  override def setSensorProcessor(sensorProcessor: SensorProcessor): Unit = {
    mqttListener.setSensorProcessor(sensorProcessor)
  }

  /**
   * Add in a new MQTT subscription.
   *
   * @param mqttSensorTopicName
   *       the sensor topic name
   * @param qos
   *       the Quality of Service for the connection
   */
  def addMqttSubscription(mqttSensorTopicName: String, qos: Int): Unit = {
    mqttEndpoint.subscribe(mqttSensorTopicName, qos, true, mqttListener)

    log.info(s"Subscribing to MQTT topic $mqttSensorTopicName with QoS $qos")
  }
}
