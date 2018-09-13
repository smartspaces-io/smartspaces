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

package io.smartspaces.sensor.messaging.messages

/**
 * Constants and functions for working with sensor messages.
 * 
 * See [[io.smartspaces.messaging.dynamic.SmartSpacesMessages]] for the message envelopes.
 *
 * @author Keith M. Hughes
 */
object SensorSmartSpacesMessages {

  /**
   * The common message field for the entity ID.
   */
  val MESSAGE_ENTITY_COMMON_ID = "id"

  /**
   * The common message field for the entity external ID.
   */
  val MESSAGE_ENTITY_COMMON_EXTERNAL_ID = "externalId"

  /**
   * The common message field for the entity display name.
   */
  val MESSAGE_ENTITY_COMMON_DISPLAY_NAME = "displayName"

  /**
   * The common message field for the entity display description.
   */
  val MESSAGE_ENTITY_COMMON_DISPLAY_DESCRIPTION = "displayDescription"

  /**
   * The sensor message field for an active sensor.
   */
  val MESSAGE_ENTITY_SENSOR_ACTIVE = "active"

  /**
   * The sensor message field for an online sensor.
   */
  val MESSAGE_ENTITY_SENSOR_ONLINE = "online"

  /**
   * The sensor message field for the last heartbeat.
   */
  val MESSAGE_ENTITY_SENSOR_HEARTBEAT_UPDATE_LAST = "heartbeatUpdateLast"

  /**
   * The sensor message field for the last update.
   */
  val MESSAGE_ENTITY_SENSOR_STATE_UPDATE_LAST = "stateUpdateLast"

  /**
   * The sensor message field for the location.
   */
  val MESSAGE_ENTITY_SENSOR_LOCATION = "location"
}