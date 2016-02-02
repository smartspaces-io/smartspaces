/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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
import io.smartspaces.spacecontroller.SpaceControllerState;

/**
 * Base class for {@link RemoteSpaceControllerClientListener} subclasses that
 * implements every method with a no-op.
 *
 * @author Keith M. Hughes
 */
public class BaseRemoteSpaceControllerClientListener implements RemoteSpaceControllerClientListener {

  @Override
  public void onSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing
  }

  @Override
  public void onSpaceControllerConnectFailed(ActiveSpaceController controller, long waitedTime) {
    // Default is do nothing
  }

  @Override
  public void onSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    // Default is do nothing
  }

  @Override
  public void onSpaceControllerHeartbeat(String uuid, long timestamp) {
    // Default is do nothing
  }

  @Override
  public void onSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    // Default is do nothing
  }

  @Override
  public void onSpaceControllerShutdown(String uuid) {
    // Default is do nothing.
  }

  @Override
  public void onLiveActivityDeployment(String uuid, LiveActivityDeploymentResponse result) {
    // Default is do nothing
  }

  @Override
  public void onLiveActivityDelete(String uuid, LiveActivityDeleteResponse result) {
    // Default is do nothing
  }

  @Override
  public void onLiveActivityRuntimeStateChange(String uuid, ActivityState state, String detail) {
    // Default is do nothing
  }

  @Override
  public void onDataBundleStateChange(String uuid, DataBundleState state) {
    // Default is do nothing
  }
}
