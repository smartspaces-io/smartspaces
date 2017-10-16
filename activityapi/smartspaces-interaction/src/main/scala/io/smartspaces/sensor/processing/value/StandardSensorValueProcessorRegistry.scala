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

package io.smartspaces.sensor.processing.value

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.logging.ExtendedLog

/**
 * The standard implementation of the sensor value processor registry.
 * 
 * @author Keith M. Hughes
 */
class StandardSensorValueProcessorRegistry(private val log: ExtendedLog) extends SensorValueProcessorRegistry {

  /**
   * The map of value types to sensor processors.
   */
  private val sensorValuesProcessors: Map[String, SensorValueProcessor] = new HashMap
  
  override def addSensorValueProcessor(processor: SensorValueProcessor): SensorValueProcessorRegistry = {
    log.info(s"Adding sensor processor for ${processor.sensorValueType}")

    val previous = sensorValuesProcessors.put(processor.sensorValueType, processor)
    if (previous.isDefined) {
      log.warn(s"A sensor processor for ${processor.sensorValueType} has just been replaced")
    }

    this
  }
   
  override def getSensorValueProcessor(sensorValueType: String): Option[SensorValueProcessor] = {
    sensorValuesProcessors.get(sensorValueType)
  }
}