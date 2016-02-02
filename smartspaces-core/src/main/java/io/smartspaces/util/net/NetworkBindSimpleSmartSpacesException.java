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

package io.smartspaces.util.net;

import io.smartspaces.SimpleSmartSpacesException;

/**
 * An exception happened while attempting a network bind.
 *
 * @author Keith M. Hughes
 */
public class NetworkBindSimpleSmartSpacesException extends SimpleSmartSpacesException {

  /**
   * Create a simple exception using the given message.
   *
   * @param message
   *          message for exception
   */
  public NetworkBindSimpleSmartSpacesException(String message) {
    super(message);
  }

  /**
   * Create a simple exception with message and cause.
   *
   * @param message
   *          message for exception
   * @param cause
   *          underlying cause of exception
   */
  public NetworkBindSimpleSmartSpacesException(String message, Throwable cause) {
    super(message, cause);
  }
}
