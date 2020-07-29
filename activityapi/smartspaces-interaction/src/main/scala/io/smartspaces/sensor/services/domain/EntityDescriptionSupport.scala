/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.sensor.services.domain

import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.domain.SensorTypeDescription
import io.smartspaces.sensor.domain.SensorChannelDetailDescription

/**
 * A collection of methods to support working with entity descriptions.
 *
 * @author Keith M. Hughes
 */
object EntityDescriptionSupport {

  /**
   * Get the valid channel IDs for the sensor from the channel ID description.
   *
   * Only the supported channels will be examined.
   *
   * @param sensor
   *        the sensor to get the channel IDs from
   * @param sensorChannelIdDescription
   *        the description of channel IDs
   */
  def getSensorChannelIdsFromSensorDescription(sensor: SensorEntityDescription, sensorChannelIdDescription: String): Iterable[String] = {
    getSensorChannelIdsFromSensorTypeDescription(sensor.sensorType, sensorChannelIdDescription)
  }
  
  /**
   * Get the valid channel IDs for the sensor from the channel ID description.
   *
   * Only the supported channels will be examined.
   *
   * @param sensorType
   *        the sensor type to get the channel IDs from
   * @param sensorChannelIdDescription
   *        the description of channel IDs
   */
  def getSensorChannelIdsFromSensorTypeDescription(sensorType: SensorTypeDescription, sensorChannelIdDescription: String): Iterable[String] = {
     getSensorChannelIdsFromSensorChannelDetailDescription(sensorType.supportedChannelDetails, sensorChannelIdDescription)
  }
  
  /**
   * Get the valid channel IDs for the sensor from the channel ID description.
   *
   * @param sensorChannelDetails
   *        the sensor channel details from a sensor
   * @param sensorChannelIdDescription
   *        the description of channel IDs
   */
  def getSensorChannelIdsFromSensorChannelDetailDescription(sensorChannelDetails: Iterable[SensorChannelDetailDescription], sensorChannelIdDescription: String): Iterable[String] = {
    val allChannelIds = sensorChannelDetails.map(_.channelId)
    getSensorChannelIdsFromDescriptionAndSource(allChannelIds, sensorChannelIdDescription)
  }
 
  /**
   * Get the valid channel IDs for the sensor from the channel ID description.
   *
   * @param sourceSensorChannelIds
   *        the IDs for the source sensor channels
   * @param sensorChannelIdDescription
   *        the description of channel IDs
   */
  def getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds: Iterable[String], sensorChannelIdDescription: String): Iterable[String] = {
    if (sensorChannelIdDescription == "*") {
      sourceSensorChannelIds
    } else if (sensorChannelIdDescription.startsWith("-")) {
      val toBeRemoved = sensorChannelIdDescription.substring(1).split(':').toSet
      sourceSensorChannelIds.filter(!toBeRemoved.contains(_))
    } else {
      sensorChannelIdDescription.split(':').toList.filter(id => sourceSensorChannelIds.exists(_ == id))
    }
  }
}