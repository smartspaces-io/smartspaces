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
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.server.services.ActiveSpaceController;
import io.smartspaces.master.server.services.RemoteSpaceControllerClientListener;
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
   * @param controller
   *          the controller being disconnected from
   */
  public void signalSpaceControllerConnectAttempt(ActiveSpaceController controller) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectAttempted(controller);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller connect event for %s",
            controller.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller connection failed.
   *
   * @param controller
   *          the controller being disconnected from
   * @param timeToWait
   *          the time waited for the space controller connection, in
   *          milliseconds
   */
  public void signalSpaceControllerConnectFailed(ActiveSpaceController controller, long waitedTime) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerConnectFailed(controller, waitedTime);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller connect event for %s",
            controller.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller disconnecting.
   *
   * @param controller
   *          the controller being disconnected from
   */
  public void signalSpaceControllerDisconnectAttempt(ActiveSpaceController controller) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerDisconnectAttempted(controller);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller disconnect event for %s",
            controller.getDisplayName());
      }
    }
  }

  /**
   * Signal a space controller heartbeat.
   *
   * @param uuid
   *          uuid of the controller
   * @param timestamp
   *          timestamp of the heartbeat
   */
  public void signalSpaceControllerHeartbeat(String uuid, long timestamp) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerHeartbeat(uuid, timestamp);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling space controller heartbeat event for UUID %s and timestamp %d", uuid,
            timestamp);
      }
    }
  }

  /**
   * Signal that the controller status has been updated.
   *
   * @param uuid
   *          the UUID of the space controller
   * @param state
   *          the new state
   */
  public void signalSpaceControllerStatusChange(String uuid, SpaceControllerState state) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerStatusChange(uuid, state);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling space controller status change event for UUID %s and state %s", uuid,
            state);
      }
    }
  }

  /**
   * Signal that the space controller is shutting down.
   *
   * @param uuid
   *          the UUID of the space controller
   */
  public void signalSpaceControllerShutdown(String uuid) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onSpaceControllerShutdown(uuid);
      } catch (Throwable e) {
        log.formatError(e, "Error handling space controller shutdown event for UUID %s", uuid);
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
  public void signalActivityDeployStatus(String uuid, LiveActivityDeploymentResponse result) {
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
   * @param uuid
   *          UUID of the activity.
   * @param result
   *          result of the deletion
   */
  public void signalActivityDelete(String uuid, LiveActivityDeleteResponse result) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityDelete(uuid, result);
      } catch (Throwable e) {
        log.formatError(e, "Error handling live activity delete event for UUID %s and result %s",
            uuid, result);
      }
    }
  }

  /**
   * Send the activity state change message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param newRuntimeState
   *          runtime status of the remote activity
   * @param newRuntimeStateDetail
   *          detail about the new runtime state, can be {@code null}
   */
  public void signalActivityStateChange(String uuid, ActivityState newRuntimeState,
      String newRuntimeStateDetail) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onLiveActivityRuntimeStateChange(uuid, newRuntimeState, newRuntimeStateDetail);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling live activity state change event for UUID %s and new runtime state %s",
            uuid, newRuntimeState);
      }
    }
  }

  /**
   * Send the data bundle state change message to all listeners.
   *
   * @param uuid
   *          UUID of the activity
   * @param status
   *          data bundle status
   */
  public void signalDataBundleState(String uuid, DataBundleState status) {
    for (RemoteSpaceControllerClientListener listener : listeners) {
      try {
        listener.onDataBundleStateChange(uuid, status);
      } catch (Throwable e) {
        log.formatError(e,
            "Error handling live activity data bundle event for UUID %s and status %s", uuid,
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
