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
 * A support implementation of {@link MasterEventListener} which provides empty
 * methods for all events.
 *
 * @author Keith M. Hughes
 */
abstract class BaseMasterEventListener extends MasterEventListener {

  override def onSpaceControllerConnectAttempted(controller: ActiveSpaceController ): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerConnectFailed(controller: ActiveSpaceController, waitedTime: Long): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerDisconnectAttempted(controller: ActiveSpaceController): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerHeartbeat(controller: ActiveSpaceController, timestamp: Long): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerHeartbeatLost(controller: ActiveSpaceController,
      timeSinceLastHeartbeat: Long): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerStatusChange(controller: ActiveSpaceController,
      state: SpaceControllerState ): Unit = {
    // Default is do nothing.
  }

  override def onSpaceControllerShutdown(controller: ActiveSpaceController): Unit = {
    // Default is do nothing.
  }

  override def onLiveActivityDeploy(liveActivity: ActiveLiveActivity,
      result: LiveActivityDeploymentResponse , timestamp: Long): Unit = {
    // Default is do nothing.
  }

  override def onLiveActivityDelete(liveActivity: ActiveLiveActivity, result: LiveActivityDeleteResponse): Unit = {
    // Default is do nothing.
  }

  override def onLiveActivityStateChange(liveActivity: ActiveLiveActivity,  oldState: ActivityState,
      newState: ActivityState ): Unit = {
    // Default is do nothing.
  }
}
