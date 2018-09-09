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

import io.smartspaces.sensor.domain.SensorEntityDescription

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.monitor.expectation.time.StandardHeartbeatMonitorable
import io.smartspaces.sensor.event.SensorOnlineEvent

/**
 * The model of a sensor.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityModel(
  val sensorEntityDescription: SensorEntityDescription,
  val allModels: CompleteSensedEntityModel,
  override val timestampItemCreation: Long) extends SensorEntityModel with StandardHeartbeatMonitorable {

  /**
   * The sensor channel models indexed by the channel ID.
   */
  private val sensorChannelModels: Map[String, SensorChannelEntityModel] = new HashMap

  override def addSensorChannelModel(sensorChannelModel: SensorChannelEntityModel): Unit = {
    sensorChannelModels.put(sensorChannelModel.sensorChannelDetail.channelId, sensorChannelModel)
  }

  override def getAllSensorChannelModels(): Iterable[SensorChannelEntityModel] = {
    sensorChannelModels.values
  }

  override def getSensorChannelEntityModel(channelId: String): Option[SensorChannelEntityModel] = {
    sensorChannelModels.get(channelId)
  }

  override def hasSensorChannel(channelId: String): Boolean = {
    sensorChannelModels.contains(channelId)
  }

  override def hasMeasurementType(measurementTypeExternalId: String): Boolean = {
    sensorChannelModels.values.find(_.sensorChannelDetail.measurementType.externalId == measurementTypeExternalId).isDefined
  }

  override def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelEntityModel] = {
    sensorChannelModels.values.filter(_.sensorChannelDetail.measurementType.externalId == measurementTypeExternalId)
  }

  override def getSensedValue(measurementTypeExternalId: String): Option[SensedValue[Any]] = {
    // TODO(keith): Needs some sort of concurrency block
    val channels = getMeasurementTypeChannels(measurementTypeExternalId)
    if (channels.isEmpty) {
      None
    } else {
      Some(channels.head.mostRecentSensedValue)
    }
  }

  override def getAllSensedValues(): Iterable[SensedValue[Any]] = {
    sensorChannelModels.values.map(_.mostRecentSensedValue)
  }

  override def updateSensedValue[T <: Any](value: SensedValue[T], timestamp: Long): Unit = {
    stateUpdated(timestamp)
  }

  override def emitOnlineEvent(timestamp: Long): Unit = {
    allModels.eventEmitter.broadcastSensorOnlineEvent(new SensorOnlineEvent(this, timestamp))
  }

  override def emitOfflineEvent(timestamp: Long): Unit = {
    allModels.eventEmitter.broadcastSensorOfflineEvent(new SensorOfflineEvent(this, timestamp))
  }

  override def stateUpdateTimeLimit: Option[Long] = sensorEntityDescription.sensorStateUpdateTimeLimit

  override def heartbeatUpdateTimeLimit: Option[Long] = sensorEntityDescription.sensorHeartbeatUpdateTimeLimit
}