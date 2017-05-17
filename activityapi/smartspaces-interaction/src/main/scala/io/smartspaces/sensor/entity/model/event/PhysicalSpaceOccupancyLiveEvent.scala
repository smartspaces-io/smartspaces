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

package io.smartspaces.sensor.entity.model.event

import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel

/**
 * A live event that signals occupancy changes in a physical location.
 * 
 * <p>
 * Live events are events coming from a sensor.
 *
 * @author Keith M. Hughes
 */
object PhysicalSpaceOccupancyLiveEvent {

  /**
   * The type of the event.
   */
  val EVENT_TYPE = "location.occupancy"
}

/**
 * A live event that signals occupancy changes in a physical location.
 * 
 * <p>
 * Live events are events coming from a sensor.
 *
 * @author Keith M. Hughes
 */
class PhysicalSpaceOccupancyLiveEvent(val physicalSpace: PhysicalSpaceSensedEntityModel,
    val entered: Set[PersonSensedEntityModel], val exited: Set[PersonSensedEntityModel], val timestamp: Long) {
}
