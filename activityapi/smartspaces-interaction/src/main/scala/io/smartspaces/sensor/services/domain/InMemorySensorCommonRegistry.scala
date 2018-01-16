/*
 * Copyright (C) 2017 Keith M. Hughes
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
import scala.collection.mutable.Map

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.MeasurementUnitDescription
import io.smartspaces.sensor.domain.PhysicalSpaceTypeDescription
import io.smartspaces.sensor.domain.SensorTypeDescription
import javax.inject.Inject


/**
 * An in-memory registry of common sensor information,
 * 
 * @author Keith M. Hughes
 */
class InMemorySensorCommonRegistry @Inject()(log: ExtendedLog) extends SensorCommonRegistry {
  
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
   * A map of persistence IDs to sensor types.
   */
  private val idToSensorType: Map[String, SensorTypeDescription] = new HashMap

  /**
   * A map of external IDs to sensor types.
   */
  private val externalIdToSensorType: Map[String, SensorTypeDescription] = new HashMap

  /**
   * A map of persistence IDs to physical space types.
   */
  private val idToPhysicalSpaceType: Map[String, PhysicalSpaceTypeDescription] = new HashMap

  /**
   * A map of external IDs to physical space typels.
   */
  private val externalIdToPhysicalSpaceType: Map[String, PhysicalSpaceTypeDescription] = new HashMap

  override def registerMeasurementType(measurementType: MeasurementTypeDescription): SensorCommonRegistry = {
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

  override def registerSensorType(sensorDetail: SensorTypeDescription): SensorCommonRegistry = {
    idToSensorType.put(sensorDetail.id, sensorDetail)
    externalIdToSensorType.put(sensorDetail.externalId, sensorDetail)
    
    this
  }

  override def getSensorType(id: String): Option[SensorTypeDescription] = {
    idToSensorType.get(id)
  }

  override def getSensorTypeByExternalId(externalId: String): Option[SensorTypeDescription] = {
    externalIdToSensorType.get(externalId)
  }

  override def getAllSensorTypes(): List[SensorTypeDescription] = {
    idToSensorType.values.toList
  }

  override def registerPhysicalSpaceType(physicalSpaceType: PhysicalSpaceTypeDescription): SensorCommonRegistry = {
    idToPhysicalSpaceType.put(physicalSpaceType.id, physicalSpaceType)
    externalIdToPhysicalSpaceType.put(physicalSpaceType.externalId, physicalSpaceType)
    
    this
  }

  override def getPhysicalSpaceType(id: String): Option[PhysicalSpaceTypeDescription] = {
    idToPhysicalSpaceType.get(id)
  }

  override def getPhysicalSpaceTypeByExternalId(externalId: String): Option[PhysicalSpaceTypeDescription] = {
    externalIdToPhysicalSpaceType.get(externalId)
  }

  override def getAllPhysicalSpaceTypes(): List[PhysicalSpaceTypeDescription] = {
    idToPhysicalSpaceType.values.toList
  }

}