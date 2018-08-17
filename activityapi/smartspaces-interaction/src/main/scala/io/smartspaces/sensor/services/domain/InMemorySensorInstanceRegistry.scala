/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
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

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import io.smartspaces.logging.ExtendedLog
import javax.inject.Inject
import io.smartspaces.sensor.domain.MarkableEntityDescription
import io.smartspaces.sensor.domain.MarkerEntityDescription
import io.smartspaces.sensor.domain.MarkerMarkedEntityAssociationDescription
import io.smartspaces.sensor.domain.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.domain.SensedEntityDescription
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.domain.SensorSensedEntityAssociationDescription
import io.smartspaces.sensor.domain.SimpleMarkerMarkedEntityAssociationDescription
import io.smartspaces.sensor.domain.SimpleSensorSensedEntityAssociationDescription
import io.smartspaces.sensor.domain.SimpleSensorSensedEntityAssociationDescription

/**
 * A sensor registry totally contained in memory.
 *
 * @author Keith M. Hughes
 */
class InMemorySensorInstanceRegistry @Inject()(log: ExtendedLog) extends SensorInstanceRegistry {

  /**
   * A map of persistence sensor IDs to their description.
   */
  private val idToSensor: Map[String, SensorEntityDescription] = new HashMap

  /**
   * A map of external sensor IDs to their description.
   */
  private val externalIdToSensor: Map[String, SensorEntityDescription] = new HashMap

  /**
   * A map persistence of marker IDs to their description.
   */
  private val idToMarker: Map[String, MarkerEntityDescription] = new HashMap

  /**
   * A map external of marker IDs to their description.
   */
  private val externalIdToMarker: Map[String, MarkerEntityDescription] = new HashMap

  /**
   * A map of markable persistence IDs to their description.
   */
  private val idToMarkable: Map[String, MarkableEntityDescription] = new HashMap

  /**
   * A map of markable external IDs to their description.
   */
  private val externalIdToMarkable: Map[String, MarkableEntityDescription] = new HashMap

  /**
   * A map of markable persistence IDs to their description.
   */
  private val idToPhysicalSpace: Map[String, PhysicalSpaceSensedEntityDescription] = new HashMap

  /**
   * A map of markable external IDs to their description.
   */
  private val externalIdToPhysicalSpace: Map[String, PhysicalSpaceSensedEntityDescription] = new HashMap

  /**
   * A map of marker IDs to their description.
   */
  private val markerIdToMarker: Map[String, MarkerEntityDescription] = new HashMap

  /**
   * A map of marker IDs to their description.
   */
  private val markerIdToMarkable: Map[String, MarkableEntityDescription] = new HashMap

  /**
   * A map of sensed entities  persistence IDs to their description
   */
  private val idToSensed: Map[String, SensedEntityDescription] = new HashMap

  /**
   * A map of sensed entities  external IDs to their description
   */
  private val externalIdToSensed: Map[String, SensedEntityDescription] = new HashMap

  /**
   * The associations between sensors and what entity is being sensed by them.
   */
  private val sensorSensedEntityAssociations: ListBuffer[SensorSensedEntityAssociationDescription] =
    new ListBuffer

  /**
   * The associations between markers and what entity is being marked by them.
   */
  private val markerMarkedEntityAssociations: ListBuffer[MarkerMarkedEntityAssociationDescription] = new ListBuffer

  /**
   * The entity configurations.
   */
  private val configurations: Map[String, Map[String, AnyRef]] = new HashMap

  override def registerSensor(sensor: SensorEntityDescription): SensorInstanceRegistry = {
    idToSensor.put(sensor.id, sensor)
    externalIdToSensor.put(sensor.externalId, sensor)

    this
  }

  override def getSensor(id: String): Option[SensorEntityDescription] = {
    idToSensor.get(id)
  }

  override def getSensorByExternalId(externalId: String): Option[SensorEntityDescription] = {
    externalIdToSensor.get(externalId)
  }
  
  override def getAllSensorEntities(): List[SensorEntityDescription] = {
      idToSensor.values.toList
  }
  
  override def registerMarker(marker: MarkerEntityDescription): SensorInstanceRegistry = {
    idToMarker.put(marker.id, marker)
    externalIdToMarker.put(marker.externalId, marker)
    markerIdToMarker.put(marker.markerId, marker)

    this
  }

  override def getMarker(id: String): Option[MarkerEntityDescription] = {
    idToMarker.get(id)
  }

  override def getMarkerByExternalId(externalId: String): Option[MarkerEntityDescription] = {
    externalIdToMarker.get(externalId)
  }

  override def getMarkableEntity(id: String): Option[MarkableEntityDescription] = {
    idToMarkable.get(id)
  }

  override def getMarkableEntityByExternalId(externalId: String): Option[MarkableEntityDescription] = {
    externalIdToMarkable.get(externalId)
  }

  override def registerSensedEntity(sensedEntity: SensedEntityDescription): SensorInstanceRegistry = {
    idToSensed.put(sensedEntity.id, sensedEntity)
    externalIdToSensed.put(sensedEntity.externalId, sensedEntity)

    if (sensedEntity.isInstanceOf[MarkableEntityDescription]) {
      val markable = sensedEntity.asInstanceOf[MarkableEntityDescription]
      idToMarkable.put(sensedEntity.id, markable)
      externalIdToMarkable.put(sensedEntity.externalId, markable)
    }

    if (sensedEntity.isInstanceOf[PhysicalSpaceSensedEntityDescription]) {
      val physicalSpace = sensedEntity.asInstanceOf[PhysicalSpaceSensedEntityDescription]
      idToPhysicalSpace.put(sensedEntity.id, physicalSpace)
      externalIdToPhysicalSpace.put(sensedEntity.externalId, physicalSpace)
    }

    this
  }

  override def getSensedEntity(id: String): Option[SensedEntityDescription] = {
    idToSensed.get(id)
  }

  override def getSensedEntityByExternalId(externalId: String): Option[SensedEntityDescription] = {
    externalIdToSensed.get(externalId)
  }

  override def getAllSensedEntities(): scala.collection.immutable.List[SensedEntityDescription] = {
    idToSensed.values.toList
  }

  override def getAllPhysicalSpaceSensedEntities(): List[PhysicalSpaceSensedEntityDescription] = {
    idToPhysicalSpace.values.toList
  }

  override def getPhysicalSpaceSensedEntity(id: String): Option[PhysicalSpaceSensedEntityDescription] = {
    idToPhysicalSpace.get(id)
  }

  override def getPhysicalSpaceSensedEntityByExternalId(externalId: String): Option[PhysicalSpaceSensedEntityDescription] = {
    externalIdToPhysicalSpace.get(externalId)
  }

  override def associateSensorWithSensedEntity(sensorExternalId: String, sensorChannelId: String, sensedEntityExternalId: String): SensorInstanceRegistry = {
    // TODO(keith) Decide what to do if neither exists
    val sensor = externalIdToSensor.get(sensorExternalId)
    val sensedEntity = externalIdToSensed.get(sensedEntityExternalId)
    
    if (sensor.isEmpty) {
      log.warn(s"Sensor external ID ${sensorExternalId} not found when associating sensor with sensed entity")
      return this
    }
    
    val sensorChannelDetail = sensor.get.sensorType.getSupportedSensorChannelDetail(sensorChannelId)
    if (sensorChannelId.isEmpty) {
      log.warn(s"Sensor channel ID ${sensorChannelId} in sensor detail for sensor external ID ${sensorExternalId} not found when associating sensor with sensed entity")
      return this
    }
    
    if (sensedEntity.isEmpty) {
      log.warn(s"Sensed entity external ID ${sensedEntityExternalId} not found when associating sensor with sensed entity")
      return this
    }

    sensorSensedEntityAssociations +=
      new SimpleSensorSensedEntityAssociationDescription(sensor.get, sensorChannelDetail.get, sensedEntity.get)

    this
  }

  override def getSensorSensedEntityAssociations(): scala.collection.immutable.List[SensorSensedEntityAssociationDescription] = {
    sensorSensedEntityAssociations.toList
  }

  override def getSensorsForSensedEntityByExternalId(externalId: String): scala.collection.immutable.List[SensorSensedEntityAssociationDescription] = {
    sensorSensedEntityAssociations.filter(_.sensedEntity.externalId == externalId).toList
  }
  
  override def associateMarkerWithMarkedEntity(markerExternalId: String, markedEntityExternalId: String): SensorInstanceRegistry = {
    // TODO(keith) Decide what to do if neither exists
    val marker = externalIdToMarker.get(markerExternalId)
    val markedEntity = externalIdToMarkable.get(markedEntityExternalId)
    
    if (marker.isEmpty) {
      log.warn(s"Marker ID ${markerExternalId} not found when associating marker with marked entity")
      return this
    }
    
    if (markedEntity.isEmpty) {
      log.warn(s"Marked entity ID ${markedEntityExternalId} not found when associating marker with marked entity")
      return this
    }
    

    associateMarkerWithMarkedEntity(marker.get, markedEntity.get)
  }

  override def associateMarkerWithMarkedEntity(marker: MarkerEntityDescription,
    markableEntity: MarkableEntityDescription): SensorInstanceRegistry = {
    markerMarkedEntityAssociations +=
      new SimpleMarkerMarkedEntityAssociationDescription(marker, markableEntity)

    markerIdToMarkable.put(marker.markerId, markableEntity)

    this
  }

  override def getMarkerMarkedEntityAssociations(): List[MarkerMarkedEntityAssociationDescription] = {
    markerMarkedEntityAssociations.to
  }

  override def getMarkerEntityByMarkerId(markerId: String): Option[MarkerEntityDescription] = {
    markerIdToMarker.get(markerId)
  }

  override def getMarkableEntityByMarkerId(markerId: String): Option[MarkableEntityDescription] = {
    markerIdToMarkable.get(markerId)
  }

  override def addConfigurationData(entityId: String,
    configurationData: scala.collection.immutable.Map[String, AnyRef]): SensorInstanceRegistry = {
    val map: Map[String, AnyRef] = getConfigurationMap(entityId)

    map ++= configurationData

    this
  }

  override def getConfigurationData(entityId: String): scala.collection.immutable.Map[String, AnyRef] = {
    val map = getConfigurationMap(entityId)

    map.toMap
  }

  /**
   * Get the configuration map for the given entity ID.
   *
   * [p]
   * Create a map if there isn't one yet.
   *
   * @param entityId
   *          the ID of the entity
   *
   * @return the map for the entity
   */
  private def getConfigurationMap(entityId: String): Map[String, AnyRef] = {
    val map = configurations.get(entityId)
    if (map.isEmpty) {
      val newMap: Map[String, AnyRef] = new HashMap

      configurations.put(entityId, newMap)

      newMap
    } else {
      map.get
    }
  }
}
