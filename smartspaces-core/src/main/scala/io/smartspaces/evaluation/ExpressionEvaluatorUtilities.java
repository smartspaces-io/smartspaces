/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.evaluation;

/**
 * A collection of methods that define various aspects of evaluation.
 * 
 * @author Keith M. Hughes
 */
public class ExpressionEvaluatorUtilities {

  /**
   * How does the object evaluate as a boolean?
   * 
   * @param obj
   *          the object to test
   * 
   * @return {@code true} if the object evaluates to our definition of true
   */
  public static boolean isTrue(Object obj) {
    if (obj instanceof Boolean) {
      return (Boolean) obj;
    } else if (obj instanceof String) {
      return Boolean.parseBoolean((String) obj);
    } else {
      return false;
    }
  }
}
