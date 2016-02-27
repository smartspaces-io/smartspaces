/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.activity.component.route.ros;

import io.smartspaces.activity.component.route.MessageRouterSupportedMessageTypes;
import io.smartspaces.messaging.MessagePublisher;
import io.smartspaces.messaging.route.RouteMessagePublisher;
import io.smartspaces.util.data.json.JsonBuilder;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

import java.util.Map;

import org.apache.commons.logging.Log;

import smartspaces_msgs.GenericMessage;

/**
 * A simple route message publisher that uses JSON for serialization and
 * {@link GenericMessage} for the transport wrapper.
 *
 * @author Keith M. Hughes
 */
public class JsonGenericMessageSimpleRouteMessagePublisher implements MessagePublisher {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The message publisher for messages.
   */
  private RouteMessagePublisher<GenericMessage> messagePublisher;

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * Construct a new publisher.
   *
   * @param messagePublisher
   *          the underlying message publisher
   * @param log
   *          the logger to use
   */
  public JsonGenericMessageSimpleRouteMessagePublisher(
      RouteMessagePublisher<GenericMessage> messagePublisher, Log log) {
    this.messagePublisher = messagePublisher;
    this.log = log;
  }

  @Override
  public String getChannelId() {
    return messagePublisher.getChannelId();
  }

  @Override
  public void sendMessage(Map<String, Object> message) {
    GenericMessage outgoing = messagePublisher.newMessage();

    try {
      outgoing.setType(MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE);
      outgoing.setMessage(MAPPER.toString(message));

      messagePublisher.writeOutputMessage(outgoing);
    } catch (Throwable e) {
      log.error(
          String.format("Could not write JSON message on output channel %s",
              messagePublisher.getChannelId()), e);
    }
  }

  @Override
  public void sendMessage(JsonBuilder message) {
    sendMessage(message.build());
  }

  @Override
  public void sendMessage(String message) {
    GenericMessage outgoing = messagePublisher.newMessage();
    try {
      outgoing.setType(MessageRouterSupportedMessageTypes.STRING_MESSAGE_TYPE);
      outgoing.setMessage(message);

      messagePublisher.writeOutputMessage(outgoing);
    } catch (Exception e) {
      log.error(
          String.format("Could not write message on output channel %s",
              messagePublisher.getChannelId()), e);
    }
  }
}
