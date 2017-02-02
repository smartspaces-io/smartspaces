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

package io.smartspaces.sensor.entity.model.query

import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedValue
import io.smartspaces.sensor.value.converter.ObjectConverter

/**
 * A processor for queries against a sensor model.
 *
 * <p>
 * All queries are done in an appropriate transaction.
 *
 * @author Keith M. Hughes
 */
trait SensedEntityModelQueryProcessor {

  /**
   * Get all values from the entire model for a given measurement type.
   *
   * @param sensedEntityId
   *           ID of the sensed entity of interest
   *
   * @return the list of sensor values for the given sensed entity
   */
  def getAllValuesForSensedEntity(sensedEntityId: String): Option[List[SensedValue[Any]]]

  /**
   * Get all values from the entire model for a given measurement type.
   *
   * @param measurementTypeId
   *           ID of the measurement type of interest
   *
   * @return the list of sensor values for the given measurement type
   */
  def getAllValuesForMeasurementType(measurementTypeId: String): List[SensedValue[Any]]

  /**
   * Get all occupants of a given physical location.
   *
   * @param physicalLocationId
   *            ID of the physical location
   *
   * @returns the list of occupants of the physical location, or none if the location doesn't exist.
   */
  def getOccupantsOfPhysicalLocation(physicalLocationId: String): Option[Set[PersonSensedEntityModel]]

  /**
   * Get all physical location models and convert them.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param converter
   * 			the converter to be applied to the location models
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getAllPhysicalLocations[T](converter: ObjectConverter[List[PhysicalSpaceSensedEntityModel], T]): T
}