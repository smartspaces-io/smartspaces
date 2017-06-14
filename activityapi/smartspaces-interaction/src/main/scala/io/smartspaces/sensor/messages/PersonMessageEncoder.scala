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

package io.smartspaces.sensor.messages

import io.smartspaces.messaging.codec.IncrementalMessageEncoder
import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.messaging.dynamic.SmartSpacesMessages
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

/**
 * A message encoder from a person to a web representation.
 *
 * @author Keith M. Hughes
 */
class PersonMessageEncoder(private val builder: DynamicObjectBuilder,
    private val personEncoder: IncrementalMessageEncoder[PersonSensedEntityModel, DynamicObjectBuilder], private val messageType: String) extends MessageEncoder[PersonSensedEntityModel, DynamicObjectBuilder] {

  def this(messageType: String) = {
    this(new StandardDynamicObjectBuilder(), StandardPersonIncrementalMessageEncoder, messageType)

    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, messageType)
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT, SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS)
    builder.newObject(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA)
  }

  override def encode(model: PersonSensedEntityModel): DynamicObjectBuilder = {
    personEncoder.encode(model, builder)

    builder
  }
} 