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

package io.smartspaces.sensor.entity

import scala.collection.mutable.ArrayBuffer

/**
 * Details about a sensor.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorDetail(val id: String, val externalId: String, val displayName: String, val displayDescription: String, val sensorUpdateTimeLimit: Option[Long], val sensorHeartbeatUpdateTimeLimit: Option[Long]) extends SensorDetail {

  /**
   * The measurement units for this type.
   */
  private val channelDetails: ArrayBuffer[SensorChannelDetail] = new ArrayBuffer

  override def addSensorChannelDetail(sensorChannelDetail: SensorChannelDetail): Unit = {
    channelDetails += sensorChannelDetail
  }

  override def getAllSensorChannelDetails(): List[SensorChannelDetail] = {
    channelDetails.toList
  }

  override def getSensorChannelDetail(id: String): Option[SensorChannelDetail] = {
    channelDetails.find(_.channelId == id)
  }
}
