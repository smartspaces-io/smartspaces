/*
 * Copyright (C) 2014 Keith M. Hughes
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

package io.smartspaces.service.web;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.resource.managed.ManagedResource;

import java.nio.charset.Charset;

/**
 * Copy content from an HTTP URL to a file.
 *
 * <p>
 * It is safe to have multiple threads copying content.
 *
 * @author Keith M. Hughes
 */
public interface RestWebClient extends ManagedResource {

  /**
   * See {@link #performGet(String, Charset)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performGet(String sourceUri) throws SmartSpacesException;

  /**
   * Get the content of the source URI and return as a string.
   *
   * <p>
   * This method blocks until the transfer is complete or it fails.
   *
   * <p>
   * This method will fail if there are not enough connections available and
   * blocking until a connection becomes ready is not enabled.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param charset
   *          the charset the content will be in
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performGet(String sourceUri, Charset charset) throws SmartSpacesException;

  /**
   * See {@link #performGet(String, Charset)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPut(String sourceUri, String putContent) throws SmartSpacesException;

  /**
   * Get the content of the source URI and return as a string.
   *
   * <p>
   * This method blocks until the transfer is complete or it fails.
   *
   * <p>
   * This method will fail if there are not enough connections available and
   * blocking until a connection becomes ready is not enabled.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param putContent
   *          the content to put to the remote
   * @param charset
   *          the charset the content will be in
   * 
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPut(String sourceUri, String putContent, Charset charset)
      throws SmartSpacesException;

  /**
   * Get the total number of simultaneous connections allowed.
   *
   * @return the total number of connections, or {@code 0} if there is no limit
   */
  int getTotalConnectionsAllowed();
}
