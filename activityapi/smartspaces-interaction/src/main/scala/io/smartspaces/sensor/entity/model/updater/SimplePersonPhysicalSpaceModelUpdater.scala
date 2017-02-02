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

package io.smartspaces.sensor.entity.model.updater

import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel;

/**
 * A model updater for interactions between a person and a physical space.
 * 
 * @author Keith M. Hughes
 */
class SimplePersonPhysicalSpaceModelUpdater(val physicalSpace: PhysicalSpaceSensedEntityModel, val person: PersonSensedEntityModel) {

  /**
   * Have the person enter the space.
   * 
   * @param timestamp
   *        the time when the person entered the space
   */
  def enterSpace(timestamp: Long): Unit = {
    physicalSpace.occupantEntered(person, timestamp)
  }

  /**
   * Have the person exit the space.
   * 
   * @param timestamp
   *        the time when the person exited the space
   */
  def exitSpace(timestamp: Long): Unit = {
    physicalSpace.occupantExited(person, timestamp);
  }
}
