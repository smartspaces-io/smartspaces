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

package io.smartspaces.sensor.services.domain

import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.MeasurementUnitDescription
import io.smartspaces.sensor.domain.PhysicalSpaceTypeDescription
import io.smartspaces.sensor.domain.SensorDetailDescription

/**
 * A registry of known sensor common information.
 *
 * @author Keith M. Hughes
 */
trait SensorCommonRegistry {
  
  /**
   * Register a measurement type with the registry.
   *
   * @param measurementType
   *          the measurement type to add
   *
   * @return this registry
   */
  def registerMeasurementType(measurementType: MeasurementTypeDescription): SensorCommonRegistry

  /**
   * Get a measurement type from the registry by persistence ID.
   *
   * @param id
   *          id of the the measurement type
   *
   * @return the measurement type
   */
  def getMeasurementType(id: String): Option[MeasurementTypeDescription]

  /**
   * Get a measurement type from the registry by external ID.
   *
   * @param externalId
   *          external id of the the measurement type
   *
   * @return the measurement type
   */
  def getMeasurementTypeByExternalId(externalId: String): Option[MeasurementTypeDescription]

  /**
   * Get all measurement types from the registry.
   *
   * @return all measurement types in the registry
   */
  def getAllMeasurementTypes(): List[MeasurementTypeDescription]

  /**
   * Get a measurement unit from the registry persistence ID.
   *
   * @param id
   *          id of the the measurement unit
   *
   * @return the measurement unit
   */
  def getMeasurementUnit(id: String): Option[MeasurementUnitDescription]

  /**
   * Get a measurement unit from the registry persistence ID.
   *
   * @param id
   *          id of the the measurement unit
   *
   * @return the measurement unit
   */
  def getMeasurementUnitByExternalId(id: String): Option[MeasurementUnitDescription]

  /**
   * Register a sensor detail with the registry.
   *
   * @param sensorDetail
   *          the sensor detail to add
   *
   * @return this registry
   */
  def registerSensorDetail(sensorDetail: SensorDetailDescription): SensorCommonRegistry

  /**
   * Get a sensor detail from the registry persistence ID.
   *
   * @param id
   *          id of the the sensor detail
   *
   * @return the sensor detail
   */
  def getSensorDetail(id: String): Option[SensorDetailDescription]

  /**
   * Get a sensor detail from the registry external ID.
   *
   * @param externalId
   *          external id of the the sensor detail
   *
   * @return the sensor detail
   */
  def getSensorDetailByExternalId(externalId: String): Option[SensorDetailDescription]

  /**
   * Get all sensor details from the registry.
   *
   * @return all sensor details in the registry
   */
  def getAllSensorDetails(): List[SensorDetailDescription]

  /**
   * Register a physical space type with the registry.
   *
   * @param physicalSpaceType
   *          the physical space type to add
   *
   * @return this registry
   */
  def registerPhysicalSpaceType(physicalSpaceType: PhysicalSpaceTypeDescription): SensorCommonRegistry

  /**
   * Get a physical space type from the registry persistence ID.
   *
   * @param id
   *          id of the physical space type
   *
   * @return the physical space type if found
   */
  def getPhysicalSpaceType(id: String): Option[PhysicalSpaceTypeDescription]

  /**
   * Get a physical space type from the registry external ID.
   *
   * @param externalId
   *          external id of the the physical space type
   *
   * @return the physical space type if found
   */
  def getPhysicalSpaceTypeByExternalId(externalId: String): Option[PhysicalSpaceTypeDescription]

  /**
   * Get all physical space types from the registry.
   *
   * @return all physical space types in the registry
   */
  def getAllPhysicalSpaceTypes(): List[PhysicalSpaceTypeDescription]

}