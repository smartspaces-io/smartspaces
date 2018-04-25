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

import io.smartspaces.messaging.codec.MessageDecoder
import io.smartspaces.messaging.codec.MessageCodec

/**
 * Handle HTTP POST requests with the supplied message codec.
 * 
 * @author Keith M. Hughes
 */
abstract class MessageCodecHttpPostRequestHandler[D](
    private val messageCodec: MessageCodec[D, Array[Byte]]) extends HttpPostRequestHandler {

  /**
   * Handle an HTTP request.
   *
   * @param request
   *          the request to handle
   * @param postBody
   *          the post body from the request
   * @param response
   *          the response
   */
  override def handlePostHttpRequest(request: HttpRequest, postBody: HttpPostBody, response: HttpResponse): Unit = {
    val responseData = onHandlePostHttpRequest(request, messageCodec.decode(postBody.getContent), response)
    
    response.getOutputStream.write(messageCodec.encode(responseData))
  }
  
  /**
   * Handle the decoded HTTP request.
   *
   * @param request
   *          the request to handle
   * @param body
   *          the decoded post body from the request
   * @param response
   *          the response
   */
  def onHandlePostHttpRequest(request: HttpRequest , body: D, response:  HttpResponse ): D
}