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

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel

/**
 * A listener for sensor events for sensed entities.
 *
 * @author Keith M. Hughes
 */
trait SensedEntitySensorListener {

  /**
   * Handle sensor data that has come in.
   *
   * @param handler
   *          the handler the sensor data came in on
   * @param timestamp
   *          the time the sensor event came in
   * @param sensor
   *          the sensor the data came in on
   * @param sensedEntity
   *          the entity the sensor gives data for
   * @param data
   *          the sensor data
   */
  def handleSensorData(handler: SensedEntitySensorHandler, timestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, data: DynamicObject): Unit
}
