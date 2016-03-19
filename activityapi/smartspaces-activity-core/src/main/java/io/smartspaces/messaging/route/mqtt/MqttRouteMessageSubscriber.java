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

import io.smartspaces.messaging.codec.MessageDecoder;
import io.smartspaces.messaging.route.InternalRouteMessageSubscriber;
import io.smartspaces.util.messaging.mqtt.MqttSubscribers;

/**
 * A route message subscriber for MQTT messages.
 * 
 * @author Keith M. Hughes
 */
public class MqttRouteMessageSubscriber implements InternalRouteMessageSubscriber {

  /**
   * The channel ID for the route.
   */
  private String channelId;

  /**
   * The MQTT subscribers for the route.
   */
  private MqttSubscribers subscribers;

  /**
   * The message decoder for incoming messages.
   */
  private MessageDecoder<Map<String, Object>, byte[]> messageDecoder;

  /**
   * Construct a new route subscriber.
   * 
   * @param channelId
   *          ID of the route channel
   * @param subscribers
   *          the MQTT subscribers for this route
   * @param messageDecoder
   *          the message decoder for the subscribers
   */
  public MqttRouteMessageSubscriber(String channelId, MqttSubscribers subscribers,
      MessageDecoder<Map<String, Object>, byte[]> messageDecoder) {
    this.channelId = channelId;
    this.subscribers = subscribers;
    this.messageDecoder = messageDecoder;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }

  @Override
  public Map<String, Object> decodeMessage(Object message) {
    return messageDecoder.decode((byte[]) message);
  }

  @Override
  public void shutdown() {
    subscribers.shutdown();
  }
}
