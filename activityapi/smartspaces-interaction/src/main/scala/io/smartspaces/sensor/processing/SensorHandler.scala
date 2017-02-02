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

import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * A handler for sensor data in a sensor processor.
 * 
 * @author Keith M. Hughes
 */
trait SensorHandler extends ManagedResource {

  /**
   * Handle sensor data that has come in.
   * 
   * @param timestamp
   *          the time the sensor event came in
   * @param data
   *          the sensor data
   */
  def handleSensorData(timestamp: Long,  data: DynamicObject): Unit

  /**
   * The sensor processor the handler is running under.
   */
  var sensorProcessor: SensorProcessor
}
