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

import org.jboss.netty.handler.codec.http.HttpHeaders.addHeader

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.codec.http.HttpVersion
import org.jboss.netty.handler.codec.http.CookieEncoder

import java.io.IOException
import java.net.HttpCookie
import java.util.{ HashMap => JHashMap }
import java.util.{ Map => JMap }
import java.util.{ Set => JSet }
import org.jboss.netty.handler.codec.http.Cookie

abstract class BaseNettyHttpRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean) extends NettyHttpRequestHandler {

  /**
   * The URI prefix to be handled by this handler.
   */
  private[netty] val uriPrefix = {
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

  override def isHandledBy(req: NettyHttpRequest): Boolean = {
    if (usePath) {
      req.getUri.getPath.startsWith(uriPrefix)
    } else {
      req.getUri.getPath == uriPrefix
    }
  }

  def writeSuccessHttpResponse(request: NettyHttpRequest, response: NettyHttpResponse): Unit = {
    if (!response.isResponseWritten()) {
      response.prepareForWrite();
      
      val res = new DefaultHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.valueOf(response.getResponseCode()))
      if (response.hasContentForWriting()) {
        res.setContent(response.getChannelBuffer())
      }

      val contentType = response.getContentType()
      if (contentType != null) {
        addHeader(res, HttpHeaders.Names.CONTENT_TYPE, contentType)
      }

      parentHandler.addHttpResponseHeaders(res, response)

      parentHandler.sendHttpResponse(response.getChannelHandlerContext, 
          request.getUnderlyingRequest(), res, true, false)
    }
  }

  def writeErrorHttpResponse(req: NettyHttpRequest, ctx: ChannelHandlerContext, e: Throwable): Unit = {
    parentHandler.getWebServer().getLog().error(
      s"Error while handling dynamic web server ${req.getMethod} request ${req.getUri()}", e)

    parentHandler.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR)

  }
}