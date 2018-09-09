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
import io.smartspaces.data.entity.CategoricalValueInstance

/**
 * A sensed value from a sensor.
 *
 * @author Keith M. Hughes
 */
class SimpleSensedValue[T <: Any](
    override val sensorChannel: SensorChannelEntityModel,
    override val value: T,
    override val additional: Option[Any],
    override val timestampMeasurement: Long,
    override val timestampMeasurementReceived: Long) extends SensedValue[T] {

  override def toString() = {
    "SimpleSensedValue [sensorChannel=" + sensorChannel + 
    ", value=" + value + ", additional=" + additional + 
    ", timestampMeasurement=" + timestampMeasurement + 
    ", timestampMeasurementReceived=" + timestampMeasurementReceived + "]"
  }
}

class SimpleNumericContinuousSensedValue(
    sensorChannel: SensorChannelEntityModel,
    value: Double,
    additional: Option[Any],
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) extends SimpleSensedValue[Double](sensorChannel, value, additional, measurementTimestamp, sensorMessageReceivedTimestamp) {
  def this(
    sensorChannel: SensorChannelEntityModel,
    value: Double,
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) = {
    this(sensorChannel, value, None, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
}

class SimpleCategoricalValueSensedValue(
    sensorChannel: SensorChannelEntityModel,
    value: CategoricalValueInstance,
    additional: Option[Any],
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) extends SimpleSensedValue[CategoricalValueInstance](sensorChannel, value, additional, measurementTimestamp, sensorMessageReceivedTimestamp) {
  def this(
    sensorChannel: SensorChannelEntityModel,
    value: CategoricalValueInstance,
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) = {
    this(sensorChannel, value, None, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
}

