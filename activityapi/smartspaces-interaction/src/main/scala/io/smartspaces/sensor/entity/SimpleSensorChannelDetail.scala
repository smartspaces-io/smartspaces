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

package io.smartspaces.sensor.entity

/**
 * Details about a sensor channel.
 *
 * <p>
 * The ID of the channel is local to the sensor detail it is part of. For example, multiple
 * sensor detail items could contain a channel called temperature.
 *
 * <p>
 * The type of the channel specifies the sort of measurement the channel supplies.
 *
 * <p>
 * The units of the channel specify the units the channel is supplying the measurement in.
 *
 * @author Keith M. Hughes
 */
case class SimpleSensorChannelDetail(val sensorDetail: SensorDetail, val id: String, val displayName: String, val displayDescription: String, val measurementType: MeasurementTypeDescription, val measurementUnit: MeasurementUnitDescription) extends SensorChannelDetail {
}