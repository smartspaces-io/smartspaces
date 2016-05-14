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

package io.smartspaces.messaging.route;

import com.google.common.collect.SetMultimap;

/**
 * The description of a route.
 * 
 * @author Keith M. HUghes
 */
public class RouteDescription {

  /**
   * The channel ID for the route.
   */
  private final String channelId;

  /**
   * The map of protocols to the topics the protocol communicates through.
   */
  private final SetMultimap<String, String> protocolToTopic;

  /**
   * Construct a new description.
   * 
   * @param channelId
   *          the ID of the channel for this route
   * @param protocolToTopic
   *          the map of protocols to the topics the protocol communicates
   *          through
   */
  public RouteDescription(String channelId, SetMultimap<String, String> protocolToTopic) {
    this.channelId = channelId;
    this.protocolToTopic = protocolToTopic;
  }

  /**
   * Get the ID of the channel for this route.
   * 
   * @return the ID
   */
  public String getChannelId() {
    return channelId;
  }

  /**
   * Get the map of protocols to the topics the protocol communicates through
   * 
   * @return the map
   */
  public SetMultimap<String, String> getProtocolToTopic() {
    return protocolToTopic;
  }
}
