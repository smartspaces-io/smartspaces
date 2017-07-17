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

/**
 * A sensor processor that will update sensed entity models.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelProcessor(private val completeSensedEntityModel: CompleteSensedEntityModel,
  private val managedScope: ManagedScope, private val log: ExtendedLog)
    extends SensedEntityModelProcessor with SensedEntitySensorListener {

  /**
   * The map of sensor types to sensor processors.
   */
  private val sensorValuesProcessors: Map[String, SensorValueProcessor] = new HashMap

  /**
   * The context for sensor value processors.
   */
  val processorContext = new SensorValueProcessorContext(completeSensedEntityModel, managedScope, log)

  override def addSensorValueProcessor(processor: SensorValueProcessor): SensedEntityModelProcessor = {
    log.info(s"Adding sensor processor for ${processor.sensorValueType}")

    val previous = sensorValuesProcessors.put(processor.sensorValueType, processor)
    if (previous.isDefined) {
      log.warn("A sensor processor for ${processor.sensorValueType} has just been replaced")
    }

    this
  }

  override def handleSensorData(handler: SensedEntitySensorHandler, timestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, data: DynamicObject): Unit = {

    val messageType = data.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT)

    log.info(s"Updating model with message type ${messageType} from sensor ${sensor} for entity ${sensedEntity}")

    messageType match {
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT =>
        handleMeasurement(handler, timestamp, sensor, sensedEntity, data)
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_HEARTBEAT =>
        handleHeartbeat(handler, timestamp, sensor, sensedEntity, data)
    }
  }

  private def handleMeasurement(handler: SensedEntitySensorHandler, timestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, data: DynamicObject): Unit = {

    // Go into the data fields.
    data.down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    // If the message contained a timestamp, use it, otherwise use when the message came into the processor.
    val measurementTimestamp = data.getLong(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)

    val sensorDetail = sensor.sensorEntityDescription.sensorDetail
    if (sensorDetail.isDefined) {
      // Go through every property in the data set, find its type, and then create
      // appropriate values.
      data.getProperties().foreach((channelId) => {
        if (data.isObject(channelId)) {
          log.info(s"Processing channel data ${channelId}")

          val sensedMeasurementType = sensorDetail.get.getSensorChannelDetail(channelId).get.measurementType
          val sensorValueProcessor = sensorValuesProcessors.get(sensedMeasurementType.externalId)
          if (sensorValueProcessor.isDefined) {
            data.down(channelId)
            sensorValueProcessor.get.processData(measurementTimestamp, sensor, sensedEntity, processorContext,
              data);
            data.up
          } else {
            log.warn(s"Got unknown sensed type with no apparent processor ${sensedMeasurementType}")
          }
        }

      })
      data.up
    } else {
      log.warn(s"Got sensor with no sensor detail ${sensor.sensorEntityDescription}")
    }
  }

  private def handleHeartbeat(handler: SensedEntitySensorHandler, timestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, data: DynamicObject): Unit = {

    sensor.updateHeartbeat(timestamp)
  }
}
