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

package io.smartspaces.sensor.event

import io.smartspaces.sensor.value.entity.PresenceCategoricalValueInstances.PresenceCategoricalValueInstance

/**
 * An event that a person entered or exited a physical space.
 *
 * @author Keith M. Hughes
 */
class PhysicalSpaceOccupancyEvent(
    val physicalSpaceExternalId: String,
    val personExternalId: String, 
    val timestamp: Long, 
    val presence: PresenceCategoricalValueInstance) {
  override def toString(): String = {
    s"PhysicalSpaceOccupancyEvent[ physicalSpaceExternalId=${physicalSpaceExternalId}, personExternalId=${personExternalId}, timestamp=${timestamp}, presence=${presence.label}]"
  }
}