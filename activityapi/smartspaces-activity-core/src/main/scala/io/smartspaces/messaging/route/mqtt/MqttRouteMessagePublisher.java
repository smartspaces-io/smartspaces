/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.messaging.route.mqtt;

import java.util.Map;

import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.util.messaging.mqtt.MqttPublishers;

/**
 * A route message publisher for MQTT messages.
 * 
 * @author Keith M. Hughes
 */
public class MqttRouteMessagePublisher implements InternalRouteMessagePublisher {

  /**
   * The channel ID for this publisher.
   */
  private String channelId;

  /**
   * The publishers for this message publisher.
   */
  private MqttPublishers<Map<String, Object>> publishers;

  /**
   * Construct a new publisher.
   *
   * @param channelId
   *          the channel ID for the route
   * @param publishers
   *          the MQTT publishers
   */
  public MqttRouteMessagePublisher(String channelId,
      MqttPublishers<Map<String, Object>> publishers) {
    this.channelId = channelId;
    this.publishers = publishers;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }

  @Override
  public void writeMessage(Map<String, Object> message) {
    publishers.publishMessage(message);
  }

  @Override
  public void shutdown() {
    publishers.shutdown();
  }
}
