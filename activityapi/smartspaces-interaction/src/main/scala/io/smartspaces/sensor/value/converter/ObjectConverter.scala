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

package io.smartspaces.sensor.value.converter

/**
 * Converts from one object type to another object type.
 * 
 * @param [F]
 * 		the type converting from
 * @param [T]
 * 		the type converting to
 * 
 * @author Keith M. Hughes
 */
trait ObjectConverter[F,T] {
  
  /**
   * Convert a value.
   * 
   * @param value
   * 			the value to convert from
   * 
   * @returns the value converted to
   */
  def convert(value: F) : T
}
