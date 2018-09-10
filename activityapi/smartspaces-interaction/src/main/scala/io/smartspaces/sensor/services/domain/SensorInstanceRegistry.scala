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

package io.smartspaces.sensor.services.domain

import io.smartspaces.sensor.domain.MarkableEntityDescription
import io.smartspaces.sensor.domain.MarkerEntityDescription
import io.smartspaces.sensor.domain.MarkerMarkedEntityAssociationDescription
import io.smartspaces.sensor.domain.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.domain.SensedEntityDescription
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.domain.SensorSensedEntityAssociationDescription

/**
 * A registry of known sensor instances and entities that are being sensed.
 *
 * @author Keith M. Hughes
 */
trait SensorInstanceRegistry {

  /**
   * Register a sensor with the registry.
   *
   * @param sensor
   *          the sensor to add
   *
   * @return this registry
   */
  def registerSensor(sensor: SensorEntityDescription): SensorInstanceRegistry

  /**
   * Get the sensor description by its persistence ID.
   *
   * @param id
   *          the sensor ID
   *
   * @return the description
   */
  def getSensor(id: String): Option[SensorEntityDescription]

  /**
   * Get the sensor description by its external ID.
   *
   * @param externalId
   *          the sensor ID
   *
   * @return the description
   */
  def getSensorByExternalId(externalId: String): Option[SensorEntityDescription]

  /**
   * Get all the sensor entities in the registry.
   *
   * @return a collection of the entities
   */
  def getAllSensorEntities(): List[SensorEntityDescription]

  /**
   * Register a marker with the registry.
   *
   * @param marker
   *          the marker to add
   *
   * @return this registry
   */
  def registerMarker(marker: MarkerEntityDescription): SensorInstanceRegistry

  /**
   * Get the marker description by persistence ID.
   *
   * @param id
   *          the marker ID
   *
   * @return the description
   */
  def getMarker(id: String): Option[MarkerEntityDescription]

  /**
   * Get the marker description by external ID.
   *
   * @param externalId
   *          the marker ID
   *
   * @return the description
   */
  def getMarkerByExternalId(externalId: String): Option[MarkerEntityDescription]

  /**
   * Get the markable entity description by persistence ID.
   *
   * @param id
   *          the markable entity ID
   *
   * @return the description
   */
  def getMarkableEntity(id: String): Option[MarkableEntityDescription]

  /**
   * Get the markable entity description by external ID.
   *
   * @param externalId
   *          the markable entity ID
   *
   * @return the description
   */
  def getMarkableEntityByExternalId(externalId: String): Option[MarkableEntityDescription]

  /**
   * Get the marker entity description associated with a given marker ID.
   *
   * <p>
   * The {@code markerId} is the value of
   * {@link MarkerEntityDescription.getMarkerId()}.
   *
   * @param markerId
   *          the marker ID
   *
   * @return the description
   */
  def getMarkerEntityByMarkerId(markerId: String): Option[MarkerEntityDescription]

  /**
   * Get the markable entity description associated with a given marker ID.
   *
   * <p>
   * The {@code markerId} is the value of
   * {@link MarkerEntityDescription.getMarkerId()}.
   *
   * @param markerId
   *          the marker ID
   *
   * @return the description
   */
  def getMarkableEntityByMarkerId(markerId: String): Option[MarkableEntityDescription]

  /**
   * Register a sensed entity with the registry.
   *
   * @param sensedEntity
   *          the sensed entity to add
   *
   * @return this registry
   */
  def registerSensedEntity(sensor: SensedEntityDescription): SensorInstanceRegistry

  /**
   * Get the sensed entity description by persistence ID.
   *
   * @param id
   *          the sensed entity ID
   *
   * @return the description
   */
  def getSensedEntity(id: String): Option[SensedEntityDescription]

  /**
   * Get the sensed entity description by external ID.
   *
   * @param externalId
   *          the sensed entity ID
   *
   * @return the description
   */
  def getSensedEntityByExternalId(externalId: String): Option[SensedEntityDescription]

  /**
   * Get all the sensed entities in the registry.
   *
   * @return a collection of the entities
   */
  def getAllSensedEntities(): List[SensedEntityDescription]
  
  /**
   * Get all physical spaces in the registry.
   * 
   * @return a collection of the entities
   */
  def getAllPhysicalSpaceSensedEntities(): List[PhysicalSpaceSensedEntityDescription]

  /**
   * Get the physical space sensed entity description by persistence ID.
   *
   * @param id
   *          the sensed entity ID
   *
   * @return the description
   */
  def getPhysicalSpaceSensedEntity(id: String): Option[PhysicalSpaceSensedEntityDescription]

  /**
   * Get the physical space sensed entity description by external ID.
   *
   * @param externalId
   *          the sensed entity ID
   *
   * @return the description
   */
  def getPhysicalSpaceSensedEntityByExternalId(externalId: String): Option[PhysicalSpaceSensedEntityDescription]

  /**
   * Get all sensor associations for a given sensed entity.
   * 
   * The list will be empty if the external ID is not for a sensed entity.
   * 
   * @return the list of associations
   */
  def getSensorsForSensedEntityByExternalId(externalId: String): List[SensorSensedEntityAssociationDescription]
  
  /**
   * Associate a marker with its marked entity.
   *
   * @param markerExternalId
   *          the external ID of the marker
   * @param markedEntityExternalId
   *          the external ID of the marked entity
   *
   * @returns this registry
   */
  def associateMarkerWithMarkedEntity(markerExternalId: String, markedEntityExternalId: String): SensorInstanceRegistry

  /**
   * Associate a marker with its marked entity.
   *
   * @param marker
   *          the marker
   * @param markableEntity
   *          the markable entity
   *
   * @returns this registry
   */
  def associateMarkerWithMarkedEntity(marker: MarkerEntityDescription,
    markableEntity: MarkableEntityDescription): SensorInstanceRegistry

  /**
   * Get the associations between markers and their marked entities.
   *
   * @return the associations as an unmodifiable list
   */
  def getMarkerMarkedEntityAssociations(): List[MarkerMarkedEntityAssociationDescription]

  /**
   * Associate a sensor with its sensed entity.
   *
   * @param sensorExternalId
   *          the external ID of the sensor
   * @param sensorChannelId
   *          the ID of the sensor channel for the sensed entity
   * @param sensedEntityExternalId
   *          the external ID of the sensed entity
   * @param stateUpdateTimeLimit
   *        the time limit for state updates
   * @param heartbeatUpdateTimeLimit
   *        the time limit for state updates
   *
   * @returns this registry
   */
  def associateSensorWithSensedEntity(
      sensorExternalId: String, 
      sensorChannelId: String, 
      sensedEntityExternalId: String,
      stateUpdateTimeLimit: Option[Long],
      heartbeatUpdateTimeLimit: Option[Long]): SensorInstanceRegistry

  /**
   * Get the associations between sensors and their sensed entities.
   *
   * @return the associations as an unmodifiable list
   */
  def getSensorSensedEntityAssociations(): List[SensorSensedEntityAssociationDescription]

  /**
   * Add in configuration data for a given entity.
   *
   * <p>
   * This data will be merged with any previous data.
   *
   * @param entityId
   *          the ID of the entity
   * @param configurationData
   *          the configuration data to add
   *
   * @return this registry
   */
  def addConfigurationData(entityId: String, configurationData: Map[String, AnyRef]): SensorInstanceRegistry

  /**
   * Get the configuration data for the given entity.
   *
   * @param entityId
   *          the entity ID
   *
   * @return the configuration data known for the entity
   */
  def getConfigurationData(entityId: String): Map[String, AnyRef]
}