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

package io.smartspaces.activity.behavior.web

import io.smartspaces.activity.ActivityBehavior
import io.smartspaces.activity.behavior.general.JsonActivityBehavior
import io.smartspaces.service.web.server.WebServer
import java.io.File
import java.util.Map
import io.smartspaces.activity.component.web.WebServerActivityComponent

/**
 * Web server behavior for an activity.
 *
 * @author Keith M. Hughes
 */
trait WebServerActivityBehavior extends ActivityBehavior with JsonActivityBehavior {

  /**
   * Add static content for the web server to serve.
   *
   * @param uriPrefix
   *          the URI prefix for this particular content
   * @param baseDir
   *          the base directory where the content will be found
   */
  def addStaticContent(uriPrefix: String, baseDir: File): Unit

  /**
   * Is the web socket connected to anything?
   *
   * @return {@code true} if the web socket is connected.
   */
  def isWebSocketConnected(): Boolean
  /**
   * A new web socket connection has been made.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param connectionId
   *          ID for the web socket connection
   */
  def onNewWebSocketConnection(connectionId: String): Unit

  /**
   * Web socket closed.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param connectionId
   *          ID for the web socket connection
   */
  def onWebSocketClose(connectionId: String): Unit

  /**
   * Received a web socket call.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data from the web socket call
   */
  def onWebSocketReceive(connectionId: String, data: Object): Unit

  /**
   * Send a JSON result to the web socket.
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data to send
   */
  def sendWebSocketJson(connectionId: String, data: Object): Unit

  /**
   * Send a JSON result to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  def sendAllWebSocketJson(data: Object): Unit

  /**
   * Send a string to the web socket.
   *
   * @param connectionId
   *          ID for the web socket connection
   * @param data
   *          the data to send
   */
  def sendWebSocketString(connectionId: String, data: String): Unit

  /**
   * Send a string to all web socket connections.
   *
   * @param data
   *          the data to send
   */
  def sendAllWebSocketString(data: String): Unit

  /**
   * Get the web server for the activity.
   *
   * @return the web server
   */
  def getWebServer(): WebServer

  /**
   * Get the web server activity component for the activity.
   *
   * @return the web server activity component
   */
  def getWebServerComponent(): WebServerActivityComponent
}