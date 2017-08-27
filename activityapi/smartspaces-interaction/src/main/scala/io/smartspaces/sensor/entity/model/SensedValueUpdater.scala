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

package io.smartspaces.sensor.entity.model

import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * An updater for a sensed value.
 */
trait SensedValueUpdater {

  /**
   * Create a new sensed value for a channel.
   *
   * @param sensor
   * 		the sensor that is providing the value
   * @param channelId
   *    ID of the channel the data came in on
   * @param data
   *    the data message for the sensor update
   * @param timestamp
   *    the timestamp of when the data came in
   */
  def createSensedValue(sensor: SensorEntityDescription, channelId: String, data: DynamicObject, timestamp: Long): SensedValue[Any]
}