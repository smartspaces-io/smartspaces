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

/**
 * A model of the sensor state of an entity.
 *
 * @author Keith M. Hughes
 */
trait SensedEntityModel {

  /**
   * The sensed entity model collection this model is in.
   */
  val allModels: CompleteSensedEntityModel

  /**
   * The entity description for the entity being modeled.
   */
  val sensedEntityDescription: SensedEntityDescription
  
  /**
   * Add a sensor channel model that is sensing this entity.
   * 
   * @param sensorChannelModel
   *          the channel model to add
   */
  def addSensorChannelModel(sensorChannelModel: SensorChannelEntityModel): Unit
  
  /**
   * Get all sensor channel models associated with this entity.
   * 
   * @return all associated sensor channel models
   */
  def getAllSensorChannelModels(): List[SensorChannelEntityModel]

  /**
   * Get the value of a sensed property by the value type ID.
   *
   * @param valueTypeId
   *          the ID of the value type
   *
   * @return the sensed value with the specified type ID
   */
  def getSensedValue(valueTypeId: String): Option[SensedValue[Any]]

  /**
   * Get all sensed values for this entity.
   *
   * @return all sensed values
   */
  def getAllSensedValues(): List[SensedValue[Any]]

  /**
   * Update a sensed value.
   *
   * @param value
   *          the value being updated
   * @param updateTime
   * 		the time of this update
   */
  def updateSensedValue[T <: Any](value: SensedValue[T], updateTime: Long): Unit

  /**
   * Get the last update for the model.
   *
   * @return the last time
   */
  def getLastUpdate(): Long
}
