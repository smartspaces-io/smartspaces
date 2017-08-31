/*
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

package io.smartspaces.sensor.messaging.output

import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.sensor.messaging.messages.SensorMessages

import java.util.Map

/**
 * The standard sensor message builder.
 *
 * @author Keith M. Hughes
 */
object StandardSensorMessageBuilder {

  /**
   * Create a new measurement message.
   *
   * @param sensorId
   *            the sensor external ID
   *
   * @return the message builder
   */
  def newMeasurementMessage(sensorId: String): StandardSensorMessageBuilder = {
    new StandardSensorMessageBuilder(sensorId,
      SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT)
  }
}

/**
 * The standard sensor message builder.
 *
 * @author Keith M. Hughes
 */
class StandardSensorMessageBuilder(sensorId: String, messageType: String) {

  /**
   * The message builder.
   */
  val messageBuilder = new StandardDynamicObjectBuilder
  messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR, sensorId)
  messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE, messageType)

  messageBuilder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

  /**
   * Add in a timestamp for all channels in the message.
   *
   * @param timestamp
   *       the timestamp for the data
   *
   * @return this builder
   */
  def addTimestamp(timestamp: Long): StandardSensorMessageBuilder = {
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)
    
    this
  }

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any): StandardSensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    
    messageBuilder.up

    this
  }

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param addition
   *       the additonal value
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, addition: Any): StandardSensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION, addition)
    
    messageBuilder.up

    this
  }

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param timestamp
   *       timestamp for the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, timestamp: Long): StandardSensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)
    
    messageBuilder.up

    this
  }

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param addition
   *       the additonal value
   * @param timestamp
   *       timestamp for the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, addition: Any, timestamp: Long): StandardSensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION, addition)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)
    
    messageBuilder.up

    this
  }

  /**
   * Turn the message into a map.
   *
   * @return the map for the massage
   */
  def toMap(): Map[String, Object] = {
    messageBuilder.toMap()
  }
}