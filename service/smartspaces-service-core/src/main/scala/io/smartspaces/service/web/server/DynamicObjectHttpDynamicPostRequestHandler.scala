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

import io.smartspaces.messaging.codec.MapByteArrayMessageCodec
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator

/**
 * An HTTP dynamic POST handler for Dynamic Object messages.
 * 
 * @author Keith M. Hughes
 */
abstract class DynamicObjectHttpDynamicPostRequestHandler extends HttpDynamicPostRequestHandler {

  private val messageCodec = new MapByteArrayMessageCodec
  
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
  override def handle(request: HttpRequest, postBody: HttpPostBody, response: HttpResponse): Unit = {
    val responseData = handle(request, new StandardDynamicObjectNavigator(messageCodec.decode(postBody.getContent)), response)
    
    val outputStream = response.getOutputStream
    outputStream.write(messageCodec.encode(responseData.toMap()))
    outputStream.flush()
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
  def handle(request: HttpRequest , body: DynamicObject, response:  HttpResponse ): DynamicObjectBuilder
}