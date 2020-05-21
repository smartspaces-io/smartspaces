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
 * Type information about a sensor.
 *
 * @author Keith M. Hughes
 */
trait SensorTypeDescription extends DisplayableDescription {

  /**
   * The ID of the sensor detail.
   */
  def id: String

  /**
   * The external ID of the sensor detail.
   */
  def externalId: String

  /**
   * The usage category of the sensor.
   */
  def usageCategory: Option[String]

  /**
   * The source of the data, e.g. SmartThings, internal, etc.
   */
  def dataSources: Iterable[DataSourceTypeDescription]

  /**
   * The time limit on when a sensor update should happen, in milliseconds
   */
  def sensorUpdateTimeLimit: Option[Long]

  /**
   * The time limit on when a sensor heartbeat update should happen, in milliseconds
   */
  def sensorHeartbeatUpdateTimeLimit: Option[Long]

  /**
   * The optional manufacturer's name.
   */
  def sensorManufacturerName: Option[String]

  /**
   * The optional manufacturer's model.
   */
  def sensorManufacturerModel: Option[String]

  /**
   * The supported channel IDs.
   */
  def supportedChannelIds: String

  /**
   * All channel details for the sensor type.
   */
  def allChannelDetails: List[SensorChannelDetailDescription]

  /**
   * All supported channel details for the sensor type.
   */
  def supportedChannelDetails: List[SensorChannelDetailDescription]

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
   * Does the sensor type have a supported channel with the given ID?
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
   * Does the sensor support returning a given measurement type?
   *
   * @return [[true]] if the sensor has a given measurement type
   */
  def hasSupportedMeasurementType(measurementTypeExternalId: String): Boolean

  /**
   *  Get all supported sensor channel descriptions for a given measurement type.
   *
   * @return all channels with a given measurement type
   */
  def getSupportedMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription]
}

/**
 * Details about a sensor.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorTypeDescription(
                                        override val id: String,
                                        override val externalId: String,
                                        override val displayName: String,
                                        override val displayDescription: Option[String],
                                        override val sensorUpdateTimeLimit: Option[Long],
                                        override val sensorHeartbeatUpdateTimeLimit: Option[Long],
                                        override val usageCategory: Option[String],
                                        override val dataSources: Iterable[DataSourceTypeDescription],
                                        override val sensorManufacturerName: Option[String],
                                        override val sensorManufacturerModel: Option[String],
                                        override val supportedChannelIds: String,
                                        override val allChannelDetails: List[SensorChannelDetailDescription],
                                        override val supportedChannelDetails: List[SensorChannelDetailDescription]
    ) extends SensorTypeDescription {

  override def getSupportedSensorChannelDetail(id: String): Option[SensorChannelDetailDescription] = {
    supportedChannelDetails.find(_.channelId == id)
  }

  override def hasSupportedSensorChannel(channelId: String): Boolean = {
    supportedChannelDetails.find(_.channelId == channelId).isDefined
  }
  
  override def hasSupportedMeasurementType(measurementTypeExternalId: String): Boolean = {
    supportedChannelDetails.find(_.measurementType.externalId == measurementTypeExternalId).isDefined
  }
  
  override def getSupportedMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription] = {
    supportedChannelDetails.filter(_.measurementType.externalId == measurementTypeExternalId)
  }
}
