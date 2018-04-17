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

package io.smartspaces.service.web.server

/**
 * An option request handler that creates no actual content.
 * 
 * This is often used to just send headers to the browser. The headers can be sent in the
 * call that registers the handler.
 * 
 * @author Keith M. Hughes
 */
class NoContentHttpDynamicOptionsRequestHandler extends HttpDynamicOptionsRequestHandler {
  
  override def handleOptionsHttpRequest(request: HttpRequest, response: HttpResponse): Unit = {
    // Do nothing!
  }
}