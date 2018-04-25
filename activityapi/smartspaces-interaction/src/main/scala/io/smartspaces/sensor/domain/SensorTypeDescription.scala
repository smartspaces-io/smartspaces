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

import scala.collection.mutable.ArrayBuffer

import io.smartspaces.data.entity.BaseCategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValueInstance
import io.smartspaces.data.entity.CategoricalValue
import io.smartspaces.data.entity.CategoricalValueInstance

/**
 * The activity categorical value.
 * 
 * @author Keith M. Hughes
 */
final object SensorAcquisitionModeCategoricalValue extends BaseCategoricalValue[
  SensorAcquisitionModeCategoricalValueInstances.SensorAcquisitionModeCategoricalValueInstance]( 
    "sensorAcquisitionType", List(SensorAcquisitionModeCategoricalValueInstances.PULL, SensorAcquisitionModeCategoricalValueInstances.PUSH)) {  
}

/**
 * All categorical value instances for how a sensor acquires data..
 * 
 * @author Keith M. Hughes
 */
object SensorAcquisitionModeCategoricalValueInstances {
  
  /**
   * Base class for the sensor acquisition type categorical variable instances.
   * 
   * @author Keith M. Hughes
   */
  sealed abstract class SensorAcquisitionModeCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
    override def value: CategoricalValue[CategoricalValueInstance] = SensorAcquisitionModeCategoricalValue
  }
  
  /**
   * The data must be pulled in.
   */
  final object PULL extends SensorAcquisitionModeCategoricalValueInstance(0, "PULL")
  
  /**
   * The data is pushed to the processor.
   */
  final object PUSH extends SensorAcquisitionModeCategoricalValueInstance(1, "PUSH")
}

/**
 * Type information about a sensor.
 * 
 * @author Keith M. Hughes
 */
trait SensorTypeDescription extends DisplayableDescription {
  
  /**
   * The ID of the sensor detail.
   */
  val id: String
  
  /**
   * The external ID of the sensor detail.
   */
  val externalId: String
  
  /**
   * The usage category of the sensor.
   */
  val usageCategory: Option[String]
  
  /**
   * The source of the data, e.g. SmartThings, internal, etc.
   */
  val dataSource: Option[String]
  
  /**
   * How the sensor acquires its data.
   */
  val acquisitionMode: SensorAcquisitionModeCategoricalValueInstances.SensorAcquisitionModeCategoricalValueInstance
    
  /**
   * The time limit on when a sensor update should happen, in milliseconds
   */
  val sensorUpdateTimeLimit: Option[Long]
    
  /**
   * The time limit on when a sensor heartbeat update should happen, in milliseconds
   */
  val sensorHeartbeatUpdateTimeLimit: Option[Long]
  
  /**
   * The optional manufacturer's name.
   */
  val sensorManufacturerName: Option[String]
  
  /**
   * The optional manufacturer's model.
   */
  val sensorManufacturerModel: Option[String]

  /**
   * Add in a new channel detail to the sensor detail.
   *
   * @param sensorChannelDetail
   *      the channel detail to add
   */
  def addSensorChannelDetail(sensorChannelDetail: SensorChannelDetailDescription): Unit
  
  /**
   * Get all channel details for this sensor detail.
   *
   * @return all channel details for this sensor detail
   */
  def getAllSensorChannelDetails(): List[SensorChannelDetailDescription]

  /**
   * Get a sensor channel detail of this sensor detail.
   * 
   * <p>
   * The channel must be a channel of this sensor detail to be found. Channel names are local
   * to the detail they are contained in.
   * 
   * @param id
   *     the ID of the channel detail
   *
   * @return the channel detail
   */
  def getSensorChannelDetail(id: String): Option[SensorChannelDetailDescription]

  /**
   * Does the sensor type have a channel with the given ID?
   * 
   * <p>
   * The channel must be a channel of this sensor detail to be found. Channel names are local
   * to the detail they are contained in.
   * 
   * @param channelId
   *     the ID of the channel detail
   *
   * @return [[true]] if there is a channel with the given ID
   */
  def hasSensorChannel(channelId: String): Boolean
  
  /**
   * Does the sensor return a given measurement type?
   * 
   * @return [[true]] if the sensor has a given measurement type
   */
  def hasMeasurementType(measurementTypeExternalId: String): Boolean
  
  /**
   *  Get all sensor channel descriptions for a given measurement type.
   * 
   * @return all channels with a given measurement type
   */
  def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription]
}

/**
 * Details about a sensor.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorTypeDescription(
    override val id: String, 
    override val externalId: String, 
    override val displayName: String, 
    override val displayDescription: Option[String], 
    override val sensorUpdateTimeLimit: Option[Long], 
    override val sensorHeartbeatUpdateTimeLimit: Option[Long],
    override val usageCategory: Option[String],
    override val dataSource: Option[String],
    override val acquisitionMode: SensorAcquisitionModeCategoricalValueInstances.SensorAcquisitionModeCategoricalValueInstance,
    override val sensorManufacturerName: Option[String],
    override val sensorManufacturerModel: Option[String]
    ) extends SensorTypeDescription {

  /**
   * The measurement units for this type.
   */
  private val channelDetails: ArrayBuffer[SensorChannelDetailDescription] = new ArrayBuffer

  override def addSensorChannelDetail(sensorChannelDetail: SensorChannelDetailDescription): Unit = {
    channelDetails += sensorChannelDetail
  }

  override def getAllSensorChannelDetails(): List[SensorChannelDetailDescription] = {
    channelDetails.toList
  }

  override def getSensorChannelDetail(id: String): Option[SensorChannelDetailDescription] = {
    channelDetails.find(_.channelId == id)
  }

  override def hasSensorChannel(channelId: String): Boolean = {
    channelDetails.find(_.channelId == channelId).isDefined
  }
  
  override def hasMeasurementType(measurementTypeExternalId: String): Boolean = {
    channelDetails.find(_.measurementType.externalId == measurementTypeExternalId).isDefined
  }
  
  override def getMeasurementTypeChannels(measurementTypeExternalId: String): Iterable[SensorChannelDetailDescription] = {
    channelDetails.filter(_.measurementType.externalId == measurementTypeExternalId)
  }
}