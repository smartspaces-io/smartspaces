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

import io.smartspaces.monitor.expectation.time.HeartbeatMonitorable
import io.smartspaces.sensor.domain.SensorEntityDescription

/**
 * The model of a sensor.
 *
 * <p>
 * The sensor is considered online or offline by whether or not the sensor has sent either a
 * heartbeat or a sensor message within a specified time window. The time window for a given
 * sensor is set in the sensor description.
 *
 * @author Keith M. Hughes
 */
trait SensorEntityModel extends HeartbeatMonitorable {

  /**
   * The sensor entity description for the model.
   */
  def sensorEntityDescription: SensorEntityDescription

  /**
   * Get the sensed entity model collection this model is in.
   */
  def allModels: CompleteSensedEntityModel

  /**
   * The model that is being sensed by this sensor.
   */
  //var sensedEntityModel: Option[SensedEntityModel]

  /**
   * Add in a new sensor channel entity model to the sensor model.
   * 
   * @param sensorChannelEntityModel
   *          the new channel model
   *          
   * TODO(keith): Potentially move into another interface or make a builder for these things so they are unmodifiable.
   */
  def addSensorChannelModel(sensorChannelEntityModel: SensorChannelEntityModel): Unit

  /**
   * Get all sensor channel entity models for the sensor model.
   *          
   * @return all sensor channel entity models that are associated with the sensor.
   */
  def getAllSensorChannelModels(): Traversable[SensorChannelEntityModel]
  
  /**
   * Get a sensor channel entity model for a given channel ID.
   *
   * @param channelId
   *          the ID of the channel
   *
   * @return the channel model, if any
   */
  def getSensorChannelEntityModel(channelId: String): Option[SensorChannelEntityModel]
  
  /**
   * Does the sensor model have a sensor channel for a given channel ID.
   *
   * @param channelId
   *          the ID of the channel
   *
   * @return [[true]] if the sensor has a channel with the given ID
   */
  def hasSensorChannel(channelId: String): Boolean

  /**
   * Does the sensor return a given measurement type?
   *
   * @return [[true]] if the sensor has a given measurement type
   */
  def hasMeasurementType(measurementTypeExternalId: String): Boolean

  /**
   * Get all channels giving a particular measurement type.
   *
   * @return all channels giving a particular measurement type
   */
  def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelEntityModel]

  /**
   * Get the value of a sensed property by its type ID.
   *
   * @param valueTypeId
   *          the ID of the value type
   *
   * @return the sensed value with the specified value type
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
   * 		      the time of this update
   */
  def updateSensedValue[T <: Any](value: SensedValue[T], updateTime: Long): Unit
}