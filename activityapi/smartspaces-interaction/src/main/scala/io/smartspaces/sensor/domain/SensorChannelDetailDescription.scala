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
 * Details about a sensor channel.
 *
 * <p>
 * The ID of the channel is local to the sensor detail it is part of. For example, multiple
 * sensor detail items could contain a channel called temperature.
 *
 * <p>
 * The type of the channel specifies the sort of measurement the channel supplies.
 *
 * <p>
 * The units of the channel specify the units the channel is supplying the measurement in.
 *
 * @author Keith M. Hughes
 */
trait SensorChannelDetailDescription extends DisplayableDescription {

  /**
   * The ID of the channel.
   */
  def channelId: String

  /**
   * The type of the channel measurement.
   */
  def measurementType: MeasurementTypeDescription

  /**
   * The unit being used for the channel measurement
   */
  def measurementUnit: Option[MeasurementUnitDescription]

  /**
   * The time limit on when a sensor channel update should happen, in milliseconds
   */
  def stateUpdateTimeLimit: Option[Long]

  /**
   * The time limit on when a sensor channel heartbeat update should happen, in milliseconds
   */
  def heartbeatUpdateTimeLimit: Option[Long]
}

/**
 * Details about a sensor channel.
 *
 * <p>
 * The ID of the channel is local to the sensor detail it is part of. For example, multiple
 * sensor detail items could contain a channel called temperature.
 *
 * <p>
 * The type of the channel specifies the sort of measurement the channel supplies.
 *
 * <p>
 * The units of the channel specify the units the channel is supplying the measurement in.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorChannelDetailDescription(
  override val channelId: String,
  override val displayName: String,
  override val displayDescription: Option[String],
  override val measurementType: MeasurementTypeDescription,
  override val measurementUnit: Option[MeasurementUnitDescription],
  override val stateUpdateTimeLimit: Option[Long],
  override val heartbeatUpdateTimeLimit: Option[Long]) extends SensorChannelDetailDescription
