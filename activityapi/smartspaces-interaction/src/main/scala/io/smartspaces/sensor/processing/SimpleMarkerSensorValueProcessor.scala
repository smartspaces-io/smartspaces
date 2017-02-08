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

import io.smartspaces.sensor.messages.StandardSensorData
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.updater.SimpleLocationChangeModelUpdater
import io.smartspaces.sensor.messages.SensorMessages
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * The standard processor for sensors that give a simple marker ID.
 *
 * @author Keith M. Hughes
 */
class SimpleMarkerSensorValueProcessor extends SensorValueProcessor {
  
  private val modelUpdater = new SimpleLocationChangeModelUpdater

  override val sensorValueType = StandardSensorData.SENSOR_TYPE_MARKER_SIMPLE

  override def processData(timestamp: Long, sensorModel: SensorEntityModel,
    sensedEntityModel: SensedEntityModel, processorContext: SensorValueProcessorContext,
    data: DynamicObject) {
    val markerId = data.getRequiredString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE)

    val markerEntity = processorContext.completeSensedEntityModel.sensorRegistry.getMarkerEntityByMarkerId(markerId)
    val person =
      processorContext.completeSensedEntityModel.getMarkedSensedEntityModel(markerId).get.asInstanceOf[PersonSensedEntityModel]
    val newLocation = sensedEntityModel.asInstanceOf[PhysicalSpaceSensedEntityModel]
    
    sensorModel.updateSensedValue(timestamp)

    processorContext.log.formatInfo("Detected marker ID %s,  person %s entering %s\n", markerId,
      person, newLocation);

    modelUpdater.updateLocation(newLocation, person, timestamp)
  }
}