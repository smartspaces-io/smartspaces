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

import java.util.{ Map => JMap }

import io.smartspaces.service.web.server.HttpOptionsRequestHandler

/**
 * A Netty handler for {@link NettyHttpOptionsRequestHandler}.
 *
 * @author Keith M. Hughes
 */
class NettyHttpOptionsRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean,
  requestHandler: HttpOptionsRequestHandler,
  extraHttpContentHeaders: JMap[String, String]) extends 
  BaseNettyHttpRequestHandlerHandler(parentHandler, uriPrefixBase, usePath) with NettyHttpOptionsRequestHandler {

  override def handleWebRequest(request: NettyHttpRequest, response: NettyHttpResponse): Unit = {
    try {
      response.addContentHeaders(extraHttpContentHeaders)
      requestHandler.handleOptionsHttpRequest(request, response)

      writeSuccessHttpResponse(request, response)

      parentHandler.getWebServer().getLog()
        .debug(s"Dynamic HTTP GET content handler for ${uriPrefix} completed")
    } catch {
      case e: Throwable =>
        writeErrorHttpResponse(request, response.getChannelHandlerContext, e)
    }
  }
}
