/** Copyright (C) 2017 Keith M. Hughes
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

/**
 * A registry of sensor value processors.
 * 
 * @author Keith M. Hughes
 */
trait SensorValueProcessorRegistry {
  
  /**
   * Add in a new value processor for sensor values.
   * 
   * @param processor
   *          the value processor to add
   * 
   * @return this model processor
   */
   def addSensorValueProcessor(processor: SensorValueProcessor): SensorValueProcessorRegistry
   
  /**
   * Get the value processor for a given measurement type.
   * 
   * @param measurementType
   *          the measurement type
   * 
   * @return the optional value processor
   */
   def getSensorValueProcessor(measurementType: String): Option[SensorValueProcessor]
}