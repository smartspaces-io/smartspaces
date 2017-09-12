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

  /**
   * An immutable set to represent no occupants in an event.
   */
  val NO_OCCUPANTS_SET = scala.collection.immutable.HashSet[PersonSensedEntityModel]()
  
  /**
   * Create a new event for people just entering the physical space.
   * 
   * @param physicalSpace
   *          the model for the physical space
   * @param entered
   *          the occupants that entered
   * @param timestamp
   *          the timestamp of the event
   */
  def newEnteredOnlyEvent(physicalSpace: PhysicalSpaceSensedEntityModel,
      entered: Set[PersonSensedEntityModel], 
      measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long): PhysicalSpaceOccupancyLiveEvent = {
    new PhysicalSpaceOccupancyLiveEvent(physicalSpace, entered, NO_OCCUPANTS_SET, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
  
  /**
   * Create a new event for people just entering the physical space.
   * 
   * @param physicalSpace
   *          the model for the physical space
   * @param exited
   *          the occupants that entered
   * @param timestamp
   *          the timestamp of the event
   */
  def newExitedOnlyEvent(physicalSpace: PhysicalSpaceSensedEntityModel,
      exited: Set[PersonSensedEntityModel], 
      measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long): PhysicalSpaceOccupancyLiveEvent = {
    new PhysicalSpaceOccupancyLiveEvent(physicalSpace, NO_OCCUPANTS_SET, exited, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
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
    val entered: Set[PersonSensedEntityModel], val exited: Set[PersonSensedEntityModel], 
    val measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long) {
}
