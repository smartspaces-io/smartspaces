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

package io.smartspaces.sensor.entity

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import io.smartspaces.logging.ExtendedLog


/**
 * An in-memory registry of common sensor information,
 * 
 * @author Keith M. Hughes
 */
class InMemorySensorCommonRegistry(log: ExtendedLog) extends SensorCommonRegistry {
  
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

  override def registerSensorDetail(sensorDetail: SensorDetail): SensorCommonRegistry = {
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

}