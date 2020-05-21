/**
 *
 */
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

package io.smartspaces.sensor.domain

/**
 * An entity description of a sensor.
 *
 * @author Keith M. Hughes
 */
trait SensorEntityDescription extends EntityDescription {

  /**
   * The sensor type for the sensor.
   */
  def sensorType: SensorTypeDescription

  /**
   * The source of the sensor, e.g. from an external sensor relay and the name it has in that relay.
   */
  def dataSource: DataSourceDescription

  /**
   * The time limit on when a sensor update should happen, in milliseconds
   */
  def stateUpdateTimeLimit: Option[Long]

  /**
   * The time limit on when a sensor heartbeat update should happen, in milliseconds
   */
  def heartbeatUpdateTimeLimit: Option[Long]

  /**
   * {@code true} if the sensor is active.
   */
  var active: Boolean

  /**
   * Get a supported sensor channel detail of this sensor detail.
   * 
   * <p>
   * The channel must be a channel of this sensor detail to be found. Channel names are local
   * to the detail they are contained in.
   * 
   * @param id
   *     the ID of the channel detail
   *
   * @return the channel detail
   */
  def getSupportedSensorChannelDetail(id: String): Option[SensorChannelDetailDescription]

  /**
   * Does the sensor have a channel with the given ID?
   * 
   * <p>
   * The channel must be a channel of this sensor detail to be found. Channel names are local
   * to the detail they are contained in.
   * 
   * @param channelId
   *     the ID of the channel detail
   *
   * @return [[true]] if there is a channel with the given ID
   */
  def hasSupportedSensorChannel(channelId: String): Boolean

  /**
   * Does the sensor return a given measurement type?
   *
   * @return [[true]] if the sensor has a given measurement type
   */
  def hasSupportedMeasurementType(measurementTypeExternalId: String): Boolean

  /**
   * Get all channels giving a particular measurement type.
   *
   * @return all channels giving a particular measurement type
   */
  def getSupportedMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription]
}

/**
 * The standard sensor entity description.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityDescription(
  id: String,
  externalId: String,
  displayName: String,
  displayDescription: Option[String],
  override val sensorType: SensorTypeDescription,
  override val dataSource: DataSourceDescription,
  override val stateUpdateTimeLimit: Option[Long],
  override val heartbeatUpdateTimeLimit: Option[Long]) extends SimpleEntityDescription(id, externalId, displayName, displayDescription) with SensorEntityDescription {

  override var active: Boolean = true

  override def getSupportedSensorChannelDetail(id: String): Option[SensorChannelDetailDescription] = {
    sensorType.getSupportedSensorChannelDetail(id)
  }
  
  override def hasSupportedSensorChannel(channelId: String): Boolean = {
    sensorType.hasSupportedSensorChannel(channelId)
  }

  override def hasSupportedMeasurementType(measurementTypeExternalId: String): Boolean = {
    sensorType.hasSupportedMeasurementType(measurementTypeExternalId)
  }

  override def getSupportedMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription] = {
    sensorType.getSupportedMeasurementTypeChannels(measurementTypeExternalId)
  }
} 
