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

package io.smartspaces.service.web;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.IdentityMessageCodec;
import io.smartspaces.service.web.client.internal.netty.NettyWebSocketClient;
import io.smartspaces.service.web.server.WebServerWebSocketHandler;
import io.smartspaces.service.web.server.WebServerWebSocketHandlerFactory;
import io.smartspaces.service.web.server.WebServerWebSocketHandlerSupport;
import io.smartspaces.service.web.server.internal.netty.NettyWebServer;
import io.smartspaces.testing.sizes.TestSizeLarge;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A test for web socket connections.
 *
 * <p>
 * This test does both the web socket server and client sides.
 *
 * @author Keith M. Hughes
 */
@Category(TestSizeLarge.class)
public class WebSocketTest {

  private ExtendedLog log;
  private ScheduledExecutorService threadPool;

  //private WebSocketConnection<String> serverConnection;

  @Before
  public void setup() {
    log = Mockito.mock(ExtendedLog.class);

    threadPool = Executors.newScheduledThreadPool(100);
  }

  @After
  public void cleanup() {
    threadPool.shutdown();
  }

  @Test
  public void testWebSocketCommunication() throws Exception {
    IdentityMessageCodec<String> identityMessageCodec = new IdentityMessageCodec<String>();

    final CountDownLatch clientOpenning = new CountDownLatch(1);
    final CountDownLatch clientClosing = new CountDownLatch(1);

    final AtomicBoolean onConnectCalledServer = new AtomicBoolean(false);
    final AtomicBoolean onCloseCalledServer = new AtomicBoolean(false);

    final AtomicReference<WebServerWebSocketHandler<String>> serverHandler =
        new AtomicReference<WebServerWebSocketHandler<String>>();

    int port = 9001;
    String webSocketUriPrefix = "websockettest";

    URI uri = new URI(String.format("ws://127.0.0.1:%d/%s", port, webSocketUriPrefix));

    final List<Integer> serverReceivedList = new ArrayList<>();
    final List<Integer> clientReceivedList = new ArrayList<>();
    List<Integer> serverSentList = new ArrayList<>();
    List<Integer> clientSentList = new ArrayList<>();
    Random random = new Random(System.currentTimeMillis());

    for (int i = 0; i < 100; i++) {
      clientSentList.add(random.nextInt());
      serverSentList.add(random.nextInt());
    }

    NettyWebServer server = new NettyWebServer(threadPool, log);
    server.setServerName("test-server");
    server.setPort(port);
    server.setWebSocketHandlerFactory(webSocketUriPrefix, new WebServerWebSocketHandlerFactory<String>() {

      @Override
      public WebServerWebSocketHandler<String> newWebSocketHandler(WebSocketConnection<String> connection) {
        WebServerWebSocketHandler<String> handler = new WebServerWebSocketHandlerSupport<String>(connection) {

          @Override
          public void onNewMessage(String message) {

            serverReceivedList.add(Integer.parseInt(message.substring(message.indexOf("-") + 1)));
          }

          @Override
          public void onConnect() {
            onConnectCalledServer.set(true);
          }

          @Override
          public void onClose() {
            onCloseCalledServer.set(true);
          }
        };

        serverHandler.set(handler);

        return handler;
      }
    }, identityMessageCodec);
    server.startup();

    Thread.sleep(2000);

    WebSocketMessageHandler<String> clientHandler = new WebSocketMessageHandler<String>() {

      @Override
      public void onConnect() {
        clientOpenning.countDown();
      }

      @Override
      public void onClose() {
        clientClosing.countDown();
      }

      @Override
      public void onNewMessage(String message) {
        clientReceivedList.add(Integer.parseInt(message.substring(message.indexOf("-") + 1)));
      }
    };

    NettyWebSocketClient<String> client = new NettyWebSocketClient<String>(uri, clientHandler, identityMessageCodec, threadPool, log);
    client.startup();

    Assert.assertTrue(clientOpenning.await(10, TimeUnit.SECONDS));

    Assert.assertTrue(client.isOpen());

    for (Integer i : clientSentList) {
      client.sendMessage("message-"+i);
    }

    for (Integer i : serverSentList) {
      serverHandler.get().sendMessage("message-"+i);
    }

    client.ping();

    client.shutdown();

    Assert.assertTrue(clientClosing.await(10, TimeUnit.SECONDS));

    server.shutdown();

    Assert.assertEquals(clientSentList, serverReceivedList);
    Assert.assertEquals(serverSentList, clientReceivedList);
    Assert.assertTrue(onConnectCalledServer.get());
    Assert.assertTrue(onCloseCalledServer.get());
  }
}
