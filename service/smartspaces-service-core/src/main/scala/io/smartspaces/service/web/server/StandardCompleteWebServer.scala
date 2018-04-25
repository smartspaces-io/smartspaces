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

import io.smartspaces.logging.ExtendedLog

import java.io.File
import java.util.Map
import io.smartspaces.messaging.codec.MapStringMessageCodec

/**
 * A standalone web server object that includes web socket functionality.
 * 
 * @author Keith M. Hughes
 */
class StandardCompleteWebServer(
    private val _webServer: WebServer, 
    private val log: ExtendedLog) extends CompleteWebServerBehavior {

  /**
   * Web socket handler for the connection to the browser.
   */
  private var webSocketFactory: MultipleConnectionWebServerWebSocketHandlerFactory[Map[String, Object]] = 
      new BasicMultipleConnectionWebServerWebSocketHandlerFactory(this, log)
  // TODO(keith): make the websocket URI setable. For now using default.
  _webServer.setWebSocketHandlerFactory(null, webSocketFactory, new MapStringMessageCodec())
  
  override def addStaticContentHandler(uriPrefix: String, baseDir: File): Unit = {
    _webServer.addStaticContentHandler(uriPrefix, baseDir)
  }

  override def addGetRequestHandler(uriPrefix: String, usePath: Boolean,
    handler: HttpGetRequestHandler): Unit = {
    _webServer.addGetRequestHandler(uriPrefix, usePath, handler)
  }

  override def addPostRequestHandler(uriPrefix: String, usePath: Boolean,
    handler: HttpPostRequestHandler): Unit = {
    _webServer.addPostRequestHandler(uriPrefix, usePath, handler)
  }

  override def isWebSocketConnected(): Boolean = {
    return webSocketFactory.areWebSocketsConnected()
  }

  override def isWebSocketConnected(channelId: String): Boolean = {
    return webSocketFactory.isWebSocketConnected(channelId)
  }

  override def onNewWebSocketConnection(connectionId: String): Unit = {
    // Default is nothing to do
  }

  override def onWebSocketClose(connectionId: String): Unit = {
    // Default is nothing to do.
  }

  override def onNewWebSocketMessage(connectionId: String, message: Map[String, Object]): Unit = {
    // Default is to do nothing.
  }

  override def sendWebSocketMessage(connectionId: String, message: Map[String, Object]): Unit = {
    webSocketFactory.sendMessage(connectionId, message)
  }

  override def sendWebSocketMessage(message: Map[String, Object]): Unit = {
    webSocketFactory.sendMessage(message)
  }

  override def handleNewWebSocketConnection(connectionId: String): Unit = {
    onNewWebSocketConnection(connectionId)
  }

  override def handleNewWebSocketMessage(connectionId: String, message: Map[String, Object]): Unit = {
    onNewWebSocketMessage(connectionId, message)
  }

  override def handleWebSocketClose(connectionId: String): Unit = {
    onWebSocketClose(connectionId)
  }

  override def webServer(): WebServer = {
    _webServer
  }

}