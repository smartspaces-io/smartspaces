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

package io.smartspaces.service.web.client;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.service.SupportedService;
import io.smartspaces.service.web.WebSocketMessageHandler;

/**
 * A service for obtaining web socket client instances.
 *
 * @author Keith M. Hughes
 */
public interface WebSocketClientService extends SupportedService {

  /**
   * Service name for the service.
   */
  String SERVICE_NAME = "web.client.websocket";

  /**
   * Create a new server.
   *
   * @param uri
   *          the uri to connect to
   * @param handler
   *          the handler for web socket events
   * @param log
   *          logger to be used with the client
   *
   * @return the web socket client
   */
  <M> WebSocketClient<M> newWebSocketClient(String uri, WebSocketMessageHandler<M> handler, MessageCodec<M, String> messageCodec, ExtendedLog log);

  /**
   * Create a new client.
   *
   * @param uri
   *          the uri to connect to
   * @param log
   *          logger to be used with the client
   *
   * @return the web socket client
   */
  <M> WebSocketClient<M>  newWebSocketClient(String uri, MessageCodec<M, String> messageCodec, ExtendedLog log);
}
