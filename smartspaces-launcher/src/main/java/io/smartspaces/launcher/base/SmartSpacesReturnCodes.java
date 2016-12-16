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

package io.smartspaces.launcher.base;

/**
 * Return codes from the SmartSpaces runtime.
 * 
 * @author Keith M. Hughes
 */
public class SmartSpacesReturnCodes {

  /**
   * Smart Spaces is returning success.
   */
  public static final int RETURN_CODE_SUCCESS = 0;

  /**
   * Smart Spaces is returning that it has failed.
   */
  public static final int RETURN_CODE_FAILURE = 1;

  /**
   * Smart Spaces is returning that it wants to be restarted.
   */
  public static final int RETURN_CODE_RESTART_HARD = 10;

  /**
   * Smart Spaces is returning that it wants to be restarted softly.
   * 
   * <p>
   * This is a pseudo return code, it is never returned from the process.
   */
  public static final int RETURN_CODE_RESTART_SOFT = -1;

}
