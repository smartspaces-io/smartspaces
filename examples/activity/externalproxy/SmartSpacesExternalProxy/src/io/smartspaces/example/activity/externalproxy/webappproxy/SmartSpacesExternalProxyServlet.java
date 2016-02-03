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

package io.smartspaces.example.activity.externalproxy.webappproxy;

import io.smartspaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory;
import io.smartspaces.activity.impl.web.MultipleConnectionWebServerWebSocketHandlerFactory.MultipleConnectionWebSocketHandler;
import io.smartspaces.service.web.server.HttpDynamicRequestHandler;
import io.smartspaces.service.web.server.HttpRequest;
import io.smartspaces.service.web.server.HttpResponse;
import io.smartspaces.service.web.server.internal.netty.NettyWebServer;
import io.smartspaces.util.io.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A webapp which provides a proxy into a Smart Spaces installation with
 * a private network and the external world.
 *
 * <p>
 * This webapp starts up 2 web socket servers, one for mobile devices to attach
 * to and one for the Smart Spaces side to call out to. Events from the
 * web page are sent to the mobile web socket server which then sends them to
 * the web socket client running in an activity in an Smart Spaces private
 * network.
 *
 * @author Keith M. Hughes
 */
@SuppressWarnings("serial")
public class SmartSpacesExternalProxyServlet extends HttpServlet {

  /**
   * The web server port that Smart Spaces will attach to for its web
   * socket communication.
   */
  public static final int WEBSERVER_PORT_SMARTSPACES = 8082;

  /**
   * The URL prefix for Smart Spaces to attach to the websocket portion of
   * the Smart Spaces web server.
   */
  public static final String URL_PREFIX_WEBSOCKET_SMARTSPACES = "smartspaces";

  /**
   * The web server port that the mobile device will attach to both for HTML
   * content and for the websocket.
   */
  public static final int WEBSERVER_PORT_MOBILE = 8081;

  /**
   * The URL prefix that will be used for getting mobile content.
   */
  public static final String URL_PREFIX_MOBILE_CONTENT = "/isproxy";

  /**
   * The URL prefix for the mobile device to attach to the websocket portion of
   * the mobile web server.
   */
  public static final String URL_PREFIX_WEBSOCKET_MOBILE = "mobile";

  /**
   * The name of the servlet context init parameter for discovering the host of
   * the server.
   */
  public static final String CONTEXT_PARAMETER_MY_SERVER_HOST = "myServerHost";

  /**
   * The web server for connections with mobile devices.
   */
  private NettyWebServer mobileServer;

  /**
   * Web socket handler factory for the mobile connection.
   */
  private MultipleConnectionWebServerWebSocketHandlerFactory mobileWebSocketHandlerFactory;

  /**
   * The web server for connections with the Smart Spaces network.
   */
  private NettyWebServer smartSpacesServer;

  /**
   * Web socket handler factory for the Smart Spaces connection.
   */
  private MultipleConnectionWebServerWebSocketHandlerFactory smartSpacesWebSocketHandlerFactory;

  /**
   * The thread pool for connections.
   */
  private ScheduledExecutorService threadPool;

  /**
   * The log for the servlet.
   */
  private Log log;

  @Override
  public void init() throws ServletException {
    threadPool = Executors.newScheduledThreadPool(100);
    log = new Jdk14Logger("goober");

    log.info("Starting proxy server");
    try {
      super.init();

      final String contents = getFunctionContent("deviceorientation");

      mobileServer = new NettyWebServer("mobile-server", WEBSERVER_PORT_MOBILE, threadPool, log);
      mobileServer.addDynamicContentHandler(URL_PREFIX_MOBILE_CONTENT, true,
          new HttpDynamicRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) {

              response.setContentType("text/html");
              try {
                Files.copyStream(new ByteArrayInputStream(contents.getBytes()),
                    response.getOutputStream(), false);
              } catch (IOException e) {
                log.error("Could not write response", e);
              }
            }
          });

      mobileWebSocketHandlerFactory =
          new MultipleConnectionWebServerWebSocketHandlerFactory(
              new MultipleConnectionWebSocketHandler() {

                @Override
                public void handleNewWebSocketConnection(String connectionId) {
                  log.info(String.format("Got web socket connection from mobile device %s",
                      connectionId));
                }

                @Override
                public void handleWebSocketClose(String connectionId) {
                  log.info(String.format("Lost web socket connection from mobile device %s",
                      connectionId));
                }

                @Override
                public void handleWebSocketReceive(String connectionId, Object d) {
                  @SuppressWarnings("unchecked")
                  Map<String, Object> data = (Map<String, Object>) d;
                  data.put("id", connectionId);

                  smartSpacesWebSocketHandlerFactory.sendJson(data);
                }
              }, log);

      mobileServer.setWebSocketHandlerFactory(URL_PREFIX_WEBSOCKET_MOBILE,
          mobileWebSocketHandlerFactory);
      mobileServer.startup();

      smartSpacesServer =
          new NettyWebServer("smartspaces-server", WEBSERVER_PORT_SMARTSPACES,
              threadPool, log);

      smartSpacesWebSocketHandlerFactory =
          new MultipleConnectionWebServerWebSocketHandlerFactory(
              new MultipleConnectionWebSocketHandler() {

                @Override
                public void handleNewWebSocketConnection(String connectionId) {
                  log.info(String.format("Got web socket connection from Smart Spaces %s",
                      connectionId));
                }

                @Override
                public void handleWebSocketClose(String connectionId) {
                  log.info(String.format("Lost web socket connection from Smart Spaces %s",
                      connectionId));
                }

                @Override
                public void handleWebSocketReceive(String connectionId, Object d) {
                  @SuppressWarnings("unchecked")
                  Map<String, Object> data = (Map<String, Object>) d;
                }
              }, log);
      smartSpacesServer.setWebSocketHandlerFactory(URL_PREFIX_WEBSOCKET_SMARTSPACES,
          smartSpacesWebSocketHandlerFactory);

      smartSpacesServer.startup();

      log.info("Servers started");
    } catch (Exception e) {
      log.error("Could not start up proxy", e);
    }
  }

  /**
   * Get the content for the function of the mobile device.
   *
   * @param function
   *          the function the mobile device should have
   *
   * @return the content for the function
   */
  private String getFunctionContent(String function) {
    ServletContext context = getServletContext();
    String fullPath = context.getRealPath("/WEB-INF/functions/" + function + ".html");

    String serverHost = context.getInitParameter(CONTEXT_PARAMETER_MY_SERVER_HOST);

    String content = Files.readFile(new File(fullPath));

    content = content.replaceAll("\\@HOST\\@", serverHost);

    return content;
  }

  @Override
  public void destroy() {
    super.destroy();

    smartSpacesServer.shutdown();
    mobileServer.shutdown();
    threadPool.shutdown();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    resp.getWriter()
        .println(
            "<html><body><h1>Welcome</h1><p>Welcome to the Smart Spaces External Proxy</p></body></html>");
  }
}
