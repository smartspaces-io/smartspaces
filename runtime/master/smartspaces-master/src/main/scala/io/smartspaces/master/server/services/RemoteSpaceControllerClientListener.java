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

package io.smartspaces.master.server.services;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.master.server.services.internal.DataBundleState;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.spacecontroller.SpaceControllerState;

/**
 * A listener for changes in activity states.
 *
 * @author Keith M. Hughes
 */
public interface RemoteSpaceControllerClientListener {

  /**
   * A space controller connection is being attempted.
   *
   * @param controller
   *          the controller being connected to
   */
  void onSpaceControllerConnectAttempted(ActiveSpaceController controller);

  /**
   * A space controller connection has failed.
   *
   * @param controller
   *          the controller being connected to
   * @param timeToWait
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  void onSpaceControllerConnectFailed(ActiveSpaceController controller, long timeToWait);

  /**
   * A space controller disconnection is being attempted.
   *
   * @param controller
   *          the controller being disconnected from
   */
  void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller);

  /**
   * A controller has sent a heartbeat.
   *
   * @param uuid
   *          uuid of the controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  void onSpaceControllerHeartbeat(String uuid, long timestamp);

  /**
   * The controller status has been updated.
   *
   * @param uuid
   *          the UUID of the space controller
   * @param state
   *          the new state
   */
  void onSpaceControllerStatusChange(String uuid, SpaceControllerState state);

  /**
   * The space controller has signaled a shutdown..
   *
   * @param uuid
   *          the UUID of the space controller
   */
  void onSpaceControllerShutdown(String uuid);

  /**
   * An activity has been deployed.
   *
   * @param uuid
   *          uuid of the activity
   * @param result
   *          result of the install
   */
  void onLiveActivityDeployment(String uuid, LiveActivityDeploymentResponse result);

  /**
   * An activity has been deleted.
   *
   * @param uuid
   *          uuid of the activity
   * @param result
   *          result of the delete
   */
  void onLiveActivityDelete(String uuid, LiveActivityDeleteResponse result);

  /**
   * A remote activity has deployment status.
   *
   * @param uuid
   *          UUID of the activity
   * @param runtimeState
   *          runtime state change status of the remote activity
   * @param detail
   *          detail associated with activity status change
   */
  void onLiveActivityRuntimeStateChange(String uuid, ActivityState runtimeState, String detail);

  /**
   * Data bundle control has a status update.
   *
   * @param uuid
   *          UUID of the controller
   * @param state
   *          state change status of the controller data bundle transfer
   */
  void onDataBundleStateChange(String uuid, DataBundleState state);
}
