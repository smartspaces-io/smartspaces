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

package io.smartspaces.sensor.services.processing

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.mutable.ArrayBuffer

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.model.CompleteSensedEntityModel
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * The standard implementation of a sensed entity sensor handler.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntitySensorHandler(
  private val completeSensedEntityModel: CompleteSensedEntityModel,
  private val unknownSensedEntityHandler: UnknownSensedEntityHandler,
  val log: ExtendedLog) extends SensedEntitySensorHandler with IdempotentManagedResource {

  /**
   * The sensor processor the sensor input is running under.
   */
  var sensorProcessor: SensorProcessor = null

  /**
   * The listeners for physical based sensor events.
   */
  private val sensedEntitySensorMessageHandlers: ArrayBuffer[SensedEntitySensorMessageHandler] =
    new ArrayBuffer

  override def addSensedEntitySensorMessageHandler(listener: SensedEntitySensorMessageHandler): SensedEntitySensorHandler = {
    sensedEntitySensorMessageHandlers += listener

    this
  }

  override def handleSensorMessage(timestamp: Long, message: DynamicObject): Unit = {
    val messageType = message.getString(
      SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE,
      SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT)

    messageType match {
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_COMPOSITE =>
        handleCompositeSensorMessage(timestamp, message)
      case _ =>
        handleSingleSensorMessage(timestamp, message)
    }
  }

  private def handleCompositeSensorMessage(timestamp: Long, message: DynamicObject): Unit = {
    message.down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA).
      down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_MESSAGES)

    message.getArrayEntries.asScala.foreach { (messageComponent) =>
      handleSingleSensorMessage(timestamp, messageComponent.down())
    }
  }

  private def handleSingleSensorMessage(timestamp: Long, message: DynamicObject): Unit = {
    val sensorId = message.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR)

    if (sensorId == null) {
      log.warn("Got data from unknown sensor, the sensor ID is missing")
      return
    }

    val sensor = completeSensedEntityModel.getSensorEntityModelByExternalId(sensorId)
    if (sensor.isEmpty) {
      log.warn(s"Got data from unregistered sensor ${sensorId}, the data is ${message.asMap}")
      unknownSensedEntityHandler.handleUnknownSensor(sensorId)

      return
    }

    if (!sensor.get.sensorEntityDescription.active) {
      return
    }

    if (log.isDebugEnabled()) {
      log.debug(s"Got data from sensor ${sensor}: ${message.asMap}")
    }

    completeSensedEntityModel.doVoidWriteTransaction { () =>
      sensedEntitySensorMessageHandlers.foreach((handler) => {
        try {
          handler.handleNewSensorMessage(this, timestamp, sensor.get, message)
        } catch {
          case e: Throwable =>
            log.formatError(e, "Error during listener processing of physical based sensor data")
        }
      })
    }
  }
}
