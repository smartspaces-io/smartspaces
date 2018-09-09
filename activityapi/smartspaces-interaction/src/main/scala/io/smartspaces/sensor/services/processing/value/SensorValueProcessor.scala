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

package io.smartspaces.sensor.services.processing.value

import io.smartspaces.sensor.model.SensedEntityModel
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sensor.model.SensorChannelEntityModel

/**
 * A processor for sensor value data messages.
 *
 * @author Keith M. Hughes
 */
trait SensorValueProcessor {

  /**
   * Get the value type that the processor handles.
   *
   * @return the value type
   */
  val sensorValueType: String

  /**
   * Process the incoming data.
   *
   * @param timestampMeasurement
   *          the time of the sensor measurement
   * @param timestampMeasurementReceived
   *          the time when the sensor message was received
   * @param sensorChannel
   *          the sensor channel that received the data
   * @param processorContext
   *          the context for processor handling
   * @param channelId
   *          the channel ID being processed
   * @param data
   *          the data to process, should be inside the data field
   */
  def processData(
    timestampMeasurement: Long,
    timestampMeasurementReceived: Long,
    sensorChannel: SensorChannelEntityModel,
    processorContext: SensorValueProcessorContext,
    channelId: String, 
    data: DynamicObject): Unit
}