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

import io.smartspaces.sensor.domain.SensedEntityDescription

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * A very simple model of a sensed entity.
 *
 * @author Keith M. Hughes
 */
trait BaseSensedEntityModel extends SensedEntityModel {

  /**
   * The sensor channel models indexed by the channel ID.
   */
  private val sensorChannelModels: Map[String, SensorChannelEntityModel] = new HashMap

  /**
   * The time of the last update.
   */
  private var _timestampLastUpdate: Option[Long] = None
  
  override def addSensorChannelModel(sensorChannelModel: SensorChannelEntityModel): Unit = {
    sensorChannelModels.put(sensorChannelModel.sensorChannelDetail.channelId, sensorChannelModel)
  }
  
  override def getAllSensorChannelModels(): Traversable[SensorChannelEntityModel] = {
    sensorChannelModels.values
  }

  override def getSensorChannelEntityModel(channelId: String): Option[SensorChannelEntityModel] = {
    sensorChannelModels.get(channelId)
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
    // TODO(keith): Needs some sort of concurrency block
    _timestampLastUpdate = Some(timestamp)
  }
  
  override def timestampLastUpdate: Option[Long] = {
    _timestampLastUpdate
  }
}


/**
 * A very simple model of a sensed entity.
 *
 * @author Keith M. Hughes
 */
class SimpleSensedEntityModel(override val sensedEntityDescription: SensedEntityDescription,
    override val allModels: CompleteSensedEntityModel) extends BaseSensedEntityModel {
  
  type SensedEntityDescriptionType = SensedEntityDescription
}

