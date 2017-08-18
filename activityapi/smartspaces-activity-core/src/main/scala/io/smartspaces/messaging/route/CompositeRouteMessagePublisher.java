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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;

/**
 * A route message publisher that contains multiple publishers, all of which
 * will be published to.
 * 
 * @author Keith M. Hughes
 */
public class CompositeRouteMessagePublisher implements InternalRouteMessagePublisher {

  /**
   * The channel ID for the collection of subscribers.
   */
  private String channelId;

  /**
   * The list of publishers.
   */
  private List<InternalRouteMessagePublisher> publishers = new CopyOnWriteArrayList<>();

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * Construct a new composite publisher.
   * 
   * @param channelId
   *          the channel ID for the publisher
   * @param publishers
   *          the publishers
   * @param log
   *          the logger to use
   */
  public CompositeRouteMessagePublisher(String channelId,
      List<InternalRouteMessagePublisher> publishers, Log log) {
    this.channelId = channelId;
    this.publishers.addAll(publishers);
    this.log = log;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }

  @Override
  public void writeMessage(Map<String, Object> message) {
    for (InternalRouteMessagePublisher publisher : publishers) {
      try {
        publisher.writeMessage(message);
      } catch (Throwable e) {
        log.error(String.format("Error while writing route message channel %s", channelId), e);
      }
    }
  }

  @Override
  public void shutdown() {
    for (InternalRouteMessagePublisher publisher : publishers) {
      try {
        publisher.shutdown();
      } catch (Throwable e) {
        log.error(String.format("Error while shutting down route message channel %s", channelId),
            e);
      }
    }
  }
}
