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

import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedValue
import io.smartspaces.sensor.entity.model.SensorEntityModel

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
   * Get all sensed values for a given sensed entity.
   *
   * @param sensedEntityExternalId
   *           The external ID of the sensed entity of interest
   *
   * @return the list of sensor values for the given sensed entity
   */
  def getAllValuesForSensedEntity(sensedEntityId: String): Option[List[SensedValue[Any]]]

  /**
   * Get all values from the entire model for a given measurement type.
   *
   * @param measurementTypeExternalId
   *           The external ID of the measurement type of interest
   *
   * @return the list of sensor values for the given measurement type
   */
  def getAllValuesForMeasurementType(measurementTypeExternalId: String): List[SensedValue[Any]]

  /**
   * Get all occupants of a given physical location.
   *
   * @param physicalLocationExternalId
   *            ID of the physical location
   *
   * @returns the list of occupants of the physical location, or none if the location doesn't exist.
   */
  def getOccupantsOfPhysicalSpace(physicalLocationExternalId: String): Option[Set[PersonSensedEntityModel]]

  /**
   * Get a physical space model and convert it.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param id
   *      the ID of the physical space
   * @param converter
   * 			the converter to be applied to the physical space models
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getPhysicalSpace[T](id: String, converter: MessageEncoder[PhysicalSpaceSensedEntityModel, T]): Option[T]

  /**
   * Get all physical space models and convert them.
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
  def getAllPhysicalSpaces[T](converter: MessageEncoder[List[PhysicalSpaceSensedEntityModel], T]): T

  /**
   * Get a person model and convert it.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param id
   *      ID of the person
   * @param converter
   * 			the converter to be applied to the person model
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getPerson[T](id: String, converter: MessageEncoder[PersonSensedEntityModel, T]): Option[T]

  /**
   * Get all people models and convert them.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param converter
   * 			the converter to be applied to the people models
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getAllPeople[T](converter: MessageEncoder[List[PersonSensedEntityModel], T]): T

  /**
   * Get a sensor model and convert it.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param id
   *      the ID of the sensor
   * @param converter
   * 			the converter to be applied to the sensor models
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getSensor[T](id: String, converter: MessageEncoder[SensorEntityModel, T]): Option[T]

  /**
   * Get all sensor models and convert them.
   *
   * <p>
   * The conversion will take place in a read transaction.
   *
   * @param converter
   * 			the converter to be applied to the sensor models
   * @param [T]
   * 			the return type of the converter
   *
   * @return the converted model
   */
  def getAllSensors[T](converter: MessageEncoder[List[SensorEntityModel], T]): T
  
  /**
   * Get all of the unknown marker IDs.
   *
   * @return all of the unknown marker IDs
   */
  def getAllUnknownMarkerIds(): Set[String]

  /**
   * Get all of the unknown sensor IDs.
   *
   * @return all of the unknown sensor IDs
   */
  def getAllUnknownSensorIds(): Set[String]
}