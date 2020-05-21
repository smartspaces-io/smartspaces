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

package io.smartspaces.sensor.domain

/**
 * An entity description of something that marks something else, providing an
 * identity for it.
 *
 * @author Keith M. Hughes
 */
trait MarkerEntityDescription extends EntityDescription {

  /**
   * The source of the marker data feed. 
   */
  val markerSource: DataSourceDescription
  
  /**
   * The type of the marker, i.e. the exact hardware specification.
   */
  val markerType: String
  
  /**
   * The ID the marker will use for identification.
   */
  val markerId: String
}

/**
 * The standard marker entity description.
 *
 * @author Keith M. Hughes
 */
class SimpleMarkerEntityDescription(
  id: String,
  externalId: String,
  displayName: String,
  displayDescription: Option[String],
  override val markerSource: DataSourceDescription,
  override val markerType: String,
  override val markerId: String) extends SimpleEntityDescription(id, externalId, displayName, displayDescription)
  with MarkerEntityDescription {

  override def toString(): String = {
    "SimpleMarkerEntityDescription [id=" + id + ", description=" +
      displayDescription + ", markerId=" + markerId + "]"
  }
}
