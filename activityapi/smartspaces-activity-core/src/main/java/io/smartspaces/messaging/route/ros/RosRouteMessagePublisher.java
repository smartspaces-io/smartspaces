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

package io.smartspaces.messaging.route.ros;

import io.smartspaces.activity.component.route.ros.MessageCodec;
import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.util.ros.RosPublishers;

import smartspaces_msgs.GenericMessage;

import java.util.Map;

/**
 * A route message publisher for ROS.
 *
 * @author Keith M. Hughes
 */
public class RosRouteMessagePublisher implements InternalRouteMessagePublisher {

  /**
   * The channel ID for this publisher.
   */
  private String channelId;

  /**
   * The publishers for this message publisher.
   */
  private RosPublishers<GenericMessage> publishers;

  /**
   * The message codec to use for message translation.
   */
  private MessageCodec<Map<String, Object>, GenericMessage> messageCodec;

  /**
   * Construct a new publisher.
   *
   * @param channelId
   *          the channel ID for the route
   * @param publishers
   *          the ROS publishers
   * @param messageCodec
   *          the message codec to use for message translation.
   */
  public RosRouteMessagePublisher(String channelId, RosPublishers<GenericMessage> publishers,
      MessageCodec<Map<String, Object>, GenericMessage> messageCodec) {
    this.channelId = channelId;
    this.publishers = publishers;
    this.messageCodec = messageCodec;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }

  @Override
  public void writeOutputMessage(Map<String, Object> message) {
    publishers.publishMessage(messageCodec.encode(message));
  }

  @Override
  public void shutdown() {
    publishers.shutdown();
  }
}
