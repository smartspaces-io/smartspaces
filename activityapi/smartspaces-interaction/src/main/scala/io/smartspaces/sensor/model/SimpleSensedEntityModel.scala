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

package io.smartspaces.sensor.model

import io.smartspaces.sensor.domain.SensedEntityDescription

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * A very simple model of a sensed entity.
 *
 * @author Keith M. Hughes
 */
class SimpleSensedEntityModel(val sensedEntityDescription: SensedEntityDescription,
    val allModels: CompleteSensedEntityModel) extends SensedEntityModel {
  
  /**
   * The values being sensed keyed by the value name.
   */
  private val sensedValues: Map[String, SensedValue[Any]] = new HashMap
  
  /**
   * The sensor channels associated with this sensed item.
   */
  private var sensorChannelModels: List[SensorChannelEntityModel] = List()

  /**
   * The time of the last update.
   */
  private var lastUpdate: Long = 0
  
  override def addSensorChannelModel(sensorChannelModel: SensorChannelEntityModel): Unit = {
    sensorChannelModels = sensorChannelModel :: sensorChannelModels
  }
  
  override def getAllSensorChannelModels(): List[SensorChannelEntityModel] = {
    sensorChannelModels
  }
  
  override def getSensedValue(valueTypeId: String): Option[SensedValue[Any]] = {
    // TODO(keith): Needs some sort of concurrency block
    sensedValues.get(valueTypeId)
  }

  override def getAllSensedValues(): scala.collection.immutable.List[SensedValue[Any]] = {
    sensedValues.values.toList
  }

  override def updateSensedValue[T <: Any](value: SensedValue[T], timestamp: Long): Unit = {
    // TODO(keith): Needs some sort of concurrency block
    lastUpdate = timestamp
    sensedValues.put(value.measurementTypeDescription.externalId, value);
  }
  
  override def getLastUpdate(): Long = {
    lastUpdate
  }
}
