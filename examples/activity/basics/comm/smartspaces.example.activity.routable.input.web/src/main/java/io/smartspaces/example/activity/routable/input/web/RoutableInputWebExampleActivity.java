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

package io.smartspaces.example.activity.routable.input.web;

import java.util.Map;

import io.smartspaces.activity.impl.web.BaseRoutableWebActivity;

/**
 * A Smart Spaces activity which provides a routable input web example.
 *
 * <p>
 * Messages which come on the route will be displayed on the web page.
 *
 * @author Keith M. Hughes
 */
public class RoutableInputWebExampleActivity extends BaseRoutableWebActivity {

  @Override
  public void onNewRouteMessage(String channelName, Map<String, Object> message) {
    // There is only 1 channel for this activity, so don't bother checking
    // which one it is.
    if (isActivated()) {
      // In this example just pass through the message as is.
      // This is not always the best choice.
      sendWebSocketMessage(message);
    }
  }
}
