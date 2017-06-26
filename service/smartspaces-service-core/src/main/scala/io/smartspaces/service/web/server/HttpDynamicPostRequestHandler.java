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

package io.smartspaces.service.web.server;

/**
 * A handler for dynamic POST requests.
 *
 * @author Keith M. Hughes
 */
public interface HttpDynamicPostRequestHandler {

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
  void handle(HttpRequest request, HttpPostBody postBody, HttpResponse response);
}
