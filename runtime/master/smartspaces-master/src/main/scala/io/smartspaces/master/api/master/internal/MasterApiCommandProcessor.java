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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.messaging.MessageSender;

import java.util.Map;

/**
 * The command processor for Master API requests.
 * 
 * @author Keith M. Hughes
 */
public interface MasterApiCommandProcessor {

  /**
   * Handle an API call.
   * 
   * @param incomingMessage
   *          the incoming message
   * @param responseMessageSender
   *          a message sender to send the response to
   */
  void handleApiCall(Map<String, Object> incomingMessage,
      MessageSender<Map<String, Object>> responseMessageSender);

  /**
   * Handle the state change for a live activity from the master event bus.
   *
   * @param activeLiveActivity
   *          the live activity
   * @param oldState
   *          the old state of the live activity
   * @param newState
   *          the new state of the live activity
   */
  void sendLiveActivityStateChangeMessage(ActiveLiveActivity activeLiveActivity,
      ActivityState oldState, ActivityState newState);

}