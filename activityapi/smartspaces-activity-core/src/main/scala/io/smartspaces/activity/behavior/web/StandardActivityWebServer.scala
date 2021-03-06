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

import java.io.File
import java.util.Map

import io.smartspaces.activity.behavior.general.StandardActivityJson
import io.smartspaces.activity.component.web.WebServerActivityComponent
import io.smartspaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory
import io.smartspaces.service.web.server.HttpGetRequestHandler
import io.smartspaces.service.web.server.HttpPostRequestHandler
import io.smartspaces.service.web.server.MultipleConnectionWebServerWebSocketHandlerFactory
import io.smartspaces.service.web.server.MultipleConnectionWebSocketHandler
import io.smartspaces.service.web.server.WebServer

/**
 * An activity behavior for web server support.
 *
 * <p>
 * This behavior uses the registered {@link WebServerActivityComponent.COMPONENT_NAME} activity component.
 *
 * @author Keith M. Hughes
 */
trait StandardActivityWebServer extends WebServerActivityBehavior with StandardActivityJson with MultipleConnectionWebSocketHandler[Map[String, Object]] {

  /**
   * Web socket handler for the connection to the browser.
   */
  private var webSocketFactory: MultipleConnectionWebServerWebSocketHandlerFactory[Map[String, Object]] = null

  /**
   * The web server component.
   */
  private var _webServerComponent: WebServerActivityComponent = null

  abstract override def commonActivitySetup(): Unit = {
    _webServerComponent = addActivityComponent(WebServerActivityComponent.COMPONENT_NAME)

    webSocketFactory = new BasicMultipleConnectionWebServerWebSocketHandlerFactory(this, getLog())
    _webServerComponent.setWebSocketHandlerFactory(webSocketFactory)
  }

  override def addStaticContentHandler(uriPrefix: String ,  baseDir: File): Unit =  {
    _webServerComponent.addStaticContent(uriPrefix, baseDir)
  }
  
  override def addGetRequestHandler(uriPrefix: String, usePath: Boolean,
      handler: HttpGetRequestHandler): Unit = {
    _webServerComponent.addGetRequestHandler(uriPrefix, usePath, handler)
  }

  override def addPostRequestHandler(uriPrefix: String, usePath: Boolean,
      handler: HttpPostRequestHandler): Unit = {
    _webServerComponent.addPostRequestHandler(uriPrefix, usePath, handler)
  }

  override def isWebSocketConnected(): Boolean =  {
    return webSocketFactory.areWebSocketsConnected()
  }

  override def isWebSocketConnected(channelId: String): Boolean =  {
    return webSocketFactory.isWebSocketConnected(channelId)
  }

  override def onNewWebSocketConnection(connectionId: String): Unit =  {
    // Default is nothing to do
  }

  override def onWebSocketClose(connectionId: String ): Unit =  {
    // Default is nothing to do.
  }

  override def onNewWebSocketMessage(connectionId: String , message: Map[String, Object]): Unit =  {
    // Default is to do nothing.
  }

  override def sendWebSocketMessage(connectionId: String , message: Map[String, Object]): Unit =  {
    webSocketFactory.sendMessage(connectionId, message)
  }

  override def sendWebSocketMessage(message: Map[String, Object]): Unit =  {
    webSocketFactory.sendMessage(message)
  }

  override def handleNewWebSocketConnection(connectionId: String ): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onNewWebSocketConnection(connectionId)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def handleNewWebSocketMessage(connectionId: String , message: Map[String, Object]): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onNewWebSocketMessage(connectionId, message)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def handleWebSocketClose(connectionId: String ): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onWebSocketClose(connectionId)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def webServer():  WebServer = {
    webServerComponent.getWebServer()
  }  
  
  override def webServerComponent(): WebServerActivityComponent = {
    _webServerComponent
  }
}