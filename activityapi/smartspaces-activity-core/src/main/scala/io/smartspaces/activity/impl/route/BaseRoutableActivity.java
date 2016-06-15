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

package io.smartspaces.activity.impl.route;

import java.util.Map;

import io.smartspaces.activity.Activity;
import io.smartspaces.activity.component.route.BasicMessageRouterActivityComponent;
import io.smartspaces.activity.component.route.MessageRouterActivityComponent;
import io.smartspaces.activity.execution.ActivityMethodInvocation;
import io.smartspaces.activity.impl.ros.BaseRosActivity;
import io.smartspaces.messaging.route.RoutableInputMessageListener;
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

/**
 * An {@link Activity} that simplifies the use of SmartSpaces routes.
 *
 * @author Keith M. Hughes
 */
public class BaseRoutableActivity extends BaseRosActivity {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * Router for input and output messages.
   */
  private MessageRouterActivityComponent router;

  @Override
  public void commonActivitySetup() {
    super.commonActivitySetup();

    router = addActivityComponent(BasicMessageRouterActivityComponent.COMPONENT_NAME);
    router.setRoutableInputMessageListener(new RoutableInputMessageListener() {
      @Override
      public void onNewRoutableInputMessage(String channelName, Map<String, Object> message) {
        handleRoutableInputMessage(channelName, message);
      }
    });
  }

  /**
   * Handle a new input message.
   *
   * @param channelName
   *          the name of the channel
   * @param message
   *          the generic message
   */
  private void handleRoutableInputMessage(String channelName, Map<String, Object> message) {
    try {
      callOnNewInputMessage(channelName, message);
    } catch (Exception e) {
      getLog().error("Could not process input message", e);
    }
  }

  /**
   * Convert a map to a JSON string.
   *
   * @param map
   *          the map to convert to a string
   *
   * @return the JSON string representation of the map
   */
  public String jsonStringify(Map<String, Object> map) {
    return MAPPER.toString(map);
  }

  /**
   * Parse a JSON string and return the map.
   *
   * @param data
   *          the JSON string
   *
   * @return the map for the string
   */
  public Map<String, Object> jsonParse(String data) {
    return MAPPER.parseObject(data);
  }

  /**
   * A new message is coming in.
   *
   * @param channelName
   *          name of the input channel the message came in on
   * @param message
   *          the message that came in
   */
  public void onNewInputMessage(String channelName, Map<String, Object> message) {
    // Default is to do nothing.
  }

  /**
   * Send an output message.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public void sendOutputMessage(String channelName, Map<String, Object> message) {
    try {
      router.writeOutputMessage(channelName, message);
    } catch (Throwable e) {
      getLog().error(
          String.format("Could not write message on route output channel %s", channelName), e);
    }
  }

  /**
   * Send an output message from a {@link DynamicObjectBuilder}.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public void sendOutputMessage(String channelName, DynamicObjectBuilder message) {
    sendOutputMessage(channelName, message.toMap());
  }

  /**
   * Call the {@link #onNewInputMessage(String, Map)} method.
   *
   * @param channelName
   *          the name of the channel
   * @param message
   *          the message
   */
  private void callOnNewInputMessage(String channelName, Map<String, Object> message) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onNewInputMessage(channelName, message);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Get the router for the activity.
   * 
   * @return the router for the activity
   */
  protected MessageRouterActivityComponent getRouterActivityComponent() {
    return router;
  }
}
