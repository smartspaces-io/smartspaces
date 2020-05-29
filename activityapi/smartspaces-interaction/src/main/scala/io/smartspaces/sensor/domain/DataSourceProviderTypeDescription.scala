/*
 * Copyright (C) 2020 Keith M. Hughes
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
 * Data source provider reference for sensor and marker types.
 *
 * @author Keith M. Hughes
 */
trait DataSourceProviderTypeReference {
  /**
   * The provider ID for the origin of the data.
   */
  def originProviderId: String

  /**
   * The provider ID for the interface of the data.
   */
  def interfaceProviderId: String

  /**
   * The acquisition mode for obtaining the data from this source.
   */
  def acquisitionMode: DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance
}


/**
 * Data source provider  reference for sensor and marker types.
 *
 * @author Keith M. Hughes
 */
case class SimpleDataSourceProviderTypeReference(
  override val originProviderId: String,
  override val interfaceProviderId: String,
  override val acquisitionMode: DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance
) extends DataSourceProviderTypeReference

/**
 * Data source provider descriptions for sensor and marker types.
 *
 * @author Keith M. Hughes
 */
trait DataSourceProviderTypeDescription extends DataSourceProviderTypeReference {

  /**
   * The external ID for this data source
   */
  def externalId: String
}

/**
 * Data sourceprovider  descriptions for sensor and marker types.
 *
 * @author Keith M. Hughes
 */
case class SimpleDataSourceProviderTypeDescription(
  override val externalId: String,
  override val originProviderId: String,
  override val interfaceProviderId: String,
  override val acquisitionMode: DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance
) extends DataSourceProviderTypeDescription
