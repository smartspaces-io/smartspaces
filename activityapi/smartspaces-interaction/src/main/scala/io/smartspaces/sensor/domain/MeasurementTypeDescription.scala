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

package io.smartspaces.sensor.domain

import scala.collection.mutable.ArrayBuffer

object MeasurementTypeDescription {
  
  /**
   * The prefix on the value of the value type field for categorical variables.
   */
  val VALUE_TYPE_PREFIX_CATEGORICAL_VARIABLE = "categorical:"
   
  /**
   * The value type for continuous numbers.
   */
  val VALUE_TYPE_NUMERIC_CONTINUOUS = "numeric:continuous"
  
  /**
   * The value type for ids.
   */
  val VALUE_TYPE_ID = "id"
}

/**
 * A description of a measurement type, such as temperature or humidity.
 * 
 * <p>
 * The value type is the type of the value, e.g. a double, an integer.
 *
 * @author Keith M. Hughes
 */
trait MeasurementTypeDescription extends DisplayableDescription {

  /**
   * The ID of the measurement type.
   */
  val id: String

  /**
   * The external ID of the measurement type.
   */
  val externalId: String
  
  /**
   * The processing type of the measurement
   */
  val processingType: String
  
  /**
   * The value type of the measurement type.
   */
  val valueType: String

  /**
   * The default unit for the measurement type.
   */
  var defaultUnit: Option[MeasurementUnitDescription]

  /**
   * The aliases for the measurement type.
   */
  val aliases: Set[String]

  /**
   * Add in a new measurement unit to the measurement type.
   *
   * @param measurementUnit
   *      the measurement unit to add
   */
  def addMeasurementUnit(measurementUnit: MeasurementUnitDescription): Unit

  /**
   * Does the type have measurement units.
   *
   * @return [[true]] if there are measurement units
   */
  def hasMeasurementUnits(): Boolean
  
  /**
   * Get all measurement units for this measurement type.
   *
   * @return all of the measurement units for this type
   */
  def getAllMeasurementUnits(): List[MeasurementUnitDescription]

   
  /**
   * Get a measurement unit of this measurement type.
   * 
   * <p>
   * The unit must be a unit of this type to be found.
   * 
   * @param externalId
   *     the external ID of the measurement unit
   *
   * @return the measurement unit
   */
  def getMeasurementUnit(externalId: String): Option[MeasurementUnitDescription]
}

/**
 * A simple implementation of the measurement type description.
 *
 * @author Keith M. Hughes
 */
case class SimpleMeasurementTypeDescription(
    override val id: String, 
    override val externalId: String, 
    override val displayName: String, 
    override val displayDescription: Option[String], 
    override val processingType: String, 
    override val valueType: String, 
    override val aliases: Set[String]) extends MeasurementTypeDescription {

  /**
   * The measurement units for this type.
   */
  private val measurementUnits = new ArrayBuffer[MeasurementUnitDescription]
  
  /**
   * The default unit for the measurement type.
   */
  override var defaultUnit: Option[MeasurementUnitDescription] = None
   
  override def addMeasurementUnit(measurementUnit: MeasurementUnitDescription): Unit = {
    measurementUnits += measurementUnit
  }
  
  override def hasMeasurementUnits(): Boolean = {
    !measurementUnits.isEmpty
  }
  
  override def getAllMeasurementUnits(): List[MeasurementUnitDescription] = {
    measurementUnits.toList
  }
  
  override def getMeasurementUnit(externalId: String): Option[MeasurementUnitDescription] = {
    measurementUnits.find(_.externalId == externalId)
  }
}
