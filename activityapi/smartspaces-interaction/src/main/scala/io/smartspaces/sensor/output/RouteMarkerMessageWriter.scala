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

import io.smartspaces.messaging.route.RouteMessagePublisher
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.sensor.messages.SensorMessages
import io.smartspaces.sensor.StandardSensorData

/**
 * A route message publisher that can send marker messages.
 *
 * @author Keith M. Hughes
 */
class RouteMarkerMessageWriter(sensorId: String, routeMessagePublisher: RouteMessagePublisher) extends MarkerMessageWriter {

  override def sendMarkerMessage(markerId: String): Unit = {
    val message = new StandardSensorMessageBuilder(sensorId)
    
    message.addChannelData(StandardSensorData.SENSOR_CHANNEL_NAME_MARKER, StandardSensorData.SENSOR_TYPE_MARKER_SIMPLE, markerId)
    
    routeMessagePublisher.writeOutputMessage(message.toMap())
  }
}