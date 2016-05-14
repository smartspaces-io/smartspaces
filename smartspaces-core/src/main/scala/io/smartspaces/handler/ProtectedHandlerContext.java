/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.handler;

/**
 * A context for protected handler calls.
 * 
 * @author Keith M. Hughes
 */
public interface ProtectedHandlerContext {

  /**
   * Can the handler run in the current context?
   * 
   * @return {@code true} if the handler can run
   */
  boolean canHandlerRun();

  /**
   * Begin the handler processing.
   */
  void enterHandler();

  /**
   * End the handler processing.
   */
  void exitHandler();

  /**
   * Handle an error for this component. Includes basic logging, and then
   * passing off to the activity for any activity-specific processing.
   *
   * @param message
   *          error message text
   * @param t
   *          triggering throwable or {@code null}
   */
  void handleHandlerError(String message, Throwable t);
}
