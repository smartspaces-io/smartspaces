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

package io.smartspaces.sensor.processing

/**
 * A sensor input that uses MQTT.
 *
 * @author Keith M. Hughes
 */
trait MqttSensorInput extends SensorInput {
  
  /**
   * Add in a new MQTT subscription.
   *
   * @param mqttSensorTopicName
   *       the sensor topic name
   * @param qos
   *       the Quality of Service for the connection
   */
  def addMqttSubscription(mqttSensorTopicName: String, qos: Int): Unit
}