/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.service.web.server

import java.io.File
import java.util.Map

/**
 * A behavior for web servers. Complete web servers have content and web socket behaviors.
 *
 * @author Keith M. Hughes
 */
trait CompleteWebServerBehavior extends MultipleConnectionWebSocketHandler[Map[String, Object]] {

  /**
   * Add static content for the web server to serve.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   */
  def addStaticContentHandler(uriPrefix: String, baseDir: File): Unit

  /**
   * Add dynamic content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param usePath
   *          {@code true} if the path will be used as part of request
   *          processing
   * @param handler
   *          content handler being added
   */
  def addDynamicGetContentHandler(uriPrefix: String, usePath: Boolean,
    handler: HttpDynamicGetRequestHandler): Unit

  /**
   * Add dynamic content for the web server to serve.
   *
   * <p>
   * This can be called either before or after calling
   * {@link BasicWebServerActivityComponent#configureComponent(Activity, Configuration)}
   * . But if called both before and after, the second call will be the one
   * used.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param usePath
   *          {@code true} if the path will be used as part of request
   *          processing
   * @param handler
   *          content handler being added
   */
  def addDynamicPostRequestHandler(uriPrefix: String, usePath: Boolean,
    handler: HttpDynamicPostRequestHandler): Unit

  /**
   * Is the web socket connected to anything?
   *
   * @return {@code true} if the web socket is connected.
   */
  def isWebSocketConnected(): Boolean

  /**
   * Is the web socket connected to a particular channel?
   *
   * @return {@code true} if the web socket is connected.
   */
  def isWebSocketConnected(channelId: String): Boolean

  /**
   * A new web socket connection has been made.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param channelId
   *          ID for the web socket connection
   */
  def onNewWebSocketConnection(channelId: String): Unit

  /**
   * Web socket closed.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param channelId
   *          ID for the web socket connection
   */
  def onWebSocketClose(channelId: String): Unit

  /**
   * Received a web socket call.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param channelId
   *          ID for the web socket connection
   * @param message
   *          the data from the web socket call
   */
  def onNewWebSocketMessage(channelId: String, message: Map[String, Object]): Unit

  /**
   * Send a JSON result to the web socket.
   *
   * @param channelId
   *          ID for the web socket connection
   * @param message
   *          the message to write
   */
  def sendWebSocketMessage(channelId: String, message: Map[String, Object]): Unit

  /**
   * Send a JSON result to all web socket connections.
   *
   * @param message
   *          the message to write
   */
  def sendWebSocketMessage(message: Map[String, Object]): Unit

  /**
   * Get the web server for the activity.
   *
   * @return the web server
   */
  def webServer(): WebServer
}