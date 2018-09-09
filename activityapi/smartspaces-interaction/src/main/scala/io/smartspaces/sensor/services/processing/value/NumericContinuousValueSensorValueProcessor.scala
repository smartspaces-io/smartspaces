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

import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.model.SensedEntityModel
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.model.SimpleNumericContinuousSensedValue
import io.smartspaces.sensor.event.RawSensorLiveEvent
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sensor.model.SensorChannelEntityModel

/**
 * A processor for sensor value data messages with continuous values.
 *
 * @author Keith M. Hughes
 */
class NumericContinuousValueSensorValueProcessor(
    val measurementType: MeasurementTypeDescription) extends SensorValueProcessor {
  
  override val sensorValueType = measurementType.externalId
  
  override def processData(timestampMeasurement: Long, timestampMeasurementReceived: Long, 
      sensorChannel: SensorChannelEntityModel, 
      processorContext: SensorValueProcessorContext,
      channelId: String, data: DynamicObject): Unit = {
    val value =
      new SimpleNumericContinuousSensedValue(sensorChannel,
        data.getDouble(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE), 
        timestampMeasurement, timestampMeasurementReceived)

    sensorChannel.updateSensedValue(value, timestampMeasurement)
    
    processorContext.completeSensedEntityModel.eventEmitter.broadcastRawSensorEvent(
        new RawSensorLiveEvent(value, sensorChannel, timestampMeasurement, timestampMeasurementReceived))
  }
}
