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

package io.smartspaces.sensor.messaging.codec

import io.smartspaces.messaging.codec.IncrementalMessageEncoder
import io.smartspaces.sensor.messaging.messages.SensorSmartSpacesMessages
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder

/**
 * An incremental message encoder for sensor messages.
 * 
 * This encoder handles a single sensor.
 * 
 * @author Keith M. Hughes
 */
object StandardSensorIncrementalMessageEncoder extends IncrementalMessageEncoder[SensorEntityModel, DynamicObjectBuilder] {
  
  override def encode(model: SensorEntityModel, builder: DynamicObjectBuilder): Unit = {
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_ID, model.sensorEntityDescription.id)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_EXTERNAL_ID, model.sensorEntityDescription.externalId)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_NAME, model.sensorEntityDescription.displayName)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_DESCRIPTION, model.sensorEntityDescription.displayDescription)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_SENSOR_ACTIVE, model.sensorEntityDescription.active)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_SENSOR_ONLINE, model.online)

    model.lastHeartbeatUpdate().foreach { (date) =>
      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_SENSOR_HEARTBEAT_LAST, date)
    }

    model.lastUpdateTime().foreach { (date) =>
      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_SENSOR_UPDATE_LAST, date)
    }

    model.sensedEntityModel.foreach { (sensedEntity) =>
      builder.newObject(SensorSmartSpacesMessages.MESSAGE_ENTITY_SENSOR_LOCATION)

      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_ID, sensedEntity.sensedEntityDescription.id)
      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_EXTERNAL_ID, sensedEntity.sensedEntityDescription.externalId)
      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_NAME, sensedEntity.sensedEntityDescription.displayName)
      builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_DESCRIPTION, sensedEntity.sensedEntityDescription.displayDescription)

      builder.up()
    }
  }
}
