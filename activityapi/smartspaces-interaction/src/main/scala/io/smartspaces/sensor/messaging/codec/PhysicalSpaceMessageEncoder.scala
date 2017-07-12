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

package io.smartspaces.sensor.messaging.codec

import io.smartspaces.messaging.codec.IncrementalMessageEncoder
import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.messaging.dynamic.SmartSpacesMessages
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

/**
 * A message encoder from a physical space to a web representation.
 *
 * @author Keith M. Hughes
 */
class PhysicalSpaceMessageEncoder(private val builder: DynamicObjectBuilder,
    private val physicalSpaceEncoder: IncrementalMessageEncoder[PhysicalSpaceSensedEntityModel, DynamicObjectBuilder], private val messageType: String) extends MessageEncoder[PhysicalSpaceSensedEntityModel, DynamicObjectBuilder] {

  def this(messageType: String) = {
    this(new StandardDynamicObjectBuilder(), StandardPhysicalSpaceIncrementalMessageEncoder, messageType)

    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, messageType)
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT, SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS)
    builder.newObject(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA)
  }

  override def encode(model: PhysicalSpaceSensedEntityModel): DynamicObjectBuilder = {
    physicalSpaceEncoder.encode(model, builder)

    builder
  }
} 