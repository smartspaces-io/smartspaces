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
import io.smartspaces.scope.ManagedScope
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.processing.value.SensorValueProcessor
import io.smartspaces.sensor.processing.value.SensorValueProcessorContext
import io.smartspaces.util.data.dynamic.DynamicObject

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.sensor.processing.value.SensorValueProcessorRegistry

/**
 * A sensor processor that will update sensed entity models.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelProcessor(
    private val sensorValueProcessorRegistry: SensorValueProcessorRegistry, 
    private val completeSensedEntityModel: CompleteSensedEntityModel,
    private val managedScope: ManagedScope, 
    private val log: ExtendedLog)
    extends SensedEntityModelProcessor with SensedEntitySensorMessageHandler {

  /**
   * The context for sensor value processors.
   */
  val processorContext = new SensorValueProcessorContext(completeSensedEntityModel, managedScope, log)

  override def handleNewSensorMessage(handler: SensedEntitySensorHandler, messageReceivedTimestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, message: DynamicObject): Unit = {

    val messageType = message.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT)

    log.info(s"Updating model with message type ${messageType} from sensor ${sensor.sensorEntityDescription.externalId} for entity ${sensedEntity.sensedEntityDescription.externalId}")

    messageType match {
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT =>
        handleMeasurement(handler, messageReceivedTimestamp, sensor, sensedEntity, message)
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_HEARTBEAT =>
        handleHeartbeat(handler, messageReceivedTimestamp, sensor, message)
    }
  }

  /**
   * Handle sensor data that has come in.
   *
   * @param handler
   *          the handler the sensor data came in on
   * @param messageReceivedTimestamp
   *          the time the sensor event came in
   * @param sensor
   *          the sensor the data came in on
   * @param sensedEntity
   *          the entity the sensor gives data for
   * @param message
   *          the sensor message
   */
  private def handleMeasurement(
      handler: SensedEntitySensorHandler, 
      messageReceivedTimestamp: Long,
      sensor: SensorEntityModel, 
      sensedEntity: SensedEntityModel, 
      message: DynamicObject): Unit = {

    // Go into the data fields.
    message.down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    // If the message contained a timestamp, use it, otherwise use when the message came into the processor.
    var measurementTimestamp = message.getLong(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, messageReceivedTimestamp)

    val sensorDetail = sensor.sensorEntityDescription.sensorDetail
    if (sensorDetail.isDefined) {
      // Go through every property in the data set, find its type, and then create
      // appropriate values.
      message.getProperties().foreach((channelId) => {
        if (message.isObject(channelId)) {
          val sensorChannelDetail = sensorDetail.get.getSensorChannelDetail(channelId)
          if (sensorChannelDetail.isDefined) {
            val sensedMeasurementType = sensorChannelDetail.get.measurementType
            val sensorValueProcessor = sensorValueProcessorRegistry.getSensorValueProcessor(sensedMeasurementType.externalId)
            if (sensorValueProcessor.isDefined) {
              log.info(s"Using sensor processor ${sensorValueProcessor.get}")
              message.down(channelId)
              
              // Pick up the measurement timestamp from the channel data if it is there,
              // otherwise use the last determined timestamp
              measurementTimestamp = message.getLong(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, measurementTimestamp)

              sensorValueProcessor.get.processData(
                  measurementTimestamp, messageReceivedTimestamp, 
                  sensor, sensedEntity, processorContext,
                  channelId, message);
              message.up
            } else {
              log.warn(s"Got unknown sensed type with no apparent processor ${sensedMeasurementType}")
            }
          } else {
              log.warn(s"Got unknown channel ID ${channelId}")
          }
        }

      })
      message.up
    } else {
      log.warn(s"Got sensor with no sensor detail ${sensor.sensorEntityDescription}")
    }
  }

  /**
   * Handle sensor data that has come in.
   *
   * @param handler
   *          the handler the sensor data came in on
   * @param messageReceivedTimestamp
   *          the time the sensor event came in
   * @param sensor
   *          the sensor the data came in on
   * @param message
   *          the sensor message
   */
  private def handleHeartbeat(handler: SensedEntitySensorHandler, messageReceivedTimestamp: Long,
    sensor: SensorEntityModel, message: DynamicObject): Unit = {

    sensor.updateHeartbeat(messageReceivedTimestamp)
  }
}
