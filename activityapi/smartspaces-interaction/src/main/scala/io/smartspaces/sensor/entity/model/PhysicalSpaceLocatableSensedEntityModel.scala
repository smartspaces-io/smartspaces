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

/**
 * A sensed entity model that can be located in a physical space.
 * 
 * @author Keith M. hughes
 */
trait PhysicalSpaceLocatableSensedEntityModel extends SensedEntityModel {

  /**
   * The current physical location of the entity.
   */
  var physicalSpaceLocation: PhysicalSpaceSensedEntityModel
  
  /**
   * Timestamp of the last time the entity changed its location.
   */
  var physicalSpaceLocationTimestamp: Long
}
