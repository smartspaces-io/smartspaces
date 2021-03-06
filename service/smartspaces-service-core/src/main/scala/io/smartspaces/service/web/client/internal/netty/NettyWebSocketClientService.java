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

package io.smartspaces.service.web.client.internal.netty;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.web.WebSocketMessageHandler;
import io.smartspaces.service.web.client.WebSocketClient;
import io.smartspaces.service.web.client.WebSocketClientService;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A {@link WebSocketClientService} based on Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWebSocketClientService extends BaseSupportedService implements
    WebSocketClientService {

  @Override
  public String getName() {
    return WebSocketClientService.SERVICE_NAME;
  }

  @Override
  public <M> WebSocketClient<M> newWebSocketClient(String uri, WebSocketMessageHandler<M> handler, MessageCodec<M, String> messageCodec, ExtendedLog log) {
    try {
      URI u = new URI(uri);

      return new NettyWebSocketClient<M>(u, handler, messageCodec, getSpaceEnvironment().getExecutorService(), log);
    } catch (URISyntaxException e) {
      throw new SmartSpacesException(String.format("Bad URI syntax for web socket URI: %s", uri), e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <M> WebSocketClient<M> newWebSocketClient(String uri, MessageCodec<M, String> messageCodec, ExtendedLog log) {
    return newWebSocketClient(uri, null, messageCodec, log);
  }
}
