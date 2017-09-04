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

import io.smartspaces.messaging.route.RouteMessageSender
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.messaging.messages.StandardSensorData

/**
 * A route message publisher that can send marker messages.
 *
 * @author Keith M. Hughes
 */
class RouteMarkerSensorMessageSender(sensorId: String, messageSender: RouteMessageSender) extends MarkerSensorMessageSender {

  override def sendSimpleMarkerMessage(markerId: String): Unit = {
    val message = StandardSensorMessageBuilder.newMeasurementMessage(sensorId)
    
    message.addChannelData(StandardSensorData.SENSOR_CHANNEL_NAME_MARKER, StandardSensorData.SENSOR_TYPE_MARKER_SIMPLE, markerId)
    
    messageSender.sendMessage(message.toMap())
  }
  
  override def sendHeartbeatMessage(): Unit = {
    val message = StandardSensorMessageBuilder.newHeartbeatMessage(sensorId)
    
    messageSender.sendMessage(message.toMap())
  }
}