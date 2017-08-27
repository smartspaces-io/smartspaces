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

package io.smartspaces.service.web.server;

import io.smartspaces.service.web.WebSocketConnection;

/**
 * A factory for web socket handlers.
 * 
 * @param <M>
 *          the type of the messages from the web socket connection
 *
 * @author Keith M. Hughes
 */
public interface WebServerWebSocketHandlerFactory<M> {

  /**
   * Create a new web socket handler.
   *
   * @param connection
   *          connection for communication with remote server
   *
   * @return a ready to use web socket handler
   */
  WebServerWebSocketHandler<M> newWebSocketHandler(WebSocketConnection<M> connection);
}
