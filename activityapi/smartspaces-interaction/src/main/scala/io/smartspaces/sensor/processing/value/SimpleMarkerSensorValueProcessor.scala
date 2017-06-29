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

import io.smartspaces.sensor.messages.StandardSensorData
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.updater.SimpleLocationChangeModelUpdater
import io.smartspaces.sensor.messages.SensorMessages
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sensor.entity.model.updater.LocationChangeModelUpdater
import io.smartspaces.sensor.processing.value.SensorValueProcessor
import io.smartspaces.sensor.processing.SensorValueProcessorContext
import io.smartspaces.sensor.processing.UnknownMarkerHandler

/**
 * The standard processor for sensors that give a simple marker ID.
 *
 * @author Keith M. Hughes
 */
class SimpleMarkerSensorValueProcessor(unknownMarkerHandler: UnknownMarkerHandler, val modelUpdater: LocationChangeModelUpdater) extends SensorValueProcessor {
  
  def this(unknownMarkerHandler: UnknownMarkerHandler) = {
    this(unknownMarkerHandler, new SimpleLocationChangeModelUpdater)
  }

  /**
   * The type of the sensor value for this processor.
   */
  override val sensorValueType = StandardSensorData.SENSOR_TYPE_MARKER_SIMPLE

  override def processData(timestamp: Long, sensorModel: SensorEntityModel,
    sensedEntityModel: SensedEntityModel, processorContext: SensorValueProcessorContext,
    data: DynamicObject): Unit = {
    val markerId = data.getRequiredString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE)

    val markerEntity = processorContext.completeSensedEntityModel.sensorRegistry.getMarkerEntityByMarkerId(markerId)
    if (markerEntity.isEmpty) {
      processorContext.log.warn(s"Detected unknown marker ID ${markerId}")
      unknownMarkerHandler.handleUnknownMarker(markerId, timestamp)

      return
    }

    val personOption =
      processorContext.completeSensedEntityModel.getMarkedSensedEntityModelByMarkerId(markerId)
    if (personOption.isEmpty) {
      processorContext.log.warn(s"No person associated with marker ${markerId}")

      return
    }
      
    val person = personOption.get.asInstanceOf[PersonSensedEntityModel]
    val newLocation = sensedEntityModel.asInstanceOf[PhysicalSpaceSensedEntityModel]

    sensorModel.updateSensedValue(timestamp)

    processorContext.log.info(s"Detected marker ID ${markerId}, person ${person.sensedEntityDescription.externalId} entering ${newLocation}")

    modelUpdater.updateLocation(newLocation, person, timestamp)
  }
}