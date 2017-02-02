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

package io.smartspaces.sensor.entity

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

/**
 * A sensor registry totally contained in memory.
 *
 * @author Keith M. Hughes
 */
class InMemorySensorRegistry extends SensorRegistry {

  /**
   * A map of persistence IDs to their measurement types.
   */
  private val idToMeasurementType: Map[String, MeasurementTypeDescription] = new HashMap

  /**
   * A map of external IDs to their measurement types.
   */
  private val externalIdToMeasurementType: Map[String, MeasurementTypeDescription] = new HashMap

  /**
   * A map of persistence IDs to measurement units.
   */
  private val idToMeasurementUnit: Map[String, MeasurementUnitDescription] = new HashMap

  /**
   * A map of external IDs to measurement units.
   */
  private val externalIdToMeasurementUnit: Map[String, MeasurementUnitDescription] = new HashMap

  /**
   * A map of persistence IDs to sensor details.
   */
  private val idToSensorDetail: Map[String, SensorDetail] = new HashMap

  /**
   * A map of external IDs to sensor details.
   */
  private val externalIdToSensorDetail: Map[String, SensorDetail] = new HashMap

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
  private val sensorSensedEntityAssociations: ListBuffer[SimpleSensorSensedEntityAssociation] =
    new ListBuffer

  /**
   * The associations between markers and what entity is being marked by them.
   */
  private val markerMarkedEntityAssociations: ListBuffer[MarkerMarkedEntityAssociation] = new ListBuffer

  /**
   * The entity configurations.
   */
  private val configurations: Map[String, Map[String, AnyRef]] = new HashMap

  override def registerMeasurementType(measurementType: MeasurementTypeDescription): SensorRegistry = {
    idToMeasurementType.put(measurementType.id, measurementType)
    externalIdToMeasurementType.put(measurementType.externalId, measurementType)

    measurementType.getAllMeasurementUnits().foreach((unit) => {
      idToMeasurementUnit.put(unit.id, unit)
      externalIdToMeasurementUnit.put(unit.externalId, unit)
    })

    this
  }

  override def getMeasurementType(id: String): Option[MeasurementTypeDescription] = {
    idToMeasurementType.get(id)
  }
 
  override def getMeasurementTypeByExternalId(externalId: String): Option[MeasurementTypeDescription] = {
    externalIdToMeasurementType.get(externalId)
  }
 
  override def getAllMeasurementTypes(): List[MeasurementTypeDescription] = {
    idToMeasurementType.values.toList
  }

  override def getMeasurementUnit(id: String): Option[MeasurementUnitDescription] = {
    idToMeasurementUnit.get(id)
  }

  override def getMeasurementUnitByExternalId(externalId: String): Option[MeasurementUnitDescription] = {
    externalIdToMeasurementUnit.get(externalId)
  }

  override def registerSensorDetail(sensorDetail: SensorDetail): SensorRegistry = {
    idToSensorDetail.put(sensorDetail.id, sensorDetail)
    externalIdToSensorDetail.put(sensorDetail.externalId, sensorDetail)
    
    this
  }

  override def getSensorDetail(id: String): Option[SensorDetail] = {
    idToSensorDetail.get(id)
  }

  override def getSensorDetailByExternalId(externalId: String): Option[SensorDetail] = {
    externalIdToSensorDetail.get(externalId)
  }

  override def getAllSensorDetails(): List[SensorDetail] = {
    idToSensorDetail.values.toList
  }

  override def registerSensor(sensor: SensorEntityDescription): SensorRegistry = {
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
  
  override def registerMarker(marker: MarkerEntityDescription): SensorRegistry = {
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

  override def registerSensedEntity(sensedEntity: SensedEntityDescription): SensorRegistry = {
    idToSensed.put(sensedEntity.id, sensedEntity)
    externalIdToSensed.put(sensedEntity.externalId, sensedEntity)

    if (sensedEntity.isInstanceOf[MarkableEntityDescription]) {
      val markable = sensedEntity.asInstanceOf[MarkableEntityDescription]
      idToMarkable.put(sensedEntity.id, markable)
      externalIdToMarkable.put(sensedEntity.externalId, markable)
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

  override def associateSensorWithSensedEntity(sensorId: String, sensedEntityId: String): SensorRegistry = {
    // TODO(keith) Decide what to do if neither exists
    val sensor = externalIdToSensor.get(sensorId)
    val sensedEntity = externalIdToSensed.get(sensedEntityId)

    sensorSensedEntityAssociations +=
      new SimpleSensorSensedEntityAssociation(sensor.get, sensedEntity.get)

    this
  }

  override def getSensorSensedEntityAssociations(): scala.collection.immutable.List[SimpleSensorSensedEntityAssociation] = {
    sensorSensedEntityAssociations.toList
  }

  override def associateMarkerWithMarkedEntity(markerId: String, markedEntityId: String): SensorRegistry = {
    // TODO(keith) Decide what to do if neither exists
    val marker = externalIdToMarker.get(markerId)
    val markedEntity = externalIdToMarkable.get(markedEntityId)

    associateMarkerWithMarkedEntity(marker.get, markedEntity.get)
  }

  override def associateMarkerWithMarkedEntity(marker: MarkerEntityDescription,
    markableEntity: MarkableEntityDescription): SensorRegistry = {
    markerMarkedEntityAssociations +=
      new SimpleMarkerMarkedEntityAssociation(marker, markableEntity)

    markerIdToMarkable.put(marker.markerId, markableEntity)

    this
  }

  override def getMarkerMarkedEntityAssociations(): List[MarkerMarkedEntityAssociation] = {
    markerMarkedEntityAssociations.to
  }

  override def getMarkerEntityByMarkerId(markerId: String): Option[MarkerEntityDescription] = {
    markerIdToMarker.get(markerId)
  }

  override def getMarkableEntityByMarkerId(markerId: String): Option[MarkableEntityDescription] = {
    markerIdToMarkable.get(markerId)
  }

  override def addConfigurationData(entityId: String,
    configurationData: scala.collection.immutable.Map[String, AnyRef]): SensorRegistry = {
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
