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

package io.smartspaces.util.web;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Useful web constants.
 *
 * @author Keith M. Hughes
 */
public class HttpConstants {

  /**
   * The separator between URL path components.
   */
  public static final String URL_PATH_COMPONENT_SEPARATOR = "/";

  /**
   * The URL prefix for an HTTP request.
   */
  public static final String HTTP_URL_PREFIX = "http://";

  /**
   * The separator for a URL port designator.
   */
  public static final String URL_PORT_SEPARATOR = ":";

  /**
   * The separator for a URL query string.
   */
  public static final String URL_QUERY_STRING_SEPARATOR = "?";

  /**
   * The separator for a URL query string between a name and its value.
   */
  public static final String URL_QUERY_NAME_VALUE_SEPARATOR = "=";

  /**
   * Empty map used when represending to additional headers.
   */
  public static final Map<String, String> EMPTY_HEADER_MAP = ImmutableMap.of();

  /**
   * Header key for origin access.
   */
  public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

  /**
   * Origin value for all origins.
   */
  public static final String HEADER_VALUE_ACCESS_CONTROL_ORIGIN_WILDCARD = "*";

  /**
   * The authorization header.
   */
  public static final String HEADER_AUTHORIZATION = "authorization";

  /**
   * The authentication header value prefix for basic authorization.
   */
  public static final String HEADER_VALUE_PREFIX_AUTHORIZATION_BASIC = "basic";
}
