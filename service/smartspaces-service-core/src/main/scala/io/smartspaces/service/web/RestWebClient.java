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
import java.util.Map;

/**
 * A REST web client.
 *
 * <p>
 * It is safe to have multiple threads using the client.
 *
 * @author Keith M. Hughes
 */
public interface RestWebClient extends ManagedResource {

  /**
   * Set the keep alive default time.
   *
   * <p>
   *   The default is 0 milliseconds.
   * </p>
   *
   * @param keepAliveDefault
   *        the keep alive time, in milliseconds
   */
  void setKeepAliveTimeDefault(long keepAliveDefault);

  /**
   * See {@link #performGet(String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performGet(String sourceUri, Map<String,String> headers) throws SmartSpacesException;

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
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performGet(String sourceUri, Charset charset, Map<String,String> headers) throws SmartSpacesException;

  /**
   * See {@link #performGetFull(String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performGetFull(String sourceUri, Map<String,String> headers) throws SmartSpacesException;

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
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performGetFull(String sourceUri, Charset charset, Map<String,String> headers) throws SmartSpacesException;

  /**
   * See {@link #performPut(String, String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param putContent
   *          the content to post
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPut(String sourceUri, String putContent, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Put the content of the source URI and return as a string.
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
   *          the content to put
   * @param charset
   *          the charset the content will be in
   * @param headers
   *          a map of headers for the request, can be {@code null}
   * 
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPut(String sourceUri, String putContent, Charset charset, Map<String,String> headers)
      throws SmartSpacesException;

  /**
   * See {@link #performPutFull(String, String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param putContent
   *          the content to post
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performPutFull(String sourceUri, String putContent, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Put the content of the source URI and return as a string.
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
   *          the content to put
   * @param charset
   *          the charset the content will be in
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performPutFull(String sourceUri, String putContent, Charset charset, Map<String,String> headers)
      throws SmartSpacesException;

  /**
   * See {@link #performDelete(String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performDelete(String sourceUri, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Delete the content of the source URI and return as a string.
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
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if delete was not successful
   */
  String performDelete(String sourceUri, Charset charset, Map<String,String> headers)
          throws SmartSpacesException;

  /**
   * See {@link #performDeleteFull(String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performDeleteFull(String sourceUri, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Delete the content of the source URI and return as a string.
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
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if delete was not successful
   */
  RestWebClientResponse performDeleteFull(String sourceUri, Charset charset, Map<String,String> headers)
      throws SmartSpacesException;

  /**
   * See {@link #performPost(String, String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param postContent
   *          the content to post to the remote
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPost(String sourceUri, String postContent, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Post the content to the source URI and return as a string.
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
   * @param postContent
   *          the content to post to the remote
   * @param charset
   *          the charset the content will be in
   * @param headers
   *          a map of headers for the request, can be {@code null}
   * 
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  String performPost(String sourceUri, String postContent, Charset charset, Map<String,String> headers)
      throws SmartSpacesException;

  /**
   * See {@link #performPostFull(String, String, Charset, Map)}.
   *
   * <p>
   * The charset will be UTF-8.
   *
   * @param sourceUri
   *          the URI to copy the content from
   * @param postContent
   *          the content to post to the remote
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performPostFull(String sourceUri, String postContent, Map<String,String> headers) throws SmartSpacesException;

  /**
   * Post the content to the source URI and return as a string.
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
   * @param postContent
   *          the content to post to the remote
   * @param charset
   *          the charset the content will be in
   * @param headers
   *          a map of headers for the request, can be {@code null}
   *
   * @return the content
   *
   * @throws SmartSpacesException
   *           if transfer was not successful
   */
  RestWebClientResponse performPostFull(String sourceUri, String postContent, Charset charset, Map<String,String> headers)
      throws SmartSpacesException;

  /**
   * Get the total number of simultaneous connections allowed.
   *
   * @return the total number of connections, or {@code 0} if there is no limit
   */
  int getTotalConnectionsAllowed();
}
