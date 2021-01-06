/*
 * Copyright (C) 2019 Keith M. Hughes
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

package io.smartspaces.sensor.domain

/**
 * The description of a marker type.
 *
 * @author Keith M. Mughes
 */
trait MarkerTypeDescription extends DisplayableDescription {

  /**
   * The ID of the marker type.
   */
  def id: String

  /**
   * The external ID of the marker type.
   */
  def externalId: String

  /**
   * The usage category of the sensor.
   */
  def usageCategory: Option[String]

  /**
   * The source of the data, e.g. SmartThings, internal, etc.
   */
  def dataSourceProviders: Iterable[String]

  /**
   * The optional manufacturer's name.
   */
  def sensorManufacturerName: Option[String]

  /**
   * The optional manufacturer's model.
   */
  def sensorManufacturerModel: Option[String]

  /**
   * The measurement type of the marker type.
   */
  def measurementType: MeasurementTypeDescription
}

/**
 * The description of a marker type.
 *
 * @author Keith M. Mughes
 */
case class SimpleMarkerTypeDescription(
  override val id: String,
  override val externalId: String,
  override val displayName: String,
  override val displayDescription: Option[String],
  override val usageCategory: Option[String],
  override val dataSourceProviders: Iterable[String],
  override val sensorManufacturerName: Option[String],
  override val sensorManufacturerModel: Option[String],
  override val measurementType: MeasurementTypeDescription
) extends MarkerTypeDescription
