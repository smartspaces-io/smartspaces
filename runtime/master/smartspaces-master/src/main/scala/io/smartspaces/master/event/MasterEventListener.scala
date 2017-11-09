/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

import io.smartspaces.activity.ActivityState
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse
import io.smartspaces.master.server.services.model.ActiveLiveActivity
import io.smartspaces.master.server.services.model.ActiveSpaceController
import io.smartspaces.spacecontroller.SpaceControllerState

/**
 * A listener for events within the master.
 *
 * @author Keith M. Hughes
 */
trait MasterEventListener {

  /**
   * A space controller connection is being attempted.
   *
   * @param controller
   *          the controller
   */
  def onSpaceControllerConnectAttempted(controller: ActiveSpaceController): Unit

  /**
   * A space controller connection failed.
   *
   * @param controller
   *          the controller
   * @param waitedTime
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  def onSpaceControllerConnectFailed(controller: ActiveSpaceController, waitedTime: Long): Unit

  /**
   * A space controller disconnection is being attempted.
   *
   * @param controller
   *          the controller
   */
  def onSpaceControllerDisconnectAttempted(controller: ActiveSpaceController): Unit

  /**
   * A controller has sent a heartbeat.
   *
   * @param controller
   *          the controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  def onSpaceControllerHeartbeat(controller: ActiveSpaceController, timestamp: Long): Unit

  /**
   * The master has lost the heartbeat from a controller.
   *
   * @param controller
   *          the controller
   * @param timeSinceLastHeartbeat
   *          the time since the last heartbeat that triggered the error, in
   *          milliseconds
   */
  def onSpaceControllerHeartbeatLost(controller: ActiveSpaceController, timeSinceLastHeartbeat: Long): Unit

  /**
   * The controller status has been updated.
   *
   * @param controller
   *          the space controller
   * @param state
   *          the new state
   */
  def onSpaceControllerStatusChange(controller: ActiveSpaceController, state: SpaceControllerState): Unit

  /**
   * The space controller is shutting down.
   *
   * @param controller
   *          the space controller
   */
  def onSpaceControllerShutdown(controller: ActiveSpaceController): Unit

  /**
   * A live activity has been deployed.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the installation attempt
   * @param timestamp
   *          timestamp of the event
   */
  def onLiveActivityDeploy(liveActivity: ActiveLiveActivity, result: LiveActivityDeploymentResponse,
    timestamp: Long): Unit

  /**
   * A live activity has been deleted.
   *
   * @param liveActivity
   *          the activity
   * @param result
   *          result from the deletion attempt
   */
  def onLiveActivityDelete(liveActivity: ActiveLiveActivity, result: LiveActivityDeleteResponse): Unit

  /**
   * A live activity has had a state change.
   *
   * @param liveActivity
   *          the live activity
   * @param oldState
   *          the old state the activity had
   * @param newState
   *          the new state
   */
  def onLiveActivityStateChange(liveActivity: ActiveLiveActivity, oldState: ActivityState,
    newState: ActivityState): Unit
}
