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

package io.smartspaces.service.web.server.internal.netty

import org.jboss.netty.handler.codec.http.HttpHeaders.addHeader

import io.smartspaces.service.web.server.HttpGetRequestHandler

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion

import java.io.IOException
import java.net.HttpCookie
import java.util.{ HashMap => JHashMap }
import java.util.{ Map => JMap }

/**
 * A Netty handler for {@link NettyHttpGetRequestHandler}.
 *
 * @author Keith M. Hughes
 */
class NettyHttpGetRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean,
  requestHandler: HttpGetRequestHandler,
  extraHttpContentHeaders: JMap[String, String]) extends 
  BaseNettyHttpRequestHandlerHandler(parentHandler, uriPrefixBase, usePath) with NettyHttpGetRequestHandler {

  override def handleWebRequest(request: NettyHttpRequest, response: NettyHttpResponse): Unit = {
    try {
      response.addContentHeaders(extraHttpContentHeaders)
      requestHandler.handleGetHttpRequest(request, response)

      writeSuccessHttpResponse(request, response)

      parentHandler.getWebServer().getLog()
        .debug(s"Dynamic HTTP GET content handler for ${uriPrefix} completed")
    } catch {
      case e: Throwable =>
        writeErrorHttpResponse(request, response.getChannelHandlerContext, e)
    }
  }
}
