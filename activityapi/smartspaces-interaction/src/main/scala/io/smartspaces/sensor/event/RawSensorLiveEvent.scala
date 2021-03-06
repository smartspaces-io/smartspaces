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

package io.smartspaces.sensor.event

import io.smartspaces.sensor.model.SensedValue
import io.smartspaces.sensor.model.SensedEntityModel
import io.smartspaces.sensor.model.SensorChannelEntityModel

object RawSensorLiveEvent {

  /**
   * The type of the event.
   */
  val EVENT_TYPE = "sensor.value.raw"
}

/**
 * An event for a new sensor update.
 *
 * @author Keith M. Hughes
 */
class RawSensorLiveEvent(

  /**
   * The value that was sensed.
   */
  val value: SensedValue[Any],

  /**
   * The sensor channel that got the value.
   */
  val sensorChannelModel: SensorChannelEntityModel,

  /**
   * The timestamp when the measurement was made.
   */
  val timestampMeasurement: Long,

  /**
   * The timestamp when the measurement was received by the processor.
   */
  val timestampMeasurementReceived: Long)
