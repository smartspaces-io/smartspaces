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

package io.smartspaces.sensor.input

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.messaging.route.RouteMessageListener
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.sensor.processing.SensorProcessor
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator

import java.util.Map

/**
 * A sensor input that receives messages from a route.
 *
 * @author Keith M. Hughes
 */
class RouteMessageSensorInput(log: ExtendedLog, spaceEnvironment: SmartSpacesEnvironment) extends SensorInput with RouteMessageListener with IdempotentManagedResource {

  /**
   * The sensor processor to use.
   */
  var sensorProcessor: SensorProcessor = null
  
  override def setSensorProcessor(sensorProcessor: SensorProcessor): Unit = {
    this.sensorProcessor = sensorProcessor
  }
  
  override def onNewRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
    log.info(s"Got sensor message on route with channel ID ${channelId} ${message}")

    // TODO(keith): Consider also checking message to see if it has a timestamp.
    // If so use it, otherwise use time provider.
    val currentTime = spaceEnvironment.getTimeProvider().getCurrentTime()

    val sensorDataEvent = new StandardDynamicObjectNavigator(message)

    sensorProcessor.processSensorData(currentTime, sensorDataEvent)
  }
}