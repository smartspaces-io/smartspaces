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

package io.smartspaces.messaging.route;

import java.util.Map;

/**
 * A publisher for a message route.
s *
 * @author Keith M. Hughes
 */
public interface RouteMessagePublisher {

  /**
   * Get the channel ID for the publisher.
   *
   * @return the channel ID
   */
  String getChannelId();

  /**
   * Publish a message to all registered publishers.
   *
   * @param message
   *          The message to be published.
   */
  void writeOutputMessage(Map<String, Object> message);
}
