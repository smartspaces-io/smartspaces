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

package io.smartspaces.sensor.event

import io.smartspaces.data.entity.CategoricalValueInstance

/**
 * A persisted raw sensor event.
 * 
 * @author Keith M. Hughes
 */
trait RawSensorPersistedEvent[+T <: Any] {
  val sensorExternalId: String
  val sensedEntityExternalId: String
  val channelId: String
  val measurementTypeId: String
  val value: T
  val additional: Option[Any]
  val measurementTimestamp: Long 
  val sensorMessageReceivedTimestamp: Long
  
  /**
   * Get a representation for the value that can be written out.
   * 
   * @return the exportable value
   */
  def exportableValue(): Any
  
  /**
   * Get a representation for the additional value that can be written out.
   * 
   * @return the exportable additional value
   */
  def exportableAdditional(): Any
}

/**
 * A persisted raw sensor event that has a continuous value.
 * 
 * @author Keith M. Hughes
 */
class NumericContinuousRawSensorPersistedEvent(
  override val sensorExternalId: String,
  override val sensedEntityExternalId: String,
  override val channelId: String,
  override val measurementTypeId: String,
  override val value: Double,
  override val additional: Option[Any],
  override val measurementTimestamp: Long,
  override val sensorMessageReceivedTimestamp: Long)
    extends RawSensorPersistedEvent[Double] {
  override def exportableValue(): Any = value
  
  override def exportableAdditional(): Any = additional.getOrElse("NA")
  
  override def toString(): String = {
    s"sensorExternalId=${sensorExternalId}, sensedEntityExternalId=${sensedEntityExternalId} channelId=${channelId}, value=${value}, measurementTimestamp=${measurementTimestamp}"
  }
}

/**
 * A persisted raw sensor event that has a string value.
 * 
 * @author Keith M. Hughes
 */
class StringRawSensorPersistedEvent(
  override val sensorExternalId: String,
  override val sensedEntityExternalId: String,
  override val channelId: String,
  override val measurementTypeId: String,
  override val value: String,
  override val additional: Option[Any],
  override val measurementTimestamp: Long,
  override val sensorMessageReceivedTimestamp: Long)
    extends RawSensorPersistedEvent[String] {
  override def exportableValue(): Any = value
  
  override def exportableAdditional(): Any = additional.getOrElse("NA")
  
  override def toString(): String = {
    s"sensorExternalId=${sensorExternalId}, sensedEntityExternalId=${sensedEntityExternalId} channelId=${channelId}, value=${value}, measurementTimestamp=${measurementTimestamp}"
  }
}

/**
 * A persisted raw sensor event that has a categorical value.
 * 
 * @author Keith M. Hughes
 */
class CategoricalValueRawSensorPersistedEvent(
  override val sensorExternalId: String,
  override val sensedEntityExternalId: String,
  override val channelId: String,
  override val measurementTypeId: String,
  override val value: CategoricalValueInstance,
  override val additional: Option[Any],
  override val measurementTimestamp: Long,
  override val sensorMessageReceivedTimestamp: Long)
    extends RawSensorPersistedEvent[CategoricalValueInstance] {
  override def exportableValue(): Any = value.label
  
  override def exportableAdditional(): Any = additional.getOrElse("NA")
  
  override def toString(): String = {
    s"sensorExternalId=${sensorExternalId}, sensedEntityExternalId=${sensedEntityExternalId} channelId=${channelId}, value=${value.label}, measurementTimestamp=${measurementTimestamp}"
  }
}
