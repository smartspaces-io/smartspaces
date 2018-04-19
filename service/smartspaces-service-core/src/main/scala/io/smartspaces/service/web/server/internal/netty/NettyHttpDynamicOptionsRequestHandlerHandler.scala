/*
 * Copyright (C) 2018 Keith M. Hughes
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

import java.net.HttpCookie
import java.util.{ HashMap => JHashMap }
import java.util.{ Map => JMap }
import java.util.{ Set => JSet }

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpHeaders.addHeader
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion

import io.smartspaces.service.web.server.HttpDynamicOptionsRequestHandler

/**
 * A Netty handler for {@link NettyHttpOptionsRequestHandler}.
 *
 * @author Keith M. Hughes
 */
class NettyHttpDynamicOptionsRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean,
  requestHandler: HttpDynamicOptionsRequestHandler,
  extraHttpContentHeaders: JMap[String, String]) extends
  BaseNettyHttpDynamicRequestHandlerHandler(parentHandler, uriPrefixBase, usePath, extraHttpContentHeaders) with NettyHttpOptionsRequestHandler {

  override def handleWebRequest(ctx: ChannelHandlerContext, request: NettyHttpRequest,
    cookiesToAdd: JSet[HttpCookie]): Unit = {
    val response = newNettyHttpResponse(ctx, cookiesToAdd)

    try {
      requestHandler.handleOptionsHttpRequest(request, response)

      writeSuccessHttpResponse(ctx, request, response)

      parentHandler.getWebServer().getLog()
        .debug(s"Dynamic HTTP GET content handler for ${uriPrefix} completed")
    } catch {
      case e: Throwable =>
         writeErrorHttpResponse(request, ctx, e)
   }
  }
}
