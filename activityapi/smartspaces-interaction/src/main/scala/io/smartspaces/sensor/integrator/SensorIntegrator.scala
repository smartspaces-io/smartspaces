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

package io.smartspaces.sensor.integrator

import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.sensor.entity.SensorDescriptionImporter
import io.smartspaces.sensor.entity.model.query.SensedEntityModelQueryProcessor
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription
import io.smartspaces.sensor.input.MqttSensorInput
import io.smartspaces.sensor.input.SensorInput

/**
 * The sensor integration layer.
 *
 * <p>
 * It is important to start up the integrator so that its observables are available for subscribing to.
 *
 * @author Keith M. Hughes
 */
trait SensorIntegrator extends ManagedResource {

  /**
   * The sensor query processor.
   */
  def queryProcessor: SensedEntityModelQueryProcessor

  /**
   * The description importer
   */
  var descriptionImporter: SensorDescriptionImporter

  /**
   * Add in an MQTT sensor input.
   *
   * @param brokerDescription
   *      the description for the MQTT broker
   * @param clientId
   *      the broker client ID to use
   *      
   * @return the MQTT sensor input that has been created
   */
  def addMqttSensorInput(brokerDecription: MqttBrokerDescription, clientId: String): MqttSensorInput

  /**
   * Add in a sensor input.
   *
   * @param brokerDescription
   *      the description for the MQTT broker
   * @param clientId
   *      the broker client ID to use
   *      
   * @return the MQTT sensor input that has been created
   */
  def addSensorInput(sensorInput: SensorInput): Unit
}