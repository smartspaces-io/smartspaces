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

package io.smartspaces.sensor.model

import io.smartspaces.sensor.domain.MeasurementTypeDescription

/**
 * The value of a sensor.
 *
 * @param <T>
 *          the type of the value
 *
 * @author Keith M. Hughes
 */
trait SensedValue[+T <: Any] {

  /**
   * The model of the sensor that gave the value.
   */
  def sensorChannel: SensorChannelEntityModel

  /**
   * The timestamp of when the measurement was taken, 
   * in milliseconds since the epoch.
   */
  def timestampMeasurement: Long

  /**
   * The timestamp of when the sensor measurement was received, 
   * in milliseconds since the epoch.
   */
  def timestampMeasurementReceived: Long
  
  /**
   * The value of the sensor.
   *
   * @return the value of the sensor
   */
  def value: T
  
  /**
   * Additional value from the sensor, if any.
   */
  def additional: Option[Any]
}
