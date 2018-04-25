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

import io.smartspaces.util.web.HttpResponseCode
import io.smartspaces.util.web.HttpConstants
import com.google.common.base.Charsets

/**
 * A collection of canned HTTP responses.
 *
 * @author Keith M. Hughes
 */
object HttpResponses {

  /**
   * Make the response into a redirect response.
   *
   * @param response
   *        the response to make a redirect
   * @param redirectUrl
   *        the URL to redirect to
   */
  def redirectResponse(response: HttpResponse, redirectUrl: String): Unit = {
    response.setResponseCode(HttpResponseCode.FOUND)
    response.addContentHeader(HttpConstants.HEADER_NAME_LOCATION, redirectUrl)
  }

  /**
   * Send a string response.
   * 
   * @param response
   *        the HTTP response
   * @param content
   *        the content to send
   * @param contentType
   *        the type of the content
   */
  def stringResponse(response: HttpResponse, content: String, contentType: String): Unit = {
    response.setContentType(contentType)
    val os = response.getOutputStream
    os.write(content.getBytes(Charsets.UTF_8))
    os.flush
  }
}