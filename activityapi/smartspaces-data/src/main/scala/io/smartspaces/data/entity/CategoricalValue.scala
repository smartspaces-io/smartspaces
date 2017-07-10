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
 * An instance of an individual categorical value.
 *
 * @author Keith M. Hughes
 */
trait CategoricalValueInstance extends Equals {

  /**
   * The ID of the value instance.
   */
  val id: Int

  /**
   * The label for the instance.
   */
  val label: String
  
  /**
   * Get the value this instance is an instance of.
   * 
   * @return the value
   */
  def value(): CategoricalValue[CategoricalValueInstance]
}

/**
 * A base class for categorical value instances that supplies some needed standard implementations.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseCategoricalValueInstance extends CategoricalValueInstance {

  override def canEqual(other: Any) = {
    other.isInstanceOf[CategoricalValueInstance] && (other.asInstanceOf[CategoricalValueInstance].value eq value)
  }

  override def equals(other: Any): Boolean = {
    other match {
      case that: CategoricalValueInstance =>
        if (this eq that) true else {
          (that.## == this.##) &&
            (that canEqual this) &&
            (id == that.id)
        }
      case _ => false
    }
  }

  override def hashCode(): Int = {
    id.hashCode()
  }
  
  override def toString(): String = {
    s"${this.getClass.getName}[id=(${this.id}, label=${this.label}]"
  }
}

/**
 * A categorical value instance that was generated dynamically.
 * 
 * @author Keith M. Hughes
 */
final class DynamicCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
  private var _value: CategoricalValue[CategoricalValueInstance] = _
  
  override def value(): CategoricalValue[CategoricalValueInstance] = {
    _value
  }
  
  private[entity] def setValue(value: CategoricalValue[CategoricalValueInstance]): Unit = {
    _value = value
  }

}

/**
 * A categorical value.
 *
 * @author Keith M. Hughes
 */
trait CategoricalValue[+T <: CategoricalValueInstance] {
  
  /**
   * The name of the value.
   */
  val name: String

  /**
   * The universe of values for the categorical value.
   */
  val values: List[T]

  /**
   * Get the categorical value by its ID.
   *
   * @param id
   *          the ID
   *
   * @return the value as an option
   */
  def fromId(id: Int): Option[T]

  /**
   * Get the categorical value by its label.
   *
   * @param label
   *          the label
   *
   * @return the value as an option
   */
  def fromLabel(label: String): Option[T]
  
  /**
   * Is the given ID an ID for this value?
   * 
   * @param id
   *         the ID to test
   *         
   * @return {@code true} if a legal ID for this value
   */
  def isId(id: Int): Boolean 
  
  /**
   * Is the given label a label for this value?
   * 
   * @param label
   *         the label to test
   *         
   * @return {@code true} if a legal label for this value
   */
  def isLabel(label: String): Boolean 
}

/**
 * A base class for categorical values to simplify implementation.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseCategoricalValue[T <: CategoricalValueInstance](override val name: String, override val values: List[T]) extends CategoricalValue[T] {
    
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
  
  override def isId(id: Int): Boolean = {
    idToValue.contains(id)
  }
  
  override def isLabel(label: String): Boolean = {
    labelToValue.contains(label)
  }
}

/**
 * A factory class for creating dynamic categorical values.
 * 
 * @author Keith M. Hughes
 */
object DynamicCategoricalValue {
  
  /**
   * Create a dynamic categorical value from a set of labels.
   * 
   * @param name
   *        the name of the categorical value
   * @param labels
   *        the labels for the categorical value instances
   *        
   * @return the newly created categorical value
   */
  def fromLabels(name:String, labels: List[String]): DynamicCategoricalValue = {
    val values = labels.zipWithIndex.map { (pair) => new DynamicCategoricalValueInstance(pair._2,pair._1) }
       
    val value = new DynamicCategoricalValue(name, values)
    
    values.foreach { _.setValue(value) }
    
    return value
  }
}

/**
 * A class for dynamic categorical values.
 */
class DynamicCategoricalValue(_name: String, _values: List[DynamicCategoricalValueInstance]) extends 
    BaseCategoricalValue[DynamicCategoricalValueInstance](_name, _values) {
}

