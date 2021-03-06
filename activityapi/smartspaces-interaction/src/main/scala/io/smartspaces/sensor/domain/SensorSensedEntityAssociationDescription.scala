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

package io.smartspaces.sensor.domain

/**
 * An association between a sensor channel and the entity it senses.
 *
 * @author Keith M. Hughes
 */
trait SensorSensedEntityAssociationDescription {

  /**
   * The sensor description.
   */
  def sensor: SensorEntityDescription

  /**
   * The sensor channel from the sensor to be associated with the sensed entity.
   */
  def sensorChannelDetail: SensorChannelDetailDescription

  /**
   * The entity being sensed by the sensor channel.
   */
  def sensedEntity: SensedEntityDescription

  /**
   * The time limit on when a sensor update should happen, in milliseconds
   */
  def stateUpdateTimeLimit: Option[Long]

  /**
   * The time limit on when a sensor heartbeat update should happen, in milliseconds
   */
  def heartbeatUpdateTimeLimit: Option[Long]
}

/**
 * An association between a sensor channel and the entity it senses.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorSensedEntityAssociationDescription(
  override val sensor: SensorEntityDescription,
  override val sensorChannelDetail: SensorChannelDetailDescription,
  override val sensedEntity: SensedEntityDescription,
  override val stateUpdateTimeLimit: Option[Long],
  override val heartbeatUpdateTimeLimit: Option[Long]) extends SensorSensedEntityAssociationDescription
