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
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.messaging.messages.SensorSmartSpacesMessages
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder

/**
 * An incremental message encoder for person messages.
 * 
 * This encoder handles a single person.
 * 
 * @author Keith M. Hughes
 */
object StandardPersonIncrementalMessageEncoder extends IncrementalMessageEncoder[PersonSensedEntityModel, DynamicObjectBuilder] {
  override def encode(model: PersonSensedEntityModel, builder: DynamicObjectBuilder): Unit = {
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_ID, model.sensedEntityDescription.id)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_EXTERNAL_ID, model.sensedEntityDescription.externalId)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_NAME, model.sensedEntityDescription.displayName)
    builder.setProperty(SensorSmartSpacesMessages.MESSAGE_ENTITY_COMMON_DISPLAY_DESCRIPTION, model.sensedEntityDescription.displayDescription)
  }
}