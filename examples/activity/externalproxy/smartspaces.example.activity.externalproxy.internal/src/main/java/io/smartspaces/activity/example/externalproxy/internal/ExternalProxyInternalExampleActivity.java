/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.activity.example.externalproxy.internal;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.impl.web.BaseRoutableWebActivity;
import io.smartspaces.messaging.codec.MapStringMessageCodec;
import io.smartspaces.service.web.WebSocketMessageHandler;
import io.smartspaces.service.web.client.WebSocketClient;
import io.smartspaces.service.web.client.WebSocketClientService;
import io.smartspaces.service.web.client.internal.netty.NettyWebSocketClient;
import io.smartspaces.service.web.server.HttpRequest;
import io.smartspaces.service.web.server.handler.barcode.ZXingBarcodeHttpDynamicRequestHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * A sample Smart Spaces Java-based activity which uses web sockets to
 * communicate between a browser external to the local network, say on a mobile
 * device, and the Live Activity.
 *
 * <p>
 * The activity will use a browser to show a barcode on the display. Scanning
 * the barcode will take the user to an externally facing URL. This activity
 * then receives messages from the external proxy and transmits them on the
 * configured route.
 *
 * @author Keith M. Hughes
 */
public class ExternalProxyInternalExampleActivity extends BaseRoutableWebActivity {

  /**
   * Configuration property for the host where the external proxy resides.
   */
  public static final String CONFIGURATION_NAME_PROXY_HOST = "space.activity.proxy.host";

  /**
   * Configuration property for the port on which the external proxy is
   * listening to talk to this internal listener.
   */
  public static final String CONFIGURATION_NAME_PROXY_PORT = "space.activity.proxy.port";

  /**
   * Configuration property giving the URL to be displayed in the barcode.
   */
  public static final String CONFIGURATION_NAME_BARCODE_URL = "space.activity.barcode.url";

  private String barcodeUrl;

  @Override
  public void onActivitySetup() {
    barcodeUrl = getConfiguration().getRequiredPropertyString(CONFIGURATION_NAME_BARCODE_URL);

    String proxyHost = getConfiguration().getRequiredPropertyString(CONFIGURATION_NAME_PROXY_HOST);
    int proxyPort = getConfiguration().getRequiredPropertyInteger(CONFIGURATION_NAME_PROXY_PORT);

    WebSocketClientService clientService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            WebSocketClientService.SERVICE_NAME);

    try {
      String url = new URI(String.format("ws://%s:%d/%s", proxyHost, proxyPort, "smartspaces")).toString();

      WebSocketClient<Map<String, Object>> proxyClient = clientService.newWebSocketClient(url, new WebSocketMessageHandler<Map<String, Object>>() {

        @Override
        public void onClose() {
          getLog().info("Lost connection to proxy");
        }

        @Override
        public void onConnect() {
          getLog().info("Got connection to proxy");
        }

        @Override
        public void onNewMessage(Map<String, Object> data) {
          sendRouteMessage("output1", data);
        }
      }, new MapStringMessageCodec(), getLog());

      addManagedResource(proxyClient);
    } catch (URISyntaxException e) {
      throw new SmartSpacesException("Could not create web socket client", e);
    }
  }

  @Override
  public void onActivityStartup() {
    webServer().addDynamicGetRequestHandler("barcode", true,
        new ZXingBarcodeHttpDynamicRequestHandler() {

          @Override
          public String getBarcodeContent(HttpRequest request) {
            return barcodeUrl;
          }

        });
  }

  @Override
  public void onNewWebSocketConnection(String connectionId) {
    getLog().info("Got web socket connection from connection " + connectionId);
  }

  @Override
  public void onWebSocketClose(String connectionId) {
    getLog().info("Got web socket close from connection " + connectionId);
  }
}
