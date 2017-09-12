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
import io.smartspaces.sensor.entity.EntityMapper
import io.smartspaces.sensor.entity.MemoryEntityMapper
import io.smartspaces.sensor.entity.SensedEntityDescription
import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.util.data.dynamic.DynamicObject

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.resource.managed.IdempotentManagedResource

/**
 * The standard implementation of a sensed entity sensor handler.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntitySensorHandler(private val completeSensedEntityModel: CompleteSensedEntityModel, private val unknownSensedEntityHandler: UnknownSensedEntityHandler,
    val log: ExtendedLog) extends SensedEntitySensorHandler with IdempotentManagedResource {

  /**
   * The mapping from sensor to sensed entity.
   */
  private val sensorToSensedEntity: EntityMapper = new MemoryEntityMapper

  /**
   * The sensors being handled, keyed by their ID.
   */
  private val sensors: Map[String, SensorEntityModel] = new HashMap

  /**
   * The entities being sensed, keyed by their ID.
   */
  private val sensedEntities: Map[String, SensedEntityModel] = new HashMap

  /**
   * The sensor processor the sensor input is running under.
   */
  var sensorProcessor: SensorProcessor = null

  /**
   * The listeners for physical based sensor events.
   */
  private val sensedEntitySensorListeners: ArrayBuffer[SensedEntitySensorListener] =
    new ArrayBuffer

  override def addSensedEntitySensorListener(listener: SensedEntitySensorListener): SensedEntitySensorHandler = {
    sensedEntitySensorListeners += listener

    this
  }

  override def associateSensorWithEntity(sensor: SensorEntityDescription,
    sensedEntity: SensedEntityDescription): SensedEntitySensorHandler = {

    val sensorModel = completeSensedEntityModel.getSensorEntityModelByExternalId(sensor.externalId)
    sensors.put(sensor.externalId, sensorModel.get)

    val sensedModel = completeSensedEntityModel.getSensedEntityModelByExternalId(sensedEntity.externalId)
    sensedEntities.put(sensedEntity.externalId, sensedModel.get)
    sensorToSensedEntity.put(sensor.externalId, sensedEntity.externalId)

    this
  }

  override def handleSensorData(timestamp: Long, data: DynamicObject): Unit = {
    val messageType = data.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT)

    messageType match {
      case SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_COMPOSITE =>
        handleCompositeSensorMessage(timestamp, data)
      case _ =>
        handleSingleSensorMessage(timestamp, data)
    }
  }

  private def handleCompositeSensorMessage(timestamp: Long, data: DynamicObject): Unit = {
    data.down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA).
        down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_MESSAGES)
        
    data.getArrayEntries.asScala.foreach { (messageComponent) =>
      handleSingleSensorMessage(timestamp, messageComponent.down())
    }
  }

  private def handleSingleSensorMessage(timestamp: Long, data: DynamicObject): Unit = {
    val sensorId = data.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR)

    if (sensorId == null) {
      log.warn("Got data from unknown sensor, the sensor ID is missing")
      return
    }

    val sensor = sensors.get(sensorId)
    if (sensor.isEmpty) {
      log.formatWarn("Got data from unregistered sensor %s, the data is %s", sensorId,
        data.asMap())
      unknownSensedEntityHandler.handleUnknownSensor(sensorId)

      return
    }

    if (!sensor.get.sensorEntityDescription.active) {
      return
    }

    val sensedEntityId = sensorToSensedEntity.get(sensorId)
    if (sensedEntityId.isEmpty) {
      log.formatWarn("Got data from sensor %s with no registered sensed entity: %s", sensorId,
        data.asMap())
      return
    }

    // No need to confirm sensed entity, we would not have a sensed entity ID
    // unless there was an entity registered.
    val sensedEntity = sensedEntities.get(sensedEntityId.get)

    if (log.isDebugEnabled()) {
      log.formatDebug("Got data from sensor %s for sensed entity %s: %s", sensor, sensedEntity,
        data.asMap());
    }

    completeSensedEntityModel.doVoidWriteTransaction { () =>
      sensedEntitySensorListeners.foreach((listener) => {
        try {
          listener.handleNewSensorData(this, timestamp, sensor.get, sensedEntity.get, data);
        } catch {
          case e: Throwable =>
            log.formatError(e, "Error during listener processing of physical based sensor data");
        }
      })
    }
  }
}
