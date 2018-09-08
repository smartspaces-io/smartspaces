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
    override val sensor: SensorEntityModel,
    override val channelId: Option[String],
    override val measurementTypeDescription: MeasurementTypeDescription,
    override val value: T,
    override val additional: Option[Any],
    override val timestampMeasurement: Long,
    override val timestampMeasurementReceived: Long) extends SensedValue[T] {

  override def toString() = {
    "SimpleSensedValue [sensor=" + sensor + ", channelId=" + channelId + 
    ", measurementTypeDescription=" + measurementTypeDescription + 
    ", value=" + value + ", additional=" + additional + 
    ", timestampMeasurement=" + timestampMeasurement + 
    ", timestampMeasurementReceived=" + timestampMeasurementReceived + "]"
  }
}

class SimpleNumericContinuousSensedValue(
    sensor: SensorEntityModel,
    channelId: Option[String],
    measurementTypeDescription: MeasurementTypeDescription,
    value: Double,
    additional: Option[Any],
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) extends SimpleSensedValue[Double](sensor, channelId, measurementTypeDescription, value, additional, measurementTimestamp, sensorMessageReceivedTimestamp) {
  def this(
    sensor: SensorEntityModel,
    channelId: Option[String],
    measurementTypeDescription: MeasurementTypeDescription,
    value: Double,
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) = {
    this(sensor, channelId, measurementTypeDescription, value, None, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
}

class SimpleCategoricalValueSensedValue(
    sensor: SensorEntityModel,
    channelId: Option[String],
    measurementTypeDescription: MeasurementTypeDescription,
    value: CategoricalValueInstance,
    additional: Option[Any],
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) extends SimpleSensedValue[CategoricalValueInstance](sensor, channelId, measurementTypeDescription, value, additional, measurementTimestamp, sensorMessageReceivedTimestamp) {
  def this(
    sensor: SensorEntityModel,
    channelId: Option[String],
    measurementTypeDescription: MeasurementTypeDescription,
    value: CategoricalValueInstance,
    measurementTimestamp: Long,
    sensorMessageReceivedTimestamp: Long) = {
    this(sensor, channelId, measurementTypeDescription, value, None, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
}

