/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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
import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.spacecontroller.SpaceControllerState

/**
 * The manager for master events.
 *
 * @author Keith M. Hughes
 */
trait MasterEventManager extends ManagedResource {

  /**
   * Add in a new event listener.
   *
   * @param listener
   *          the new listener
   */
  def addListener(listener: MasterEventListener): Unit

  /**
   * Remove an event listener.
   *
   * <p>
   * Does nothing if the listener wasn't registered.
   *
   * @param listener
   *          the listener to remove
   */
  def removeListener(listener: MasterEventListener): Unit

  /**
   * Clear all listeners from the manager.
   */
  def removeAllListeners(): Unit

  /**
   * Send the live activity state change message to all listeners.
   *
   * @param liveActivity
   *          the activity
   * @param oldState
   *          old state of the remote activity
   * @param newState
   *          new state of the remote activity
   */
  def signalLiveActivityRuntimeStateChange(
    liveActivity: ActiveLiveActivity, oldState: ActivityState, newState: ActivityState): Unit

  /**
   * Send the live activity deletion message to all listeners.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the deletion
   */
  def signalLiveActivityDelete(liveActivity: ActiveLiveActivity, result: LiveActivityDeleteResponse): Unit

  /**
   * Send the on deployment message to all listeners.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          result of the install
   * @param timestamp
   *          timestamp of the deployment
   */
  def signalLiveActivityDeploy(
    liveActivity: ActiveLiveActivity,
    result: LiveActivityDeploymentResponse, timestamp: Long): Unit

  /**
   * Signal that the controller status has been updated.
   *
   * @param controller
   *          the space controller
   * @param state
   *          the new state
   */
  def signalSpaceControllerStatusChange(
    controller: ActiveSpaceController,
    state: SpaceControllerState): Unit

  /**
   * Signal that a space controller status is shutting down.
   *
   * @param controller
   *          the space controller
   */
  def signalSpaceControllerShutdown(controller: ActiveSpaceController): Unit

  /**
   * Signal a space controller heartbeat.
   *
   * @param controller
   *          the space controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  def signalSpaceControllerHeartbeat(controller: ActiveSpaceController, timestamp: Long): Unit

  /**
   * Signal that master has lost the heartbeat from a controller.
   *
   * @param controller
   *          the controller
   * @param timeSinceLastHeartbeat
   *          the time since the last heartbeat that triggered the error, in
   *          milliseconds
   */
  def signalSpaceControllerHeartbeatLost(
    controller: ActiveSpaceController,
    timeSinceLastHeartbeat: Long): Unit

  /**
   * Signal a space controller disconnection attempt.
   *
   * @param controller
   *          the space controller
   */
  def signalSpaceControllerDisconnectAttempted(controller: ActiveSpaceController): Unit

  /**
   * Signal a space controller connection attempt.
   *
   * @param controller
   *          the space controller
   */
  def signalSpaceControllerConnectAttempted(controller: ActiveSpaceController): Unit

  /**
   * Signal a space controller connection failure.
   *
   * @param controller
   *          the space controller
   * @param waitedTime
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  def signalSpaceControllerConnectFailed(controller: ActiveSpaceController, waitedTime: Long): Unit

  /**
   * Broadcast a space controller offline alert event.
   *
   * @param event
   *          the event to broadcast
   */
  def broadcastSpaceControllerConnectionLostAlertEvent(event: SpaceControllerConnectionLostAlertEvent): Unit

  /**
   * Broadcast a space controller connection failure alert event.
   *
   * @param event
   *          the event to broadcast
   */
  def broadcastSpaceControllerConnectionFailureAlertEvent(event: SpaceControllerConnectionFailureAlertEvent): Unit
}
