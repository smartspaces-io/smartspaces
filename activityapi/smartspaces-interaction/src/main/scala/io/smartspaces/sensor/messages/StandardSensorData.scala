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

package io.smartspaces.sensor.messages

/**
 * A collection of constants helpful for sensors.
 * 
 * @author Keith M. Hughes
 */
object StandardSensorData {

  /**
   * The type of a temperature sensor value.
   */
  val MEASUREMENT_TYPE_TEMPERATURE = "/sensor/measurement/temperature"

  /**
   * The type of a humidity sensor value.
   */
  val MEASUREMENT_TYPE_HUMIDITY = "/sensor/measurement/humidity"

  /**
   * The type of a motion sensor value.
   */
  val MEASUREMENT_TYPE_MOTION = "/sensor/measurement/motion"

  /**
   * The type of a vibration sensor value.
   */
  val MEASUREMENT_TYPE_VIBRATION = "/sensor/measurement/vibration"

  /**
   * The type of a contact sensor value.
   */
  val MEASUREMENT_TYPE_CONTACT = "/sensor/measurement/contact"

  /**
   * The type of a contact sensor.
   */
  val SENSOR_TYPE_CONTACT = "/sensor/type/contact"

  /**
   * The type of a motion sensor.
   */
  val SENSOR_TYPE_MOTION = "/sensor/type/motion"

  /**
   * The type of a vibration sensor.
   */
  val SENSOR_TYPE_VIBRATION = "/sensor/type/vibration"

  /**
   * The type of a BLE Proximity sensor value.
   */
  val SENSOR_TYPE_PROXIMITY_BLE = "proximity.ble"

  /**
   * The type of a simple marker sensor value.
   */
  val SENSOR_TYPE_MARKER_SIMPLE = "/sensor/type/marker"
  
  /**
   * A standard name for a marker channel (though not all marker channel names.
   */
  val SENSOR_CHANNEL_NAME_MARKER = "marker"
}
