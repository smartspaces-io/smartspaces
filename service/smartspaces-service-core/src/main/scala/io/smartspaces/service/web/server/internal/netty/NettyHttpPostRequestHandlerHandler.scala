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

import java.util.{ Map => JMap }

import io.smartspaces.service.web.server.HttpPostBody
import io.smartspaces.service.web.server.HttpPostRequestHandler

/**
 * A Netty handler for {@link NettyHttpPostRequestHandler}.
 *
 * @author Keith M. Hughes
 */
class NettyHttpPostRequestHandlerHandler(
  parentHandler: NettyWebServerHandler,
  uriPrefixBase: String,
  usePath: Boolean,
  requestHandler: HttpPostRequestHandler,
  extraHttpContentHeaders: JMap[String, String]) extends BaseNettyHttpRequestHandlerHandler(parentHandler, uriPrefixBase, usePath) with NettyHttpPostRequestHandler {

  override def handleWebRequest(
    request: NettyHttpRequest,
    postBody: HttpPostBody, response: NettyHttpResponse): Unit = {
    try {
      response.addContentHeaders(extraHttpContentHeaders)
      requestHandler.handlePostHttpRequest(request, postBody, response)

      writeSuccessHttpResponse(request, response)

      parentHandler.getWebServer().getLog()
        .debug(s"HTTP POST content handler for ${uriPrefix} completed")
    } catch {
      case e: Throwable =>
        writeErrorHttpResponse(request, response.getChannelHandlerContext, e)
    }
  }
}
