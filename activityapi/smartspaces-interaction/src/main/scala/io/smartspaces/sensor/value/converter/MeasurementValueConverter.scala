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

package io.smartspaces.sensor.value.converter

import io.smartspaces.sensor.domain.MeasurementUnitDescription

/**
 * A converter from one measurement unit to another.
 * 
 * @param [F]
 * 		the type converting from
 * @param [T]
 * 		the type converting to
 *
 * @author Keith M. Hughes
 */
trait MeasurementValueConverter[F, T] {

  /**
   * The measurement unit being converted from.
   */
  val from: MeasurementUnitDescription

  /**
   * The measurement unit being converted to.
   */
  val to: MeasurementUnitDescription

  /**
   * Convert a value.
   *
   * @param value
   * 		the value to convert from
   *
   * @return the value after conversion
   */
  def convert(value: F): T
}