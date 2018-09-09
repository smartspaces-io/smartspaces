/**
 * Copyright (C) 2017 Keith M. Hughes
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

import io.smartspaces.monitor.expectation.time.HeartbeatMonitorable
import io.smartspaces.monitor.expectation.time.StandardHeartbeatMonitorable
import io.smartspaces.sensor.domain.SensorChannelDetailDescription
import io.smartspaces.sensor.event.SensorChannelOfflineEvent
import io.smartspaces.sensor.event.SensorChannelOnlineEvent
import io.smartspaces.sensor.services.processing.value.SensorValueProcessor

/**
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 *
 * @author Keith M. Hughes
 */
trait SensorChannelEntityModel extends HeartbeatMonitorable {

  /**
   * The detail of the sensor channel.
   */
  def sensorChannelDetail: SensorChannelDetailDescription

  /**
   * The sensor model for this channel.
   */
  def sensorModel: SensorEntityModel

  /**
   * The model of the entity being sensed.
   */
  def sensedEntityModel: SensedEntityModel

  /**
   * The processor for sensor values for this channel.
   */
  def sensorValueProcessor: SensorValueProcessor

  /**
   * Update a sensed value.
   *
   * @param value
   *          the value being updated
   * @param timestampUpdate
   * 		      the timestamp of this update
   */
  def updateSensedValue[T <: Any](value: SensedValue[T], timestampUpdate: Long): Unit

  /**
   * The most recent sensed value for this channel.
   */
  def mostRecentSensedValue: SensedValue[Any]
}

/*
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorChannelEntityModel(
  override val sensorChannelDetail: SensorChannelDetailDescription,
  override val sensorModel: SensorEntityModel,
  override val sensedEntityModel: SensedEntityModel,
  override val sensorValueProcessor: SensorValueProcessor,
  val allModels: CompleteSensedEntityModel,
  override val timestampItemCreation: Long) extends SensorChannelEntityModel with StandardHeartbeatMonitorable {

  /**
   * The most recent sensed value for this channel.
   */
  private var _mostRecentSensedValue: SensedValue[Any] = _

  override def updateSensedValue[T <: Any](value: SensedValue[T], timestampUpdate: Long): Unit = {
    synchronized {
      _mostRecentSensedValue = value
    }
    
    // ??? Update in time between channel and the sensor and sensed sending potential online events.
    stateUpdated(timestampUpdate)
    
    sensorModel.updateSensedValue(value, timestampUpdate)
    sensedEntityModel.updateSensedValue(value, timestampUpdate)
  }

  override  def mostRecentSensedValue: SensedValue[Any] = {
    synchronized {
      _mostRecentSensedValue
    }
  }

  override def stateUpdateTimeLimit: Option[Long] = sensorChannelDetail.stateUpdateTimeLimit

  override def heartbeatUpdateTimeLimit: Option[Long] = sensorChannelDetail.heartbeatUpdateTimeLimit

  override def emitOnlineEvent(timestamp: Long): Unit = {
    allModels.eventEmitter.broadcastSensorChannelOnlineEvent(new SensorChannelOnlineEvent(this, timestamp))
  }

  override def emitOfflineEvent(timestamp: Long): Unit = {
    allModels.eventEmitter.broadcastSensorChannelOfflineEvent(new SensorChannelOfflineEvent(this, timestamp))
  }

  override def toString(): String = {
    s"sensrModel: ${sensorModel.sensorEntityDescription.externalId}, sensorChannelDetail: ${sensorChannelDetail.channelId}, sensedEntityModel: ${sensedEntityModel.sensedEntityDescription.externalId}"
  }
}
