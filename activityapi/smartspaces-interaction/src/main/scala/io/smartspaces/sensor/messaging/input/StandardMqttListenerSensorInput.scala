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

import io.smartspaces.sensor.processing.SensorProcessor
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener
import io.smartspaces.messaging.codec.DynamicObjectByteArrayCodec
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.time.provider.TimeProvider
import io.smartspaces.resource.managed.IdempotentManagedResource

/**
 * An MQTT message handler that transfers its message to a sensor input.
 *
 * @author Keith M. Hughes
 */
class StandardMqttListenerSensorInput(
    private val timeProvider: TimeProvider, 
    private val log: ExtendedLog) extends MqttSubscriberListener with SensorInput with IdempotentManagedResource {

  /**
   * The sensor processor the sensor input is running under.
   */
  private var sensorProcessor: SensorProcessor = null

  /**
   * The codec for translating messages.
   */
  private val codec = new DynamicObjectByteArrayCodec()

  override def setSensorProcessor(sensorProcessor: SensorProcessor): Unit = {
    this.sensorProcessor = sensorProcessor
  }
  
  override def handleMessage(endpoint: MqttCommunicationEndpoint, topicName: String,
    payload: Array[Byte]): Unit = {

    val message = codec.decode(payload)
    log.formatDebug("Got sensor message on topic %s", topicName)

    // TODO(keith): Consider also checking message to see if it has a timestamp.
    // If so use it, otherwise use time provider.
    val currentTime = timeProvider.getCurrentTime()

    sensorProcessor.processSensorData(currentTime, message)
  }
}
