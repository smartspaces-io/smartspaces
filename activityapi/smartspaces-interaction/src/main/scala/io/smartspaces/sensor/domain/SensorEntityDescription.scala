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
  val sensorType: SensorTypeDescription

  /**
   * The source of the sensor, e.g. from an external sensor relay and the name it has in that relay.
   */
  val sensorSource: String

  /**
   * The time limit on when a sensor update should happen, in milliseconds
   */
  val sensorStateUpdateTimeLimit: Option[Long]

  /**
   * The time limit on when a sensor heartbeat update should happen, in milliseconds
   */
  val sensorHeartbeatUpdateTimeLimit: Option[Long]

  /**
   * {@code true} if the sensor is active.
   */
  var active: Boolean

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
  def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription]
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
  override val sensorSource: String,
  override val sensorStateUpdateTimeLimit: Option[Long],
  override val sensorHeartbeatUpdateTimeLimit: Option[Long]) extends SimpleEntityDescription(id, externalId, displayName, displayDescription) with SensorEntityDescription {

  override var active: Boolean = true

  override def hasMeasurementType(measurementTypeExternalId: String): Boolean = {
    sensorType.hasMeasurementType(measurementTypeExternalId)
  }

  override def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription] = {
    sensorType.getMeasurementTypeChannels(measurementTypeExternalId)
  }
} 
