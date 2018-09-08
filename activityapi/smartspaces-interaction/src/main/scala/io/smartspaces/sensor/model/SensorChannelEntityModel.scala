/** Copyright (C) 2017 Keith M. Hughes
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

import io.smartspaces.sensor.domain.SensorChannelDetailDescription
import io.smartspaces.sensor.services.processing.value.SensorValueProcessor

/**
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 * 
 * @author Keith M. Hughes
 */
trait SensorChannelEntityModel {
  
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
}

/*
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 * 
 * @author Keith M. Hughes
 */
class SimpleSensorChannelEntityModel(
  override val sensorModel: SensorEntityModel,
  override val sensorChannelDetail: SensorChannelDetailDescription,
  override val sensedEntityModel: SensedEntityModel,
  override val sensorValueProcessor: SensorValueProcessor
) extends SensorChannelEntityModel {
    
  override def toString(): String = {
    s"sensrModel: ${sensorModel.sensorEntityDescription.externalId}, sensorChannelDetail: ${sensorChannelDetail.channelId}, sensedEntityModel: ${sensedEntityModel.sensedEntityDescription.externalId}"
  }
}
