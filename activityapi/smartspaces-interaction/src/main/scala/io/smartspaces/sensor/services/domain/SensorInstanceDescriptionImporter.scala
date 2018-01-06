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

package io.smartspaces.sensor.services.domain

/**
 * An importer for sensor instance descriptions into a registry.
 *
 * @author Keith M. Hughes
 */
trait SensorInstanceDescriptionImporter {

  /**
   * Import a description into a particular instance registry.
   *
   * @param register
   *          the sensor registry
   *
   * @return this importer
   */
  def importDescriptions(register: SensorInstanceRegistry): SensorInstanceDescriptionImporter
}
