/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.service.web.server.internal.netty;

import org.jboss.netty.handler.codec.http.HttpHeaders.addHeader

import io.smartspaces.service.web.server.HttpDynamicPostRequestHandler
import io.smartspaces.service.web.server.HttpPostBody

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
import java.util.{ Set => JSet }

/**
 * A Netty handler for {@link NettyHttpPostRequestHandler}.
 *
 * @author Keith M. Hughes
 */
class NettyHttpDynamicPostRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean,
  requestHandler: HttpDynamicPostRequestHandler,
  _extraHttpContentHeaders: JMap[String, String]) extends NettyHttpPostRequestHandler {

  /**
   * The URI prefix to be handled by this handler.
   */
  private val uriPrefix = {
    val uriPrefix = new StringBuilder()
    if (!uriPrefixBase.startsWith("/")) {
      uriPrefix.append('/')
    }
    uriPrefix.append(uriPrefixBase)
    if (usePath && !uriPrefixBase.endsWith("/")) {
      uriPrefix.append('/')
    }
    uriPrefix.toString()
  }

  /**
   * Extra headers to add to the response.
   */
  private val extraHttpContentHeaders: JMap[String, String] = new JHashMap()
  if (_extraHttpContentHeaders != null) {
    extraHttpContentHeaders.putAll(_extraHttpContentHeaders)
  }

  override def isHandledBy(req: HttpRequest): Boolean = {
    req.getUri().startsWith(uriPrefix)
  }

  override def handleWebRequest(ctx: ChannelHandlerContext, nettyRequest: HttpRequest,
    postBody: HttpPostBody, cookiesToAdd: JSet[HttpCookie]): Unit = {
    val request = new NettyHttpRequest(
      nettyRequest,
      ctx.getChannel().getRemoteAddress(), parentHandler.getWebServer().getLog())
    val response = new NettyHttpResponse(ctx, extraHttpContentHeaders)
    response.addCookies(cookiesToAdd)

    //DefaultHttpResponse res
    try {
      requestHandler.handlePost(request, postBody, response)

      val res = new DefaultHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.valueOf(response.getResponseCode()))
      res.setContent(response.getChannelBuffer())

      val contentType = response.getContentType()
      if (contentType != null) {
        addHeader(res, HttpHeaders.Names.CONTENT_TYPE, contentType)
      }

      parentHandler.addHttpResponseHeaders(res, response.getContentHeaders())
      parentHandler.sendHttpResponse(ctx, nettyRequest, res, true, false)

      parentHandler.getWebServer().getLog()
        .formatDebug("Dynamic content handler for %s completed", uriPrefix)
    } catch {
      case e: Throwable =>
        parentHandler.getWebServer().getLog().error(s"Error while handling dynamic web server POST request ${nettyRequest.getUri()}", e)

        parentHandler.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    }
  }
}
