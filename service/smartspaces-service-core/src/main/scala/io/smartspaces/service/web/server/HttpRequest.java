/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

import io.smartspaces.logging.ExtendedLog;

import com.google.common.collect.Multimap;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * An HTTP request coming into the server.
 *
 * @author Keith M. Hughes
 */
public interface HttpRequest {
  
  /**
   * Get the remote address for the request.
   * 
   * @return the remote address for the request
   */
  SocketAddress getRemoteAddress();
  
  /**
   * Get the HTTP method for the request.
   * 
   * @return the HTTP method
   */
  String getMethod();

  /**
   * Get the URI of the request.
   *
   * @return the URI of the request.
   */
  URI getUri();

  /**
   * Get the query parameters from the URI.
   *
   * @return the query parameters
   */
  Map<String, String> getUriQueryParameters();

  /**
   * Get the logger for this request.
   *
   * @return the logger to use
   */
  ExtendedLog getLog();

  /**
   * Get the header for this request
   *
   * @return the header for the http message
   */
  Multimap<String, String> getHeaders();

  /**
   * Return the set of header strings for the given key.
   *
   * @param key
   * @return
   */
  Set<String> getHeader(String key);

  /**
   * Return the cookie which has the given name, if it exists.
   *
   * @param name
   * @return
   */
  HttpCookie getCookie(String name);

  /**
   * Return a map of all cookie values set on the request.
   *
   * @return
   */
  Set<HttpCookie> getCookies();
}
