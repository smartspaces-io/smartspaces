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

package io.smartspaces.sensor.messaging.messages

/**
 * Useful support for sensor messaging.
 *
 * @author Keith M. Hughes
 */
object SensorMessages {

  /**
   * The field name for the sensor field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_SENSOR = "sensor"

  /**
   * The version field for a message envelope.
   */
  val SENSOR_MESSAGE_FIELD_NAME_VERSION = "version";

  /**
   * The field name for the message type field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE = "messageType"

  /**
   * The field name for the message sender field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_MESSAGE_SENDER = "messageSender"

  /**
   * The field name for the message destination field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_MESSAGE_DESTINATION = "messageDestination"

  /**
   * The field value for a measurement for the message type field.
   */
  val SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT = "measurement"

  /**
   * The field value for a heartbeat for the message type field.
   */
  val SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_HEARTBEAT = "heartbeat"

  /**
   * The field value for a composite message for the message type field.
   */
  val SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_COMPOSITE = "composite"

  /**
   * The field name for the data field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA = "data"

  /**
   * The field name for the value field in the data field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE = "value"

  /**
   * The field name for the addition field in the data field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION = "addition"

  /**
   * The field name for the type field in the data field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE = "type"

  /**
   * The field name for the timestamp field in the data field.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP = "timestamp"

  /**
   * The field name for the messages field in the data field of a composite message.
   */
  val SENSOR_MESSAGE_FIELD_NAME_DATA_MESSAGES = "messages"

  /**
   * A collection of fields in a sensor data packet that are not channels.
   */
  val SENSOR_MESSAGE_DATA_NON_CHANNEL_FIELDS = Set(SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP)
}
