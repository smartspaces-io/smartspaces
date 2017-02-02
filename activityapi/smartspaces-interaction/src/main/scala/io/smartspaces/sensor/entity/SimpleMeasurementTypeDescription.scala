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

package io.smartspaces.sensor.entity

import scala.collection.mutable.ArrayBuffer

/**
 * A simple implementation of the measurement type description.
 *
 * @author Keith M. Hughes
 */
case class SimpleMeasurementTypeDescription(val id: String, val externalId: String, val displayName: String, val displayDescription: String, val valueType: String, val aliases: Set[String]) extends MeasurementTypeDescription {

  /**
   * The measurement units for this type.
   */
  private val measurementUnits = new ArrayBuffer[MeasurementUnitDescription]
  
  /**
   * The default unit for the measurement type.
   */
  var defaultUnit: MeasurementUnitDescription = null
   
  override def addMeasurementUnit(measurementUnit: MeasurementUnitDescription): Unit = {
    measurementUnits += measurementUnit
  }
  
  override def getAllMeasurementUnits(): List[MeasurementUnitDescription] = {
    measurementUnits.toList
  }
  
  override def getMeasurementUnit(id: String): Option[MeasurementUnitDescription] = {
    measurementUnits.find(_.id == id)
  }
}