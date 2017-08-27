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

package io.smartspaces.activity.example.web;

import java.util.HashMap;
import java.util.Map;

import io.smartspaces.activity.impl.web.BaseWebActivity;

/**
 * A sample Smart Spaces Java-based activity which uses web sockets to
 * communicate between the browser and the Live Activity.
 * 
 * @author Keith M. Hughes
 */
public class JavaWebExampleActivity extends BaseWebActivity {

  @Override
  public void onActivityActivate() {
    sendImageUrl("images/activate.jpg");
  }

  @Override
  public void onActivityDeactivate() {
    sendImageUrl("images/deactivate.jpg");
  }

  private void sendImageUrl(String imageUrl) {
    Map<String, Object> message = new HashMap<>();
    message.put("imageUrl", imageUrl);

    // Send data to all websocket connections
    sendWebSocketMessage(message);
  }

  @Override
  public void onNewWebSocketConnection(String channelId) {
    getLog().info("Got web socket connection from connection " + channelId);
  }

  @Override
  public void onWebSocketClose(String channelId) {
    getLog().info("Got web socket close from connection " + channelId);
  }

  @Override
  public void onNewWebSocketMessage(String channelId, Map<String, Object> message) {
    getLog().info("Got web socket data from connection " + channelId);

    getLog().info(message);
  }
}
