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

package io.smartspaces.sensor

import io.smartspaces.service.SupportedService
import io.smartspaces.sensor.integrator.SensorIntegrator

/**
 * The sensor interaction service.
 * 
 * @author Keith M. Hughes
 */
object SensorInteractionService {
  
  /**
   * The name of the service.
   */
  val SERVICE_NAME = "sensor.interaction"
}

/**
 * The sensor interaction service.
 * 
 * @author Keith M. Hughes
 */
trait SensorInteractionService extends SupportedService {
  
  /**
   * Create a new sensor integrator.
   */
  def newSensorIntegrator(): SensorIntegrator
}