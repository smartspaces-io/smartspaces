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
package io.smartspaces.sensor.output

import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.sensor.messages.SensorMessages

import java.util.Map

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
   * Add in data for a channel.
   *
   * @param channelName
   *       the name of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   */
  def addChannelData(channelName: String, channelType: String, value: String) {
    messageBuilder.newObject(channelName)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
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