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

package io.smartspaces.sensor.entity.model

import io.smartspaces.sensor.entity.SensorEntityDescription

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.sensor.entity.model.event.SensorOfflineEvent
import io.smartspaces.monitor.expectation.time.StandardHeartbeatMonitorable

/**
 * The model of a sensor.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityModel(
    val sensorEntityDescription: SensorEntityDescription, 
    val allModels: CompleteSensedEntityModel, 
    override val itemCreationTime: Long) extends SensorEntityModel with StandardHeartbeatMonitorable {

  /**
   * The values being sensed keyed by the value name.
   */
  private val sensedValues: Map[String, SensedValue[Any]] = new HashMap

  /**
   * The sensor channel models indexed by the channel ID.
   */
  private val sensorChannelModels: Map[String, SensorChannelEntityModel] = new HashMap

  /**
   * The model that is being sensed by this sensor.
   */
  var sensedEntityModel: Option[SensedEntityModel] = None

  override def addSensorChannelModel(sensorChannelModel: SensorChannelEntityModel): Unit = {
    sensorChannelModels.put(sensorChannelModel.sensorChannelDetail.channelId, sensorChannelModel)
  }
  
  override def getSensorChannelEntityModel(channelId: String): Option[SensorChannelEntityModel] = {
    sensorChannelModels.get(channelId)
  }

  override def getSensedValue(valueTypeId: String): Option[SensedValue[Any]] = {
    // TODO(keith): Needs some sort of concurrency block
    sensedValues.get(valueTypeId)
  }

  override def getAllSensedValues(): scala.collection.immutable.List[SensedValue[Any]] = {
    sensedValues.values.toList
  }

  override def updateSensedValue[T <: Any](value: SensedValue[T], timestamp: Long): Unit = {
    stateUpdated(timestamp)

    sensedValues.put(value.measurementTypeDescription.externalId, value)
  }
  
  override def checkIfOfflineTransition(currentTime: Long): Boolean = {
    // Only check if the model thinks it is online and there was an update time,
    // otherwise we want the initial 
    if (_online) {
      val updateTimeLimit = sensorEntityDescription.sensorUpdateTimeLimit
      if (updateTimeLimit.isDefined) {
        // The only way we would ever be considered online is if there was a lastUpdate,
        // so the .get will work.
        _online = !isTimeout(currentTime, _lastUpdateTime.get, updateTimeLimit.get)
      } else if (sensorEntityDescription.sensorHeartbeatUpdateTimeLimit.isDefined) {
        // If this sensor requires a heartbeat, the heartbeat time can be checked.
        
        // Would be lovely to have a magic function that could calculate the max of a series of options.
        val sensorHeartbeatUpdateTimeLimit = sensorEntityDescription.sensorHeartbeatUpdateTimeLimit

        val updateToUse = if (_lastUpdateTime.isDefined) { if (_lastHeartbeatUpdate.isDefined) Math.max(_lastUpdateTime.get, _lastHeartbeatUpdate.get) else _lastUpdateTime.get } else _lastHeartbeatUpdate.get
        _online = !isTimeout(currentTime, updateToUse, sensorHeartbeatUpdateTimeLimit.get)
      }

      // We knew online was true, so if now offline, then transitioned.
      if (!_online) {
        signalOffline(currentTime)
      }
      
      !_online
    } else {
      // Now, we are considered offline. If we have never been updated then we can check at the
      // time of birth of the model. otherwise no need to check.
      if (!offlineSignaled) {
        val sensorUpdateTimeLimit = sensorEntityDescription.sensorUpdateTimeLimit
        if (sensorUpdateTimeLimit.isDefined) {
          if (isTimeout(currentTime, _lastUpdateTime.getOrElse(itemCreationTime), sensorUpdateTimeLimit.get)) {
            signalOffline(currentTime)
            
            true
          } else {
            false
          }
        } else if (sensorEntityDescription.sensorHeartbeatUpdateTimeLimit.isDefined) {
          // If this sensor requires a heartbeat, the heartbeat time can be checked.
          val sensorHeartbeatUpdateTimeLimit = sensorEntityDescription.sensorHeartbeatUpdateTimeLimit
          if (isTimeout(currentTime, _lastHeartbeatUpdate.getOrElse(itemCreationTime), sensorHeartbeatUpdateTimeLimit.get)) {
            signalOffline(currentTime)
            
            true
          } else {
            false
          }
        } else {
          false
        }
      } else {
        false
      }
    }
  }

  /**
   * Signal that the sensor has gone offline.
   *
   * @param currentTime
   * 		the time when the sensor was detected offline
   */
  private def signalOffline(currentTime: Long): Unit = {
    offlineSignaled = true
    allModels.eventEmitter.broadcastSensorOfflineEvent(new SensorOfflineEvent(this, currentTime))
  }
}