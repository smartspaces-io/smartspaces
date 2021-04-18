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

import io.smartspaces.sensor.domain.DataSourceAcquisitionModeCategoricalValueInstances
import io.smartspaces.sensor.domain.DataSourceProviderInterfaceTypeDescription
import io.smartspaces.sensor.domain.DataSourceProviderOriginTypeDescription
import io.smartspaces.sensor.domain.DataSourceProviderTypeDescription
import io.smartspaces.sensor.domain.MarkerTypeDescription
import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.MeasurementUnitDescription
import io.smartspaces.sensor.domain.PhysicalSpaceTypeDescription
import io.smartspaces.sensor.domain.SensorTypeDescription

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
  def getAllMeasurementTypes(): Iterable[MeasurementTypeDescription]

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
   * Get all measurement units from the registry.
   *
   * @return all measurement units
   */
  def getAllMeasurementUnits(): Iterable[MeasurementUnitDescription]

  /**
   * Register a sensor type with the registry.
   *
   * @param sensorType
   *          the sensor type to add
   *
   * @return this registry
   */
  def registerSensorType(sensorType: SensorTypeDescription): SensorCommonRegistry

  /**
   * Get a sensor type from the registry persistence ID.
   *
   * @param id
   *          id of the the sensor type
   *
   * @return the sensor type
   */
  def getSensorType(id: String): Option[SensorTypeDescription]

  /**
   * Get a sensor type from the registry external ID.
   *
   * @param externalId
   *          external id of the the sensor type
   *
   * @return the sensor type
   */
  def getSensorTypeByExternalId(externalId: String): Option[SensorTypeDescription]

  /**
   * Get all sensor types from the registry.
   *
   * @return all sensor types in the registry
   */
  def getAllSensorTypes(): Iterable[SensorTypeDescription]

  /**
   * Register a marker type with the registry.
   *
   * @param markerType
   *          the marker type to add
   *
   * @return this registry
   */
  def registerMarkerType(markerType: MarkerTypeDescription): SensorCommonRegistry

  /**
   * Get a marker type from the registry persistence ID.
   *
   * @param id
   *          id of the the marker type
   *
   * @return the marker type
   */
  def getMarkerType(id: String): Option[MarkerTypeDescription]

  /**
   * Get a marker type from the registry external ID.
   *
   * @param externalId
   *          external id of the the marker type
   *
   * @return the marker type
   */
  def getMarkerTypeByExternalId(externalId: String): Option[MarkerTypeDescription]

  /**
   * Get all marker types from the registry.
   *
   * @return all marker types in the registry
   */
  def getAllMarkerTypes(): Iterable[MarkerTypeDescription]

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
  def getAllPhysicalSpaceTypes(): Iterable[PhysicalSpaceTypeDescription]

  /**
   * Register a data source with the registry.
   *
   * @param dataSourceProviderType
   *          the data source to add
   *
   * @return this registry
   */
  def registerDataSourceProviderType(dataSourceProviderType: DataSourceProviderTypeDescription): SensorCommonRegistry

  /**
   * Get a data source type from the registry by external ID.
   *
   * @param externalId
   *          external id of the the data source type
   *
   * @return the data source type
   */
  def getDataSourceProviderTypeByExternalId(externalId: String): Option[DataSourceProviderTypeDescription]

  /**
   * Get all data source types from the registry.
   *
   * @return all data source types in the registry
   */
  def getAllDataSourceProviderTypes(): Iterable[DataSourceProviderTypeDescription]

  /**
   * Get all data source types from the registry that provide a given interface data source.
   *
   * @param interfaceId
   *        the ID for an interface
   *
   * @return all data source types in the registry
   */
  def getAllDataSourceProviderTypesByInterface(interfaceId: String): Iterable[DataSourceProviderTypeDescription]

  /**
   * Get all data source types from the registry that provide a given data source origin.
   *
   * @param originId
   *        the ID for an origin
   *
   * @return all data source types in the registry
   */
  def getAllDataSourceProviderTypesByOrigin(originId: String): Iterable[DataSourceProviderTypeDescription]

  /**
   * Get all data source types from the registry that provide a given acquisition.
   *
   * @param acquisitionMode
   *        the acquisition mode
   *
   * @return all data source types in the registry
   */
  def getAllDataSourceProviderTypesByAcquisitionMode(
    acquisitionMode: DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance):
  Iterable[DataSourceProviderTypeDescription]

  /**
   * Register a data source provider interface with the registry.
   *
   * @param dataSourceProviderInterfaceType
   *          the data source provider interface type to add
   *
   * @return this registry
   */
  def registerDataSourceProviderInterfaceType(dataSourceProviderInterfaceType: DataSourceProviderInterfaceTypeDescription): SensorCommonRegistry

  /**
   * Get a data source provider interface type from the registry by external ID.
   *
   * @param externalId
   *          external id of the the data source provider interface type
   *
   * @return the data source provider interface type, if found
   */
  def getDataSourceProviderInterfaceTypeByExternalId(externalId: String): Option[DataSourceProviderInterfaceTypeDescription]

  /**
   * Get all data source provider interface types from the registry.
   *
   * @return all data source provider interface types in the registry
   */
  def getAllDataSourceProviderInterfaceTypes(): Iterable[DataSourceProviderInterfaceTypeDescription]

  /**
   * Register a data source provider origin with the registry.
   *
   * @param dataSourceProviderOriginType
   *          the data source provider origin type to add
   *
   * @return this registry
   */
  def registerDataSourceProviderOriginType(dataSourceProviderOriginType: DataSourceProviderOriginTypeDescription): SensorCommonRegistry

  /**
   * Get a data source provider origin type from the registry by external ID.
   *
   * @param externalId
   *          external id of the the data source provider origin type
   *
   * @return the data source provider origin type, if found
   */
  def getDataSourceProviderOriginTypeByExternalId(externalId: String): Option[DataSourceProviderOriginTypeDescription]

  /**
   * Get all data source provider origin types from the registry.
   *
   * @return all data source provider origin types in the registry
   */
  def getAllDataSourceProviderOriginTypes(): Iterable[DataSourceProviderOriginTypeDescription]
}
