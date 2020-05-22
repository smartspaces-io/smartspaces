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

import io.smartspaces.data.entity.BaseCategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValueInstance
import io.smartspaces.data.entity.CategoricalValue
import io.smartspaces.data.entity.CategoricalValueInstance

/**
 * The data source acquisition mode categorical value.
 *
 * @author Keith M. Hughes
 */
final object DataSourceAcquisitionModeCategoricalValue extends BaseCategoricalValue[
  DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance](
  "sensorAcquisitionType", List(DataSourceAcquisitionModeCategoricalValueInstances.PULL, DataSourceAcquisitionModeCategoricalValueInstances.PUSH)) {
}

/**
 * All categorical value instances for how a sensor acquires data..
 *
 * @author Keith M. Hughes
 */
object DataSourceAcquisitionModeCategoricalValueInstances {

  /**
   * Base class for the sensor acquisition type categorical variable instances.
   *
   * @author Keith M. Hughes
   */
  sealed abstract class DataSourceAcquisitionModeCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
    override def value: CategoricalValue[CategoricalValueInstance] = DataSourceAcquisitionModeCategoricalValue
  }

  /**
   * The data must be pulled in.
   */
  final object PULL extends DataSourceAcquisitionModeCategoricalValueInstance(0, "PULL")

  /**
   * The data is pushed to the processor.
   */
  final object PUSH extends DataSourceAcquisitionModeCategoricalValueInstance(1, "PUSH")
}

/**
 * The description of a data source for a sensor or marker.
 *
 * @author Keith M. Hughes
 */
trait DataSourceDescription {

  /**
   * The ID of the data source. This ID will be relative to the sensor or marker type.
   */
  def sourceId: String

  /**
   * The ID of the sensor, for example the ID that the system uses if using the internal SmartThings
   * interface, vs the ID that Yonomi provides for A SmartThings sensor.
   */
  def acquisitionId: String
}

/**
 * The description of a data source for a sensor or marker.
 *
 * @author Keith M. Hughes
 */
case class SimpleDataSourceDescription(
  override val sourceId: String,
  override val acquisitionId: String
) extends DataSourceDescription
