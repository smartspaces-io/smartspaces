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

package io.smartspaces.sensor.services.processing.value

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

import io.smartspaces.interaction.event.trigger.SimpleHysteresisThresholdValueTriggerWithData
import io.smartspaces.interaction.event.trigger.TriggerEventTypes
import io.smartspaces.interaction.event.trigger.TriggerStates
import io.smartspaces.interaction.event.trigger.TriggerWithData
import io.smartspaces.interaction.event.trigger.TriggerWithDataListener
import io.smartspaces.sensor.messaging.messages.StandardSensorData
import io.smartspaces.sensor.model.PersonSensedEntityModel
import io.smartspaces.sensor.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.model.SensedEntityModel
import io.smartspaces.sensor.model.SensorChannelEntityModel
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.model.updater.SimplePersonPhysicalSpaceModelUpdater
import io.smartspaces.sensor.services.processing.StandardBleProximitySupport
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * The standard processor for BLE proximity data.
 *
 * @author Keith M. Hughes
 */
class StandardBleProximitySensorValueProcessor extends SensorValueProcessor {

  /**
   * The map from the BLE IDs to the trigger for that ID.
   */
  private val userTriggers: Map[String, SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes]] = new HashMap

  /**
   * The map from the triggers to the models to be updated by that trigger.
   */
  private val userTriggerToUpdaters: Map[SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes], 
    SimplePersonPhysicalSpaceModelUpdater] =
    new HashMap

  /**
   * The listener for trigger events being shared across all triggers.
   */
  private val triggerListener = new TriggerWithDataListener[TriggerTimes]() {
    override def onTrigger(trigger: TriggerWithData[TriggerTimes], 
        state: TriggerStates.TriggerState, 
        eventType: TriggerEventTypes.TriggerEventType): Unit = {
      handleTrigger(trigger, state, eventType);
    }
  };

  /**
   * support for working with BLE proximity devices.
   */
  private var bleProximitySupport: StandardBleProximitySupport = new StandardBleProximitySupport()

  val sensorValueType = StandardSensorData.SENSOR_TYPE_PROXIMITY_BLE

  override def processData(measurementTimestamp: Long, sensorMessageReceivedTimestamp: Long, 
      sensorChannel: SensorChannelEntityModel,
      processorContext: SensorValueProcessorContext,
      channelId: String, data: DynamicObject) {
    val markerId = "ble" + ":" + data.getRequiredString("id")
    val rssi = data.getDouble("rssi")

    val userTrigger = getTrigger(markerId, sensorChannel.sensorModel, sensorChannel.sensedEntityModel, processorContext)
    userTrigger.update(rssi, new TriggerTimes(measurementTimestamp, sensorMessageReceivedTimestamp))

    val markedEntity = processorContext.completeSensedEntityModel.
      sensorRegistry.getMarkableEntityByMarkerId(markerId)
    processorContext.log.info(s"Detected ID ${markerId},  RSSI= ${rssi}, ${markedEntity}\n");
  }

  /**
   * Get the trigger for a given marker ID.
   *
   * <p>
   * Creates the trigger if it didn't exist.
   *
   * @param markerId
   *          the marker ID for the trigger
   * @param sensedEntityModel
   *          the sensed entity model that is associated with the sensor
   * @param processorContext
   *          the context for processor handling
   *
   * @return the trigger for the marker
   */
  private def getTrigger(markerId: String,
    sensor: SensorEntityModel, sensedEntityModel: SensedEntityModel,
    processorContext: SensorValueProcessorContext): SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes] = {
    val userTrigger = userTriggers.get(markerId)
    if (userTrigger.isEmpty) {
      val newUserTrigger = new SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes](new TriggerTimes(0,0))

      val markerEntity = processorContext.completeSensedEntityModel.
        sensorRegistry.getMarkerEntityByMarkerId(markerId)

      val configData: scala.collection.immutable.Map[String, Object] = processorContext.completeSensedEntityModel.
        sensorRegistry.getConfigurationData(markerEntity.get.id)
      bleProximitySupport.configureTrigger(newUserTrigger, configData, sensor, processorContext);

      newUserTrigger.addListener(triggerListener)
      userTriggers.put(markerId, newUserTrigger)

      val person =
        processorContext.completeSensedEntityModel.getMarkedSensedEntityModelByMarkerId(markerId).asInstanceOf[PersonSensedEntityModel]
      val modelUpdater = new SimplePersonPhysicalSpaceModelUpdater(
        sensedEntityModel.asInstanceOf[PhysicalSpaceSensedEntityModel], person)
      userTriggerToUpdaters.put(newUserTrigger, modelUpdater)

      newUserTrigger
    } else {
      userTrigger.get
    }
  }

  /**
   * Handle a trigger change,
   *
   * @param trigger
   *          the trigger that changed
   * @param state
   *          the new state of the trigger
   * @param triggerType
   *          the type of the state change
   */
  private def handleTrigger(trigger: TriggerWithData[TriggerTimes], 
      state: TriggerStates.TriggerState, 
      eventType: TriggerEventTypes.TriggerEventType): Unit = {
    val t = trigger.asInstanceOf[SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes]]
    val modelUpdater = userTriggerToUpdaters.get(t).get
    if (eventType == TriggerEventTypes.RISING) {
      modelUpdater.enterSpace(t.getData.measurementTimestamp, t.getData.sensorMessageReceivedTimestamp);
    } else {
      modelUpdater.exitSpace(t.getData.measurementTimestamp, t.getData.sensorMessageReceivedTimestamp);
    }
  }
}

/**
 * The measurement times of the BLE measurements.
 * 
 * @author Keith M. Hughes
 */
class TriggerTimes(val measurementTimestamp: Long, val sensorMessageReceivedTimestamp: Long)

