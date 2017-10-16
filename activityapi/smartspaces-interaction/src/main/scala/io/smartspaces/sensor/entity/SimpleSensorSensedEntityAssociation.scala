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

/**
 * An association between a sensor channel and the entity it senses.
 *
 * @author Keith M. Hughes
 */
trait SensorSensedEntityAssociation {

  /**
   * The sensor description.
   */
  val sensor: SensorEntityDescription

  /**
   * The sensor channel from the sensor to be associated with the sensed entity.
   */
  val sensorChannelDetail: SensorChannelDetail

  /**
   * The entity being sensed by the sensor channel.
   */
  val sensedEntity: SensedEntityDescription
}

/**
 * An association between a sensor channel and the entity it senses.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorSensedEntityAssociation(

  override val sensor: SensorEntityDescription,

  override val sensorChannelDetail: SensorChannelDetail,

  override val sensedEntity: SensedEntityDescription
) extends SensorSensedEntityAssociation
