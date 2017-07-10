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

import io.smartspaces.SmartSpacesException

/**
 * An ordinal value instance is a ordinal value instance that has an order.
 * 
 * @author Keith M. Hughes
 */
trait OrdinalValueInstance extends CategoricalValueInstance with Ordered[OrdinalValueInstance] {
}

abstract class BaseOrdinalValueInstance extends BaseCategoricalValueInstance with OrdinalValueInstance {
  override def compare(that: OrdinalValueInstance) = {
    if (canEqual(that)) {
      this.id - that.id
    } else {
      throw new SmartSpacesException("Cannot compare ordinal value instances")
    }
  }
}

/**
 * A ordinal value instance that was generated dynamically.
 * 
 * @author Keith M. Hughes
 */
final class DynamicOrdinalValueInstance(override val id: Int, override val label: String) extends BaseOrdinalValueInstance {
  private var _value: OrdinalValue[OrdinalValueInstance] = _
  
  override def value(): CategoricalValue[CategoricalValueInstance] = {
    _value
  }
  
  private[entity] def setValue(value: OrdinalValue[OrdinalValueInstance]): Unit = {
    _value = value
  }

}

/**
 * An ordinal value.
 * 
 * @author Keith M. Hughes
 */
trait OrdinalValue[+T <: OrdinalValueInstance]  extends CategoricalValue[T] {
}

/**
 * A base class for ordinal values to simplify implementation.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseOrdinalValue[T <: OrdinalValueInstance](override val name: String, override val values: List[T]) extends OrdinalValue[T] {
    
  /**
   * A map from IDs to the value instance.
   */
  private val idToValue = values.map(v => (v.id, v)).toMap
   
  /**
   * A map from labels to the value instance.
   */
  private val labelToValue = values.map(v => (v.label, v)).toMap

  override def fromId(id: Int): Option[T] = {
    idToValue.get(id)
  }

  override def fromLabel(label: String): Option[T] = {
    labelToValue.get(label)
  }
}

/**
 * A factory class for creating dynamic ordinal values.
 * 
 * @author Keith M. Hughes
 */
object DynamicOrdinalValue {
  
  /**
   * Create a dynamic ordinal value from a set of labels.
   * 
   * @param name
   *        the name of the ordinal value
   * @param labels
   *        the labels for the ordinal value instances
   *        
   * @return the newly created ordinal value
   */
  def fromLabels(name:String, labels: List[String]): DynamicOrdinalValue = {
    val values = labels.zipWithIndex.map { (pair) => new DynamicOrdinalValueInstance(pair._2,pair._1) }
       
    val value = new DynamicOrdinalValue(name, values)
    
    values.foreach { _.setValue(value) }
    
    return value
  }
}

/**
 * A class for dynamic ordinal values.
 * 
 * @author Keith M. Hughes
 */
class DynamicOrdinalValue(_name: String, _values: List[DynamicOrdinalValueInstance]) extends 
    BaseOrdinalValue[DynamicOrdinalValueInstance](_name, _values) {
}
