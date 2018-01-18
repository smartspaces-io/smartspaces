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

package io.smartspaces.sensor.model.updater

import io.smartspaces.sensor.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.model.PersonSensedEntityModel

/**
 * A model updater for a change from one location to another.
 *
 * @author Keith M. Hughes
 */
class SimpleLocationChangeModelUpdater extends LocationChangeModelUpdater {

  override def updateLocation(newLocation: PhysicalSpaceSensedEntityModel, person: PersonSensedEntityModel,
      measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long): Unit = {
    val oldLocation = person.physicalSpaceLocation
    if (oldLocation != null) {
      if (oldLocation != newLocation) {
        oldLocation.occupantExited(person, measurementTimestamp, sensorMessageReceivedTimestamp)
        newLocation.occupantEntered(person, measurementTimestamp, sensorMessageReceivedTimestamp)
      }
    } else {
      newLocation.occupantEntered(person, measurementTimestamp, sensorMessageReceivedTimestamp)
    }
  }
}