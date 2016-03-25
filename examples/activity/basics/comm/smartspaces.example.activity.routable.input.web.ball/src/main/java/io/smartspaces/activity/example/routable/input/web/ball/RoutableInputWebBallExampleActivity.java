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

package io.smartspaces.activity.example.routable.input.web.ball;

import java.util.Map;

import io.smartspaces.activity.impl.web.BaseRoutableWebActivity;

/**
 * A sample Smart Spaces Java-based activity which uses web sockets to
 * communicate between the browser and the Live Activity.
 *
 * @author Keith M. Hughes
 */
public class RoutableInputWebBallExampleActivity extends BaseRoutableWebActivity {

  @Override
  public void onNewInputMessage(String channelName, Map<String, Object> message) {
    if ("input1".equals(channelName) && isActivated()) {
      sendAllWebSocketJson(message);
    }
  }

  @Override
  public void onNewWebSocketConnection(String connectionId) {
    getLog().info("Got web socket connection from connection " + connectionId);
  }

  @Override
  public void onWebSocketClose(String connectionId) {
    getLog().info("Got web socket close from connection " + connectionId);
  }

  @Override
  public void onWebSocketReceive(String connectionId, Object d) {
    getLog().info("Got web socket data from connection " + connectionId);

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) d;
    getLog().info(data);
  }
}
