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

import io.smartspaces.messaging.MessageWriter;

/**
 * A publisher for a message route.
 * 
 * <p>
 * All registered publishers will be sent the message.
s *
 * @author Keith M. Hughes
 */
public interface RouteMessagePublisher extends MessageWriter<Map<String, Object>> {

  /**
   * Get the channel ID for the publisher.
   *
   * @return the channel ID
   */
  String getChannelId();
}
