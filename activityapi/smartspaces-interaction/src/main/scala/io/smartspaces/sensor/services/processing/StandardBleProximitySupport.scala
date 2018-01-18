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

import io.smartspaces.event.trigger.SimpleHysteresisThresholdValueTriggerWithData
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.services.processing.value.SensorValueProcessorContext
import io.smartspaces.sensor.services.processing.value.TriggerTimes
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator

/**
 * Support for BLE proximity markers.
 * 
 * @author Keith M. Hughes
 */
class StandardBleProximitySupport {

  /**
   * The configuration field that gives the map between sensors and the
   * configuration values for that sensor.
   * 
   * <p>
   * The key for this map will be the sensor ID.
   */
  val CONFIGURATION_FIELD_SENSORS = "sensors"

  /**
   * The configuration field for a given sensor that gives the RSSI value that
   * should trigger when the markable entity enters the space.
   */
  val CONFIGURATION_FIELD_RSSI_TRIGGER = "rssi.trigger"

  /**
   * The configuration field for a given sensor that gives the RSSI value offset
   * from the trigger that should trigger when the markable entity exits the
   * space.
   */
  val CONFIGURATION_FIELD_RSSI_SPREAD = "rssi.spread"

  /**
   * Configure the trigger.
   * 
   * @param userTrigger
   *          the trigger to be configured
   * @param configData
   *          the configuration data
   * @param sensor
   *          the sensor that detected the BLE
   * @param processorContext
   *          the processor context
   */
  def configureTrigger(userTrigger: SimpleHysteresisThresholdValueTriggerWithData[TriggerTimes] ,
      configData: Map[String, Object] ,  sensor: SensorEntityModel,
       processorContext: SensorValueProcessorContext): Unit = {
    val config: DynamicObject = new StandardDynamicObjectNavigator(configData)

    config.down(CONFIGURATION_FIELD_SENSORS)

    val sensorId = sensor.sensorEntityDescription.id
    if (config.downChecked(sensorId)) {
      userTrigger.setThresholdsWithOffset(config.getDouble(CONFIGURATION_FIELD_RSSI_TRIGGER),
          config.getDouble(CONFIGURATION_FIELD_RSSI_SPREAD));
    } else {
      processorContext.log.formatWarn("No BLE configuration data for sensor with ID %s",
          sensorId);
    }
  }

}
