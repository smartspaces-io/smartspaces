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

package io.smartspaces.activity.behavior.general

import io.smartspaces.activity.ActivityBehavior
import java.util.Map


/**
 * An activity behavior that adds JSON support.
 * 
 * @author Keith M. Hughes
 */
trait JsonActivityBehavior extends ActivityBehavior {
    
  /**
   * Convert a map to a JSON string.
   *
   * @param map
   *          the map to convert to a string
   *
   * @return the JSON string representation of the map
   */
  def jsonStringify(map: Map[String, Object]): String

  /**
   * Parse a JSON string and return the map.
   *
   * @param data
   *          the JSON string
   *
   * @return the map for the string
   */
   def jsonParse(data: String): Map[String, Object]
}