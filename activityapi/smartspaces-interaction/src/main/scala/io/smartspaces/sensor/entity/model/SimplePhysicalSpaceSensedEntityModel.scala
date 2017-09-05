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

package io.smartspaces.sensor.entity.model

import scala.collection.mutable.HashSet
import scala.collection.mutable.Set

import io.smartspaces.sensor.entity.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.entity.model.event.PhysicalSpaceOccupancyLiveEvent

/**
 * A model of a physical space.
 *
 * @author Keith M. Hughes
 */
class SimplePhysicalSpaceSensedEntityModel(
  entityDescription: PhysicalSpaceSensedEntityDescription, models: CompleteSensedEntityModel) extends SimpleSensedEntityModel(entityDescription, models)
    with PhysicalSpaceSensedEntityModel {

  /**
   * The occupants in the space.
   */
  private val occupants: Set[PersonSensedEntityModel] = new HashSet

  override def occupantEntered(person: PersonSensedEntityModel, 
      measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long): PhysicalSpaceSensedEntityModel = {

    val hasBeenAdded = occupants.add(person)

    if (hasBeenAdded) {
      person.physicalSpaceLocation = this
      person.physicalSpaceLocationTimestamp = measurementTimestamp

      val entered = scala.collection.immutable.HashSet(person)

      models.eventEmitter.broadcastOccupancyEvent(
          PhysicalSpaceOccupancyLiveEvent.newEnteredOnlyEvent(this, entered, measurementTimestamp, sensorMessageReceivedTimestamp))
    }

    this
  }

  override def occupantExited(person: PersonSensedEntityModel, 
      measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long): PhysicalSpaceSensedEntityModel = {
    val wasHere = occupants.remove(person)

    if (wasHere) {
      person.physicalSpaceLocation = null
      person.physicalSpaceLocationTimestamp = measurementTimestamp

      val exited = scala.collection.immutable.HashSet(person)

      models.eventEmitter.broadcastOccupancyEvent(
          PhysicalSpaceOccupancyLiveEvent.newExitedOnlyEvent(this, exited, measurementTimestamp, sensorMessageReceivedTimestamp))
    }

    this
  }

  override def getOccupants(): scala.collection.immutable.Set[PersonSensedEntityModel] = {
    occupants.toSet
  }
}
