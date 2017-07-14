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
 * The standard implementation of a value registry.
 *
 * @author Keith M. Hughes
 */
object StandardValueRegistry extends ValueRegistry {

  /**
   * The mapping of names to the categorical variable for that name.
   */
  private var categoricalValues: Map[String, CategoricalValue[CategoricalValueInstance]] = Map()

  override def registerCategoricalValue(value: CategoricalValue[CategoricalValueInstance]): ValueRegistry = {

    synchronized {
      categoricalValues = categoricalValues + (value.name -> value)
    }

    this
  }

  override def registerCategoricalValues(values: CategoricalValue[CategoricalValueInstance]*): ValueRegistry = {

    synchronized {
      values.foreach(value =>
        categoricalValues = categoricalValues + (value.name -> value))
    }

    this
  }

  /**
   * Get a categorical value by name.
   *
   * @param name
   *       the name of the categorical value
   */
  override def getCategoricalValue[T <: CategoricalValueInstance](name: String): Option[CategoricalValue[T]] = {
    categoricalValues.get(name).asInstanceOf[Option[CategoricalValue[T]]]
  }
}