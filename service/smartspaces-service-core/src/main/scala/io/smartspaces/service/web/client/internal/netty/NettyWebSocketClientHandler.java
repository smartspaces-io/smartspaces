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

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.service.web.WebSocketMessageHandler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

/**
 * A Netty-based websocket client handler.
 * 
 * @param <M>
 *          the type of messages for handlers and writers
 *
 * @author Keith M. Hughes
 */

public class NettyWebSocketClientHandler<M> extends SimpleChannelUpstreamHandler {

  /**
   * The handshaker for the connection.
   */
  private WebSocketClientHandshaker handshaker;

  /**
   * The user handler for the connection.
   */
  private WebSocketMessageHandler<M> handler;
  
  /**
   * The message codec to use.
   */
  private MessageCodec<M, String> messageCodec;

  /**
   * Log for this handler.
   */
  private ExtendedLog log;

  /**
   * Construct a new client handler.
   *
   * @param handshaker
   *          the web socket handshaker
   * @param handler
   *          the handler for socket requests
   * @param log
   *          the logger
   */
  public NettyWebSocketClientHandler(WebSocketClientHandshaker handshaker,
      WebSocketMessageHandler<M> handler, MessageCodec<M, String> messageCodec, ExtendedLog log) {
    this.handshaker = handshaker;
    this.handler = handler;
    this.messageCodec = messageCodec;
    this.log = log;
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    handler.onClose();
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Channel ch = ctx.getChannel();
    if (!handshaker.isHandshakeComplete()) {
      handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());

      handler.onConnect();

      return;
    }

    if (e.getMessage() instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) e.getMessage();
      String message = "Unexpected HttpResponse (status=" + response.getStatus() + ", content="
          + response.getContent().toString(CharsetUtil.UTF_8) + ')';
      log.error(String.format("Web socket client: %s", message));

      throw new Exception(message);
    }

    WebSocketFrame frame = (WebSocketFrame) e.getMessage();
    if (frame instanceof TextWebSocketFrame) {
      TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      try {
        handler.onNewMessage(messageCodec.decode(textFrame.getText()));
      } catch (Exception e1) {
        log.error("Error while decoding JSON websocket message", e1);
      }
    } else if (frame instanceof PongWebSocketFrame) {
      // TODO(keith): What should be done with a pong?
    } else if (frame instanceof CloseWebSocketFrame) {
      ch.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Error during web socket client connection, closing connecton", e.getCause());

    e.getChannel().close();
  }
}
