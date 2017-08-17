/*
 * Copyright (C) 2017 Keith M. Hughes
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
import io.smartspaces.sensor.entity.model.updater.SimpleLocationChangeModelUpdater
import io.smartspaces.sensor.entity.model.updater.LocationChangeModelUpdater
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.processing.UnknownMarkerHandler
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.messaging.messages.StandardSensorData
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.value.entity.PresenceCategoricalValue
import io.smartspaces.sensor.value.entity.PresenceCategoricalValueInstances
import io.smartspaces.sensor.entity.model.event.RawSensorLiveEvent
import io.smartspaces.sensor.entity.model.SimpleCategoricalValueSensedValue
import io.smartspaces.sensor.entity.MeasurementTypeDescription

/**
 * The standard processor for sensors that give a stateful marker ID.
 *
 * @author Keith M. Hughes
 */
class StatefulMarkerSensorSensorValueProcessor(
    val statefulMarkerMeasurementType: MeasurementTypeDescription,
    unknownMarkerHandler: UnknownMarkerHandler, 
    val modelUpdater: LocationChangeModelUpdater) extends SensorValueProcessor {

  def this(statefulMarkerMeasurementType: MeasurementTypeDescription, unknownMarkerHandler: UnknownMarkerHandler) = {
    this(statefulMarkerMeasurementType, unknownMarkerHandler, new SimpleLocationChangeModelUpdater)
  }

  /**
   * The type of the sensor value for this processor.
   */
  override val sensorValueType = StandardSensorData.MEASUREMENT_TYPE_MARKER_STATEFUL

  override def processData(timestamp: Long, sensorEntity: SensorEntityModel,
    sensedEntityModel: SensedEntityModel, processorContext: SensorValueProcessorContext,
    channelId: String, data: DynamicObject): Unit = {
    val presenceLabel = data.getRequiredString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE)
    val presence = PresenceCategoricalValue.fromLabel(presenceLabel)
    if (presence.isEmpty) {
      processorContext.log.warn(s"Detected unknown presence label ${presenceLabel}")

      return
    }

    val markerId = data.getRequiredString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION)

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
    val sensedLocation = sensedEntityModel.asInstanceOf[PhysicalSpaceSensedEntityModel]

    sensorEntity.updateSensedValue(timestamp)

    presence.get match {
      case PresenceCategoricalValueInstances.PRESENT =>
        processorContext.log.info(s"Detected marker ID ${markerId}, person ${person.sensedEntityDescription.externalId} entering ${sensedLocation.sensedEntityDescription.externalId}")
      case PresenceCategoricalValueInstances.NOT_PRESENT =>
        processorContext.log.info(s"Detected marker ID ${markerId}, person ${person.sensedEntityDescription.externalId} exiting ${sensedLocation.sensedEntityDescription.externalId}")
    }

    //modelUpdater.updateLocation(newLocation, person, timestamp)
    
    val value = new SimpleCategoricalValueSensedValue(
        sensorEntity, Option(channelId), statefulMarkerMeasurementType, presence.get, Some(markerId), timestamp)
    processorContext.completeSensedEntityModel.eventEmitter.broadcastRawSensorEvent(new RawSensorLiveEvent(value, sensedEntityModel))
  }
}
