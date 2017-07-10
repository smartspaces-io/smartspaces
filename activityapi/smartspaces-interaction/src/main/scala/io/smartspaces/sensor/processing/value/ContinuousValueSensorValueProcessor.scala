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

package io.smartspaces.sensor.processing.value

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.MeasurementTypeDescription
import io.smartspaces.sensor.entity.model.SimpleSensedValue
import io.smartspaces.sensor.messages.SensorMessages
import io.smartspaces.sensor.processing.value.SensorValueProcessor
import io.smartspaces.sensor.processing.SensorValueProcessorContext

/**
 * A processor for sensor value data messages with continuous values.
 *
 * @author Keith M. Hughes
 */
class ContinuousValueSensorValueProcessor(val measurementType: MeasurementTypeDescription) extends SensorValueProcessor {
  
  override val sensorValueType = measurementType.externalId
  
  override def processData(timestamp: Long, sensorEntity: SensorEntityModel,
    sensedEntity: SensedEntityModel, processorContext: SensorValueProcessorContext,
    data: DynamicObject): Unit = {
    val value =
      new SimpleSensedValue[Double](sensorEntity, measurementType,
        data.getDouble(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE), timestamp)

    processorContext.log.info(value)

    sensedEntity.updateSensedValue(value, timestamp)
    sensorEntity.updateSensedValue(value, timestamp)
  }
}