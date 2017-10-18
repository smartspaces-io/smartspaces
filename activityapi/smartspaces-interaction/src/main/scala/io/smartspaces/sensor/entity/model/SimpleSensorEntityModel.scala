/*
O * Copyright (C) 2016 Keith M. Hughes
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

/**
 * The model of a sensor.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityModel(val sensorEntityDescription: SensorEntityDescription, val allModels: CompleteSensedEntityModel, val modelCreationTime: Long) extends SensorEntityModel {

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

  /**
   * Is the sensor online?
   *
   * <p>
   * Assume that it is offline until told otherwise.
   */
  var online: Boolean = false

  /**
   * {@code true} if there has been a signaling of going offline.
   */
  private var offlineSignaled: Boolean = false

  /**
   * The time of the last update.
   */
  private var lastUpdate: Option[Long] = None

  /**
   * The time of the last heartbeat update.
   */
  private var lastHeartbeatUpdate: Option[Long] = None

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
    updateSensedValue(timestamp)

    sensedValues.put(value.measurementTypeDescription.externalId, value)
  }

  override def updateSensedValue(timestamp: Long): Unit = {
    lastUpdate = Option(timestamp)

    updateHappened
  }

  override def getLastUpdate(): Option[Long] = {
    lastUpdate
  }

  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  private[model] def setLastUpdateTime(time: Long): Unit = {
    lastUpdate = Option(time)
  }
  override def updateHeartbeat(timestamp: Long): Unit = {
    lastHeartbeatUpdate = Option(timestamp)

    updateHappened
  }

  override def getLastHeartbeatUpdate(): Option[Long] = {
    lastHeartbeatUpdate
  }

  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  private[model] def setLastHeartbeatUpdateTime(time: Long): Unit = {
    lastHeartbeatUpdate = Option(time)
  }

  override def checkIfOfflineTransition(currentTime: Long): Unit = {
    // Only check if the model thinks it is online and there was an update time,
    // otherwise we want the initial 
    if (online) {
      val sensorUpdateTimeLimit = sensorEntityDescription.sensorUpdateTimeLimit
      if (sensorUpdateTimeLimit.isDefined) {
        // The only way we would ever be considered online is if there was a lastUpdate,
        // so the .get will work.
        online = !isTimeout(currentTime, lastUpdate.get, sensorUpdateTimeLimit.get)
      } else if (sensorEntityDescription.sensorHeartbeatUpdateTimeLimit.isDefined) {
        // If this sensor requires a heartbeat, the heartbeat time can be checked.
        
        // Would be lovely to have a magic function that could calculate the max of a series of options.
        val sensorHeartbeatUpdateTimeLimit = sensorEntityDescription.sensorHeartbeatUpdateTimeLimit

        val updateToUse = if (lastUpdate.isDefined) { if (lastHeartbeatUpdate.isDefined) Math.max(lastUpdate.get, lastHeartbeatUpdate.get) else lastUpdate.get } else lastHeartbeatUpdate.get
        online = !isTimeout(currentTime, updateToUse, sensorHeartbeatUpdateTimeLimit.get)
      }

      // We knew online was true, so if now offline, then transitioned.
      if (!online) {
        signalOffline(currentTime)
      }
    } else {
      // Now, we are considered offline. If we have never been updated then we can check at the
      // time of birth of the model. otherwise no need to check.
      if (!offlineSignaled) {
        val sensorUpdateTimeLimit = sensorEntityDescription.sensorUpdateTimeLimit
        if (sensorUpdateTimeLimit.isDefined) {
          if (isTimeout(currentTime, lastUpdate.getOrElse(modelCreationTime), sensorUpdateTimeLimit.get)) {
            signalOffline(currentTime)
          }
        } else if (sensorEntityDescription.sensorHeartbeatUpdateTimeLimit.isDefined){
          // If this sensor requires a heartbeat, the heartbeat time can be checked.
          println(sensorEntityDescription)
          val sensorHeartbeatUpdateTimeLimit = sensorEntityDescription.sensorHeartbeatUpdateTimeLimit
          if (isTimeout(currentTime, lastHeartbeatUpdate.getOrElse(modelCreationTime), sensorHeartbeatUpdateTimeLimit.get)) {
            signalOffline(currentTime)
          }
        }
      }
    }
  }

  /**
   * Calculate a timeout online status based on time calculations.
   *
   * @param currentTime
   *        the current time
   * @param referenceTime
   *        the time to be compared to, such as last update time or model creation time
   * @param timeLimit
   *        the maximum amount of time before it is decided to be offline
   *
   * @return {@code true} if there has been a timeout
   */
  private def isTimeout(currentTime: Long, referenceTime: Long, timeLimit: Long): Boolean = {
    currentTime - referenceTime > timeLimit
  }

  /**
   * An update happened.
   */
  private def updateHappened(): Unit = {
    // The online status is definitely true if an update is coming in.
    offlineSignaled = false
    online = true
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