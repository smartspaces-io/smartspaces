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

import io.smartspaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory
import io.smartspaces.util.data.json.StandardJsonMapper
import io.smartspaces.service.web.server.HttpFileUploadListener
import io.smartspaces.service.web.server.MultipleConnectionWebSocketHandler
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.activity.behavior.general.StandardActivityJson
import io.smartspaces.service.web.server.HttpFileUpload
import io.smartspaces.activity.component.web.WebServerActivityComponent
import io.smartspaces.service.web.server.WebServer
import java.io.File

/**
 * An activity behavior for web server support.
 *
 * <p>
 * This behavior uses the registered {@link WebServerActivityComponent.COMPONENT_NAME} activity component.
 *
 * @author Keith M. Hughes
 */
trait StandardActivityWebServer extends WebServerActivityBehavior with StandardActivityJson with MultipleConnectionWebSocketHandler with HttpFileUploadListener{

  /**
   * The JSON mapper.
   */
  private val  MAPPER: JsonMapper = StandardJsonMapper.INSTANCE

  /**
   * Web socket handler for the connection to the browser.
   */
  private var webSocketFactory: io.smartspaces.service.web.server.MultipleConnectionWebServerWebSocketHandlerFactory = null

  /**
   * The web server component.
   */
  private var webServerComponent: WebServerActivityComponent = null

  abstract override def commonActivitySetup(): Unit = {
    webServerComponent = addActivityComponent(WebServerActivityComponent.COMPONENT_NAME);

    webSocketFactory = new BasicMultipleConnectionWebServerWebSocketHandlerFactory(this, getLog());
    webServerComponent.setWebSocketHandlerFactory(webSocketFactory);
    webServerComponent.setHttpFileUploadListener(this);
  }

  override def addStaticContent(uriPrefix: String ,  baseDir: File): Unit =  {
    webServerComponent.addStaticContent(uriPrefix, baseDir);
  }

  override def isWebSocketConnected(): Boolean =  {
    return webSocketFactory.areWebSocketsConnected();
  }

  override def onNewWebSocketConnection(connectionId: String): Unit =  {
    // Default is nothing to do
  }

  override def onWebSocketClose(connectionId: String ): Unit =  {
    // Default is nothing to do.
  }

  override def onWebSocketReceive(connectionId: String , data: Object): Unit =  {
    // Default is to do nothing.
  }

  override def sendWebSocketJson(connectionId: String , data: Object ): Unit =  {
    webSocketFactory.sendJson(connectionId, data)
  }

  override def sendAllWebSocketJson(data: Object): Unit =  {
    webSocketFactory.sendJson(data)
  }

  override def sendWebSocketString(connectionId: String, data: String): Unit =  {
    webSocketFactory.sendString(connectionId, data)
  }

  override def sendAllWebSocketString(data: String ): Unit =  {
    webSocketFactory.sendString(data)
  }

  override def handleNewWebSocketConnection(connectionId: String ): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onNewWebSocketConnection(connectionId)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def handleWebSocketReceive(connectionId: String ,  data: Object): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onWebSocketReceive(connectionId, data)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def handleWebSocketClose(connectionId: String ): Unit = {
    val invocation = getExecutionContext().enterMethod();

    try {
      onWebSocketClose(connectionId)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  override def handleHttpFileUpload(fileUpload: HttpFileUpload ): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onHttpFileUpload(fileUpload)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  /**
   * A file upload has happened.
   *
   * <p>
   * This method should be overridden if it should be handled.
   *
   * @param fileUpload
   *          the file upload
   */
  def onHttpFileUpload(fileUpload: HttpFileUpload ): Unit = {
    // The default is do nothing.
  }

  override def getWebServer():  WebServer = {
    webServerComponent.getWebServer()
  }  
  
  override def getWebServerComponent(): WebServerActivityComponent = {
    webServerComponent
  }
}