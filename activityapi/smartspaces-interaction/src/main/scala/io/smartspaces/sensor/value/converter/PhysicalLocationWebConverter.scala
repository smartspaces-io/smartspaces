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

package io.smartspaces.sensor.value.converter

import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder

/**
 * A converter from a physical location list to a web representation.
 * 
 * @author Keith M. Hughes
 */
class PhysicalLocationWebConverter(private val builder: DynamicObjectBuilder) extends ObjectConverter[List[PhysicalSpaceSensedEntityModel], DynamicObjectBuilder] {

  def this() = {
    this(new StandardDynamicObjectBuilder())

    builder.setProperty("type", "update.physicallocations")
    builder.newArray("data")
  }

  override def convert(value: List[PhysicalSpaceSensedEntityModel]): DynamicObjectBuilder = {
    value.foreach { model =>
      builder.newObject()

      builder.setProperty("id", model.sensedEntityDescription.id)
      builder.setProperty("externalId", model.sensedEntityDescription.externalId)
      builder.setProperty("displayName", model.sensedEntityDescription.displayName)
      builder.setProperty("displayDescription", model.sensedEntityDescription.displayDescription)

      builder.up()
    }

    builder
  }
} 