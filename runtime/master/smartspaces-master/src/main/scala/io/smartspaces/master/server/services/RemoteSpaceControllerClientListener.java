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
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResult;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult;
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
   * @param spaceController
   *          the controller being connected to
   */
  void onSpaceControllerConnectAttempted(ActiveSpaceController spaceController);

  /**
   * A space controller connection has been made.
   *
   * @param spaceController
   *          the controller being connected to
   * @param timestamp
   *          time when connection was lost
   */
  void onSpaceControllerConnect(ActiveSpaceController spaceController, long timestamp);

  /**
   * A space controller connection attempt has failed.
   *
   * @param spaceController
   *          the controller being connected to
   * @param timeToWait
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  void onSpaceControllerConnectFailed(ActiveSpaceController spaceController, long timeToWait);

  /**
   * A space controller disconnection is being attempted.
   *
   * @param spaceController
   *          the controller being disconnected from
   */
  void onSpaceControllerDisconnectAttempted(ActiveSpaceController spaceController);

  /**
   * A space controller connection has been lost.
   *
   * @param spaceController
   *          the space controller that disconnected
   * @param timestamp
   *          time when connection was lost
   */
  void onSpaceControllerDisconnect(ActiveSpaceController spaceController, long timestamp);

  /**
   * A controller has sent a heartbeat.
   *
   * @param spaceController
   *          the space controller
   *
   * @param timestamp
   *          timestamp of the heartbeat
   */
  void onSpaceControllerHeartbeat(ActiveSpaceController spaceController, long timestamp);

  /**
   * The controller status has been updated.
   *
   * @param spaceController
   *          the space controller
   * @param state
   *          the new state
   */
  void onSpaceControllerStatusChange(ActiveSpaceController spaceController, SpaceControllerState state);

  /**
   * The space controller has signaled a shutdown..
   *
   * @param uuid
   *          the UUID of the space controller
   */
  void onSpaceControllerShutdown(ActiveSpaceController spaceController);

  /**
   * An activity has been deployed.
   *
   * @param liveActivityUuid
   *          uuid of the activity
   * @param result
   *          result of the install
   */
  void onLiveActivityDeployment(String liveActivityUuid, LiveActivityDeploymentResult result);

  /**
   * An activity has been deleted.
   *
   * @param liveActivityUuid
   *          uuid of the activity
   * @param result
   *          result of the delete
   */
  void onLiveActivityDelete(String liveActivityUuid, LiveActivityDeleteResult result);

  /**
   * A remote activity has deployment status.
   *
   * @param liveActivityUuid
   *          UUID of the activity
   * @param runtimeState
   *          runtime state change status of the remote activity
   * @param detail
   *          detail associated with activity status change
   */
  void onLiveActivityRuntimeStateChange(String liveActivityUuid, ActivityState runtimeState, String detail);

  /**
   * Data bundle control has a status update.
   *
   * @param spaceController
   *          the space controller
   * @param state
   *          state change status of the controller data bundle transfer
   */
  void onDataBundleStateChange(ActiveSpaceController spaceController, DataBundleState state);
}
