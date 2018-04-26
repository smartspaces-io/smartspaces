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
import java.util.{Map => JMap};
import java.util.{Set => JSet};

/**
 * An HTTP request coming into the server.
 *
 * @author Keith M. Hughes
 */
trait HttpRequest {
  
  /**
   * Get the remote address for the request.
   * 
   * @return the remote address for the request
   */
  def getRemoteAddress(): SocketAddress 
  
  /**
   * Get the HTTP method for the request.
   * 
   * @return the HTTP method
   */
  def getMethod(): String 

  /**
   * Get the URI of the request.
   *
   * @return the URI of the request.
   */
  def getUri(): URI 

  /**
   * Get the query parameters from the URI.
   *
   * @return the query parameters
   */
   def getUriQueryParameters(): JMap[String, String]

  /**
   * Get the logger for this request.
   *
   * @return the logger to use
   */
  def getLog(): ExtendedLog 

  /**
   * Get the header for this request
   *
   * @return the header for the http message
   */
   def getHeaders(): Multimap[String, String]

  /**
   * Return the set of header strings for the given key.
   *
   * @param name
   *        the name of the header
   *        
   * @return all values for the header
   */
   def getHeader(key: String ): JSet[String]

  /**
   * Return the cookie which has the given name, if it exists.
   *
   * @param name
   *        name of the cookie
   *        
   * @return value of the cookie
   */
   def getCookie(name: String ): HttpCookie

  /**
   * Return a set of all cookie values set on the request.
   *
   * @return
   */
   def getCookies(): JSet[HttpCookie]
  
  /**
   * Get a value from the session.
   * 
   * @param valueName
   *        the name of the value
   *        
   * @return the value, if found
   */
  def getValue[T](valueName: String): T
  
  /**
   * Set a value in the session.
   * 
   * @param valueName
   *        the name of the value
   * @param valueValue
   *        the value of the value
   *        
   * @return this session
   */
  def setValue(valueName: String, valueValue: Any): HttpRequest
}
