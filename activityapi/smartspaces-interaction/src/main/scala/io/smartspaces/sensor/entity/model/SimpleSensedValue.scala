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

import io.smartspaces.sensor.entity.MeasurementTypeDescription

/**
 * A sensed value from a sensor.
 * 
 * @author Keith M. Hughes
 */
class SimpleSensedValue[T <: Any](val sensor: SensorEntityModel, val valueType: MeasurementTypeDescription,
    val value: T, val timestamp: Long) extends SensedValue[T] {

  override def toString() = {
    "SimpleSensedValue [sensor=" + sensor + ", valueType=" +
      valueType + ", value=" + value + ", timestamp=" + timestamp + "]"
  }
}
