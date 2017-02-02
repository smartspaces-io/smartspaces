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

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * A processor for sensor data.
 *
 * @author Keith M. Hughes
 */
trait SensorProcessor extends ManagedResource {

  /**
   * Add in a new sensor input.
   *
   * @param sensorInput
   *          the sensor input to add
   *
   * @return this processor
   */
  def addSensorInput(sensorInput: SensorInput): SensorProcessor

  /**
   * Add in a new sensor handler.
   *
   * @param sensorInput
   *          the sensor handler to add
   *
   * @return this processor
   */
  def addSensorHandler(sensorHandler: SensorHandler): SensorProcessor

  /**
   * Process the given dynamic object as sensor data.
   *
   * @param timestamp
   *          the time the data came in
   * @param sensorDataEvent
   *          the sensor data
   */
  def processSensorData(timestamp: Long, sensorDataEvent: DynamicObject): Unit

  /**
   * The logger for the processor.
   */
  val log: ExtendedLog
}
