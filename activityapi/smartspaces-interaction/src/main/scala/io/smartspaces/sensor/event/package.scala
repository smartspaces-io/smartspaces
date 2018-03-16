/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.sensor

import io.smartspaces.sensor.event.PersistedRawSensorEvent
import io.smartspaces.sensor.value.entity.ActiveCategoricalValueInstances
import io.smartspaces.sensor.value.entity.ContactCategoricalValueInstances
import io.smartspaces.sensor.value.entity.MoistureCategoricalValueInstances
import io.smartspaces.sensor.value.entity.PresenceCategoricalValueInstances
import io.smartspaces.sensor.value.entity.OnlineCategoricalValueInstances

/**
 * A collection of types and entities for the event package.
 * 
 * @author Keith M. Hughes
 */
package object event {
  
  /**
   * Alias for the presence categorical value.
   */
  type PresenceCategoricalValue = PresenceCategoricalValueInstances.PresenceCategoricalValueInstance
  
  /**
   * Alias for persisted raw sensor events giving presence status.
   */
  type PresenceRawSensorEvent = PersistedRawSensorEvent[PresenceCategoricalValue]

  /**
   * Alias for the contact categorical value.
   */
  type ContactCategoricalValue = ContactCategoricalValueInstances.ContactCategoricalValueInstance
  
  /**
   * Alias for persisted raw sensor events giving contact status.
   */
  type ContactRawSensorEvent = PersistedRawSensorEvent[ContactCategoricalValue]

  /**
   * Alias for the active categorical value.
   */
  type ActiveCategoricalValue = ActiveCategoricalValueInstances.ActiveCategoricalValueInstance
  
  /**
   * Alias for persisted raw sensor events giving active status.
   */
  type ActiveRawSensorEvent = PersistedRawSensorEvent[ActiveCategoricalValue]

  /**
   * Alias for the moisture categorical value.
   */
  type MoistureCategoricalValue = MoistureCategoricalValueInstances.MoistureCategoricalValueInstance
  
  /**
   * Alias for persisted raw sensor events giving moisture status.
   */
  type MoistureRawSensorEvent = PersistedRawSensorEvent[MoistureCategoricalValue]

  /**
   * Alias for the online categorical value.
   */
  type OnlineCategoricalValue = OnlineCategoricalValueInstances.OnlineCategoricalValueInstance
  
  /**
   * Alias for persisted raw sensor events giving online status.
   */
  type OnlineRawSensorEvent = PersistedRawSensorEvent[OnlineCategoricalValue]
}