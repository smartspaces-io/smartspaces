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

package io.smartspaces.master.event

/**
 * A base alert for space controller messages
 *
 * @author Keith M. Hughes
 */
abstract class SpaceControllerAlertEvent(
  val timeSinceLastHeartbeat: Long,
  val controllerId: String,
  val controllerUuid: String,
  val controllerName: String,
  val controllerHostId: String) {

}

/**
 * An alert event for losing a space controller connection.
 *
 * @author Keith M. Hughes
 */
object SpaceControllerConnectionLostAlertEvent {

  /**
   * The type of the alert for a lost space controller connection.
   */
  val EVENT_TYPE = "alert.spacecontroller.connection.lost"
}

/**
 * An alert event for losing a space controller connection.
 *
 * @author Keith M. Hughes
 */
class SpaceControllerConnectionLostAlertEvent(
  timeSinceLastHeartbeat: Long,
  controllerId: String,
  controllerUuid: String,
  controllerName: String,
  controllerHostId: String) extends SpaceControllerAlertEvent(timeSinceLastHeartbeat, 
      controllerId, controllerUuid, controllerName, controllerHostId) {

}


/**
 * An alert event for failing to make a space controller connection.
 *
 * @author Keith M. Hughes
 */
object SpaceControllerConnectionFailureAlertEvent {

  /**
   * The type of the alert for a failed space controller connection.
   */
  val EVENT_TYPE = "alert.spacecontroller.connection.failure"
}

/**
 * An alert event for failing to make a space controller connection.
 *
 * @author Keith M. Hughes
 */
class SpaceControllerConnectionFailureAlertEvent(
  timeSinceLastHeartbeat: Long,
  controllerId: String,
  controllerUuid: String,
  controllerName: String,
  controllerHostId: String) extends SpaceControllerAlertEvent(timeSinceLastHeartbeat, 
      controllerId, controllerUuid, controllerName, controllerHostId) {

}
