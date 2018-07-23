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

/**
 * A collection of methods to support working with entity descriptions.
 *
 * @author Keith M. Hughes
 */
object EntityDescriptionSupport {

  /**
   * Get the valid channel IDs for the sensor from the channel ID description.
   * 
   * @param sensor
   *        the sensor to get the channel IDs from
   * @param sensorChannelIdDescription
   *        the description of channel IDs
   */
  def getSensorChannelIdsFromDescription(sensor: SensorEntityDescription, sensorChannelIdDescription: String): Iterable[String] = {
    var allChannelIds = sensor.sensorType.getAllSensorChannelDetails().map(_.channelId)
    if (sensorChannelIdDescription == "*" || sensorChannelIdDescription.startsWith("-")) {
      if (sensorChannelIdDescription == "*") {
        allChannelIds
      } else {
        var toBeRemoved = sensorChannelIdDescription.substring(1).split(':').toSet
        allChannelIds.filter(!toBeRemoved.contains(_))
      }
    } else {
      sensorChannelIdDescription.split(':').toList
    }
  }
}