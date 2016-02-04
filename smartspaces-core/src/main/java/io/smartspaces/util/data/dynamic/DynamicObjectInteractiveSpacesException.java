/*
 * Copyright (C) 2014 Keith M. Hughes.
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

package io.smartspaces.util.data.dynamic;

import io.smartspaces.SimpleSmartSpacesException;

/**
 * An exception for dynamic object.
 *
 * @author Keith M. Hughes
 */
public class DynamicObjectInteractiveSpacesException extends SimpleSmartSpacesException {

  /**
   * Construct a new dynamic object exception.
   *
   * @param message
   *          the message to send
   */
  public DynamicObjectInteractiveSpacesException(String message) {
    super(message);
  }
}
