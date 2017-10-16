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

package io.smartspaces.sensor.entity.model

import io.smartspaces.sensor.entity.SensorChannelDetail

/**
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 * 
 * @author Keith M. Hughes
 */
trait SensorChannelEntityModel {
  
  /**
   * The detail of the sensor channel.
   */
  val sensorChannelDetail: SensorChannelDetail
  
  /**
   * The sensor model for this channel.
   */
  val sensorModel: SensorEntityModel
  
  /**
   * The model of the entity being sensed.
   */
  val sensedEntityModel: SensedEntityModel
}

/*
 * The entity model of a sensor channel and the sensed entity the channel is associated with.
 * 
 * @author Keith M. Hughes
 */
class SimpleSensorChannelEntityModel(
  override val sensorModel: SensorEntityModel,
  override val sensorChannelDetail: SensorChannelDetail,
  override val sensedEntityModel: SensedEntityModel
) extends SensorChannelEntityModel 
