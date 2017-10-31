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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.MessageSender;
import io.smartspaces.service.web.WebSocketConnection;

/**
 * A basic implementation of the
 * {@link MultipleConnectionWebServerWebSocketHandlerFactory}.
 *
 * @author Keith M. Hughes
 */
public class BasicMultipleConnectionWebServerWebSocketHandlerFactory<M>
    implements MultipleConnectionWebServerWebSocketHandlerFactory<M> {

  /**
   * The client handler.
   */
  private final MultipleConnectionWebSocketHandler<M> clientHandler;

  /**
   * A map from connect IDs to handlers.
   */
  private final Map<String, MyWebServerWebSocketHandler> handlers = Maps.newConcurrentMap();

  /**
   * Creator of channel IDs.
   */
  private final AtomicLong channelIdFactory = new AtomicLong(System.currentTimeMillis());

  /**
   * Log.
   */
  private final ExtendedLog log;

  /**
   * Construct a basic factory.
   *
   * @param clientHandler
   *          the client handler to use
   * @param log
   *          the logger to use
   */
  public BasicMultipleConnectionWebServerWebSocketHandlerFactory(
      MultipleConnectionWebSocketHandler<M> clientHandler, ExtendedLog log) {
    this.clientHandler = clientHandler;
    this.log = log;
  }

  @Override
  public WebServerWebSocketMessageHandler<M> newWebSocketHandler(WebSocketConnection<M> connection) {
    return new MyWebServerWebSocketHandler(connection);
  }

  @Override
  public boolean areWebSocketsConnected() {
    return !handlers.isEmpty();
  }

  @Override
  public boolean isWebSocketConnected(String channelId) {
    return handlers.containsKey(channelId);
  }

  @Override
  public MessageSender<M> getChannelMessageSender(String channelId) {
    return handlers.get(channelId);
  }

  @Override
  public void sendMessage(String channelId, M data) {
    MessageSender<M> sender = getChannelMessageSender(channelId);
    if (sender != null) {
      sender.sendMessage(data);
    } else {
      log.formatError("Unknown web socket channel ID %s", channelId);
    }
  }

  @Override
  public void sendMessage(M message) {
    for (MyWebServerWebSocketHandler handler : handlers.values()) {
      handler.sendMessage(message);
    }
  }

  /**
   * Create a new connection ID.
   *
   * @return the new connection ID
   */
  private String newConnectionId() {
    return Long.toHexString(channelIdFactory.getAndAdd(1));
  }

  /**
   * Web socket handler for this class.
   *
   * @author Keith M. Hughes
   */
  public class MyWebServerWebSocketHandler extends WebServerWebSocketHandlerSupport<M> {

    /**
     * The ID for the channel.
     */
    private final String channelId;

    /**
     * Construct a new web socket handler.
     *
     * @param connection
     *          the web socket connection
     */
    public MyWebServerWebSocketHandler(WebSocketConnection<M> connection) {
      super(connection);

      this.channelId = newConnectionId();
      handlers.put(channelId, this);
    }

    @Override
    public void onNewMessage(M message) {
      clientHandler.handleNewWebSocketMessage(channelId, message);
    }

    @Override
    public void onConnect() {
      clientHandler.handleNewWebSocketConnection(channelId);
    }

    @Override
    public void onClose() {
      handlers.remove(channelId);

      clientHandler.handleWebSocketClose(channelId);
    }
  }
}
