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

package io.smartspaces.master.event;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.spacecontroller.SpaceControllerState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;

/**
 * A helper for messages to {@link MasterEventListener} instances.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterEventManager implements MasterEventManager {

  /**
   * Listeners registered with helper.
   */
  private final List<MasterEventListener> listeners =
      new CopyOnWriteArrayList<MasterEventListener>();

  /**
   * The logger for this manager.
   */
  private Log log;

  @Override
  public void addListener(MasterEventListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(MasterEventListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void signalSpaceControllerConnectAttempted(ActiveSpaceController controller) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectAttempted(controller);
      } catch (Throwable e) {
        log.error(
            String
                .format(
                    "Exception while processing space controller connection attempt master event listener: %s",
                    controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerConnectFailed(ActiveSpaceController controller, long waitedTime) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectFailed(controller, waitedTime);
      } catch (Throwable e) {
        log.error(
            String
                .format(
                    "Exception while processing space controller connection failure master event listener: %s",
                    controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerDisconnectAttempted(ActiveSpaceController controller) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerDisconnectAttempted(controller);
      } catch (Throwable e) {
        log.error(
            String
                .format(
                    "Exception while processing space controller disconnection attempt master event listener: %s",
                    controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerHeartbeat(ActiveSpaceController controller, long timestamp) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerHeartbeat(controller, timestamp);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing space controller heartbeat master event listener: %s",
            controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerHeartbeatLost(ActiveSpaceController controller,
      long timeSinceLastHeartbeat) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerHeartbeatLost(controller, timeSinceLastHeartbeat);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing space controller heartbeat lost master event listener: %s",
            controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerStatusChange(ActiveSpaceController controller,
      SpaceControllerState state) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerStatusChange(controller, state);
      } catch (Throwable e) {
        log.error(
            String
                .format(
                    "Exception while processing space controller status change master event listener (%s): %s",
                    state, controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalSpaceControllerShutdown(ActiveSpaceController controller) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onSpaceControllerShutdown(controller);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing space controller shutdown master event listener: %s",
            controller.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalLiveActivityDeploy(ActiveLiveActivity liveActivity,
      LiveActivityDeploymentResponse result, long timestamp) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onLiveActivityDeploy(liveActivity, result, timestamp);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing live activity deploy master event listener (%s): %s",
            result, liveActivity.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalLiveActivityDelete(ActiveLiveActivity liveActivity,
      LiveActivityDeleteResponse result) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onLiveActivityDelete(liveActivity, result);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing live activity delete master event listener (%s): %s",
            result, liveActivity.getDisplayName()), e);
      }
    }
  }

  @Override
  public void signalLiveActivityRuntimeStateChange(ActiveLiveActivity liveActivity,
      ActivityState oldState, ActivityState newState) {
    for (MasterEventListener listener : listeners) {
      try {
        listener.onLiveActivityStateChange(liveActivity, oldState, newState);
      } catch (Throwable e) {
        log.error(String.format(
            "Exception while processing live activity deploy master event listener (%s to %s): %s",
            oldState, newState, liveActivity.getDisplayName()), e);
      }
    }
  }

  @Override
  public void removeAllListeners() {
    listeners.clear();
  }

  /**
   * Set the logger for this manager.
   *
   * @param log
   *          the logger
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
