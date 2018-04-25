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

package io.smartspaces.service.web.server.internal.netty

import io.smartspaces.service.web.server.HttpPostBody

/**
 * Handle HTTP POST requests from Netty.
 *
 * @author Keith M. Hughes
 */
trait NettyHttpPostRequestHandler extends NettyHttpRequestHandler {

  /**
   * Handle the web request.
   *
   * @param request
   *          the Netty HTTP request
   * @param postBody
   *          the HTTP post body
   * @param response
   *          the Netty HTTP response
   *
   * @throws IOException
   *           something bad happened
   */
  def handleWebRequest(request: NettyHttpRequest, postBody: HttpPostBody, response: NettyHttpResponse): Unit
}
