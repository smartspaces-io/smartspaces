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

package io.smartspaces.data.entity

/**
 * A registry for all values, categorical and otherwise.
 * 
 * @author Keith M. Hughes
 */
trait ValueRegistry {
  
  /**
   * Register a categorical value with the registry.
   * 
   * @param value
   *       the value to add
   * 
   * @return this registry
   */
  def registerCategoricalValue(value: CategoricalValue[CategoricalValueInstance]): ValueRegistry
  
  /**
   * Get a categorical value by name.
   * 
   * @param name
   *       the name of the categorical value
   */
  def getCategoricalValue[T <: CategoricalValueInstance](name: String): Option[CategoricalValue[T]]
}