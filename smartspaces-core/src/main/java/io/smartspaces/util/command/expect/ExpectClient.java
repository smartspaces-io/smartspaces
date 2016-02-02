/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.util.command.expect;

import io.smartspaces.SmartSpacesException;

/**
 * A client for connecting to a resource and using an Expect session to
 * communicate with the resource.
 *
 * @author Keith M. Hughes
 */
public interface ExpectClient {

  /**
   * Connect to the remote device and prepare for communication.
   *
   * @throws SmartSpacesException
   *           something else bad happened
   */
  void connect() throws SmartSpacesException;

  /**
   * Disconnect from remote connection.
   *
   * <p>
   * Once disconnected, this client cannot be used again.
   */
  void disconnect();

  /**
   * Expect a particular string.
   *
   * @param expectedString
   *          the string to expect, can be a Java regex expression
   *
   * @throws SmartSpacesException
   *           no match was found, the client was not logged in, or something
   *           else bad happened
   */
  void expect(String expectedString) throws SmartSpacesException;

  /**
   * Send content to the remote system.
   *
   * <p>
   * A newline is appended.
   *
   * @param content
   *          the content to send
   *
   * @throws SmartSpacesException
   *           the client was not logged in, or something else bad happened
   */
  void sendLn(String content) throws SmartSpacesException;

  /**
   * Send content to the remote system.
   *
   * @param content
   *          the content to send
   *
   * @throws SmartSpacesException
   *           the client was not logged in, or something else bad happened
   */
  void send(String content) throws SmartSpacesException;

  /**
   * Get the timeout for content from the remote.
   *
   * @return the timeout, in milliseconds
   */
  int getTargetTimeout();

  /**
   * Set the timeout for content from the remote.
   *
   * @param targetTimeout
   *          the timeout, in milliseconds
   */
  void setTargetTimeout(int targetTimeout);
}