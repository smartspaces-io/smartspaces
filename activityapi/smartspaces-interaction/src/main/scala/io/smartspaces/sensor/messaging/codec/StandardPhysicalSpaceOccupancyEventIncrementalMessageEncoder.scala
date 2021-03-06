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
import io.smartspaces.sensor.event.PhysicalSpaceOccupancyEvent
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder

/**
 * An incremental message encoder for physical space occupancy event messages.
 * 
 * This encoder handles a single event.
 * 
 * @author Keith M. Hughes
 */
object StandardPhysicalSpaceOccupancyEventIncrementalMessageEncoder extends IncrementalMessageEncoder[PhysicalSpaceOccupancyEvent, DynamicObjectBuilder] {
  
  override def encode(model: PhysicalSpaceOccupancyEvent, builder: DynamicObjectBuilder): Unit = {
    builder.setProperty("physicalSpaceExternalId", model.physicalSpaceExternalId)
    builder.setProperty("personExternalId", model.personExternalId)
    builder.setProperty("timestamp", model.timestamp)
    builder.setProperty("presence", model.presence.label)
  }
}
