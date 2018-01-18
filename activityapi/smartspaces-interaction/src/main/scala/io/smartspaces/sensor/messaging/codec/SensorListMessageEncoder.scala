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
import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.messaging.dynamic.SmartSpacesMessages
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

/**
 * A message encoder from a sensor list to a web representation.
 *
 * @author Keith M. Hughes
 */
class SensorListMessageEncoder(private val builder: DynamicObjectBuilder, 
    private val singleSensorEncoder: IncrementalMessageEncoder[SensorEntityModel, DynamicObjectBuilder], private val messageType: String) 
    extends MessageEncoder[Iterable[SensorEntityModel], DynamicObjectBuilder] {

  def this(messageType: String) = {
    this(new StandardDynamicObjectBuilder(), StandardSensorIncrementalMessageEncoder, messageType)

    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, messageType)
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT, SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS)
    builder.newArray(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA)
  }
  
  override def encode(models: Iterable[SensorEntityModel]): DynamicObjectBuilder = {
    models.foreach { model =>
      builder.newObject()
      
      singleSensorEncoder.encode(model, builder)

      builder.up()
    }
      
    builder
  }
}
