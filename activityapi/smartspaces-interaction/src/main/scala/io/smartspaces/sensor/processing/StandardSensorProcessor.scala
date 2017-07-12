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

package io.smartspaces.sensor.processing

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.resource.managed.ManagedResources
import io.smartspaces.resource.managed.StandardManagedResources
import io.smartspaces.sensor.messaging.input.SensorInput
import io.smartspaces.util.data.dynamic.DynamicObject

import scala.collection.mutable.ListBuffer
import io.smartspaces.scope.ManagedScope

/**
 * The standard processor for sensor data.
 *
 * @author Keith M. Hughes
 */
class StandardSensorProcessor(val managedScope: ManagedScope, val log: ExtendedLog) extends SensorProcessor with IdempotentManagedResource {

  /**
   * All sensor handlers added to the processor.
   */
  private val sensorHandlers: ListBuffer[SensorHandler] = new ListBuffer

  override def addSensorInput(sensorInput: SensorInput): SensorProcessor = {
    sensorInput.setSensorProcessor(this)

    managedScope.addResource(sensorInput)

    this
  }

  override def addSensorHandler(sensorHandler: SensorHandler): SensorProcessor = {
    sensorHandler.sensorProcessor = this
    sensorHandlers += sensorHandler

    managedScope.addResource(sensorHandler)

    this
  }

  override def processSensorData(timestamp: Long, sensorDataEvent: DynamicObject): Unit = {
    sensorHandlers.foreach(handler => {
      try {
        handler.handleSensorData(timestamp, sensorDataEvent)
      } catch {
        case e: Throwable => log.error("Could not process sensor data event", e)
      }
    })
  }
}
