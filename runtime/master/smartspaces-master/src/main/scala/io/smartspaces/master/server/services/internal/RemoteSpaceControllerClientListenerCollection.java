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

package io.smartspaces.master.server.services.internal;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResult;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.server.services.RemoteSpaceControllerClientListener;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.spacecontroller.SpaceControllerState;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A collection of {@link RemoteSpaceControllerClientListener} instances.
 *
 * <p>
 * There will be one per remote controller client.
 *
 * @author Keith M. Hughes
 */
public class RemoteSpaceControllerClientListenerCollection {

  /**
   * Listeners registered with helper.
   */
  private final List<RemoteSpaceControllerClientListener> listeners = Lists
      .newCopyOnWriteArrayList();

  /**
   * Logger for this helper.
   */
  private final ExtendedLog log;

  /**
   * Construct a helper.
   *
   * @param log
   *          the logger to use
   */
  public RemoteSpaceControllerClientListenerCollection(ExtendedLog log) {
    this.log = log;
  }

  /**
   * Add in a new event listener.
   *
   * @param listener
   *          the new listener
   */
  public void addListener(RemoteSpaceControllerClientListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove an event listener.
   *
   * <p>
   * Does nothing if the listener wasn't registered.
   *
   * @param listener
   *          the listener to remove
   */
  public void removeListener(RemoteSpaceControllerClientListener listener) {
    listeners.remove(listener);
  }

  /**
   * Signal a space controller connecting.
   *
   * @param spaceController
   *          the controller being disconnected from
   */
  public void signalSpaceControllerConnectAttempt(ActiveSpaceController spaceController) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectAttempted(spaceController);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller connect event for %s",
            spaceController.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller connection failed.
   *
   * @param spaceController
   *          the controller being disconnected from
   * @param timeToWait
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  public void signalSpaceControllerConnectFailed(ActiveSpaceController spaceController, long waitedTime) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectFailed(spaceController, waitedTime);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller connect event for %s",
            spaceController.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller connect.
   *
   * @param spaceController
   *          the controller that connected
   */
  public void signalSpaceControllerConnect(ActiveSpaceController spaceController, long timestamp) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnect(spaceController, timestamp);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller connect event for %s",
            spaceController.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller disconnect attempt.
   *
   * @param spaceController
   *          the controller being disconnected from
   */
  public void signalSpaceControllerDisconnectAttempt(ActiveSpaceController spaceController) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerDisconnectAttempted(spaceController);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller disconnect attempt event for %s",
            spaceController.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller disconnect.
   *
   * @param spaceController
   *          the controller being disconnected from
   * @param timestamp
   *          the time of the disconnect
   */
  public void signalSpaceControllerDisconnect(ActiveSpaceController spaceController, long timestamp) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerDisconnect(spaceController, timestamp);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller disconnect attempt event for %s",
            spaceController.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller heartbeat.
   *
   * @param spaceController
   *          the space controller
   * @param timestamp
   *          timestamp of the heartbeat
   */
  public void signalSpaceControllerHeartbeat(ActiveSpaceController spaceController, long timestamp) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerHeartbeat(spaceController, timestamp);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling space controller heartbeat event for UUID %s and timestamp %d", spaceController.getDisplayName(),
            timestamp);
      }
    }
  }

  /**
   * Signal that the controller status has been updated.
   *
   * @param spaceController
   *          the space controller
   * @param state
   *          the new state
   */
  public void signalSpaceControllerStatusChange(ActiveSpaceController spaceController, SpaceControllerState state) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerStatusChange(spaceController, state);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling space controller status change event for %s and state %s", spaceController.getDisplayName(),
            state);
      }
    }
  }

  /**
   * Signal that the space controller is shutting down.
   *
   * @param spaceController
   *          the space controller
   */
  public void signalSpaceControllerShutdown(ActiveSpaceController spaceController) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerShutdown(spaceController);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller shutdown event for %s", spaceController.getDisplayName());
      }
    }
  }

  /**
   * Send the on deployment message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param result
   *          the result of the deployment
   */
  public void signalActivityDeployStatus(String uuid, LiveActivityDeploymentResult result) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityDeployment(uuid, result);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling space controller deployment status event for UUID %s and result %s",
            uuid, result);
      }
    }
  }

  /**
   * Send the on deletion message to all listeners.
   *
   * @param liveActivityUuid
   *          UUID of the activity.
   * @param result
   *          result of the deletion
   */
  public void signalActivityDelete(String liveActivityUuid, LiveActivityDeleteResult result) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityDelete(liveActivityUuid, result);
      } catch (Throwable e) {
        log.formatError(e, "Error handling live activity delete event for UUID %s and result %s",
            liveActivityUuid, result);
      }
    }
  }

  /**
   * Send the activity state change message to all listeners.
   *
   * @param liveActivityUuid
   *          UUID of the activity
   * @param newRuntimeState
   *          runtime status of the remote activity
   * @param newRuntimeStateDetail
   *          detail about the new runtime state, can be {@code null}
   */
  public void signalActivityStateChange(String liveActivityUuid, ActivityState newRuntimeState,
      String newRuntimeStateDetail) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityRuntimeStateChange(liveActivityUuid, newRuntimeState, newRuntimeStateDetail);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling live activity state change event for UUID %s and new runtime state %s",
            liveActivityUuid, newRuntimeState);
      }
    }
  }

  /**
   * Send the data bundle state change message to all listeners.
   *
   * @param spaceController
   *          the space controller
   * @param status
   *          data bundle status
   */
  public void signalDataBundleState(ActiveSpaceController spaceController, DataBundleState status) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onDataBundleStateChange(spaceController, status);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling live activity data bundle event for %s and status %s", spaceController.getDisplayName(),
            status);
      }
    }
  }

  /**
   * Clear all listeners from the helper.
   */
  public void clear() {
    listeners.clear();
  }
}
