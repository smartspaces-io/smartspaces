/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.service.web.server;

import io.smartspaces.messaging.ChannelMessageSender;
import io.smartspaces.messaging.MessageSender;

/**
 * A factory for web socket handlers that support multiple connections.
 * 
 * <p>
 * Messages send to the non-channel message writer go to all connected web socket clients.
 * 
 * @param <M>
 *          the message type for the web socket connections
 *
 * @author Keith M. Hughes
 */
public interface MultipleConnectionWebServerWebSocketHandlerFactory<M>
    extends WebServerWebSocketHandlerFactory<M>, MessageSender<M>, ChannelMessageSender<M> {

  /**
   * Are there any web sockets connected?
   *
   * @return {@code true} if there are any connections
   */
  boolean areWebSocketsConnected();
}
