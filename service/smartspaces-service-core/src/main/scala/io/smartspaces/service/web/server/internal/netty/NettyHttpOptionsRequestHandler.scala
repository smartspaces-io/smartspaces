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

/**
 * Handle HTTP GET requests from Netty.
 *
 * @author Keith M. Hughes
 */
trait NettyHttpOptionsRequestHandler extends NettyHttpRequestHandler {

  /**
   * Handle the web request.
   *
   * @param request
   *          the Netty HTTP request
   * @param response
   *          the Netty HTTP response
   *
   * @throws IOException
   *           something bad happened
   */
  def handleWebRequest(request: NettyHttpRequest, response: NettyHttpResponse): Unit
}
