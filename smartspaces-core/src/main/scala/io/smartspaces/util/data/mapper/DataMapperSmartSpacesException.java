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

package io.smartspaces.util.data.mapper;

import io.smartspaces.SmartSpacesException;

/**
 * Exception for data mapper operations.
 *
 * @author Keith M. Hughes
 */
public class DataMapperSmartSpacesException extends SmartSpacesException {

  /**
   * Construct a new exception.
   *
   * @param message
   *          the message for the exception
   */
  public DataMapperSmartSpacesException(String message) {
    super(message);
  }

  /**
   * Construct a new exception.
   *
   * @param message
   *          the message for the exception
   * @param e
   *          an exception created during the JSON operation
   */
  public DataMapperSmartSpacesException(String message, Throwable e) {
    super(message, e);
  }
}
