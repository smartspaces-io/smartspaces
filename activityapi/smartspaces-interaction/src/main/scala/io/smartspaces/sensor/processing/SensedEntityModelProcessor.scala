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

/**
 * A processor for sensed entity model message updates,
 * 
 * @author Keith M. Hughes
 */
trait SensedEntityModelProcessor {

  /**
   * Add in a new value processor for sensor values.
   * 
   * @param processor
   *          the value processor to add
   * 
   * @return this model processor
   */
   def addSensorValueProcessor(processor: SensorValueProcessor): SensedEntityModelProcessor
}
