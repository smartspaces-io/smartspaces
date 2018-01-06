/**
 * 
 */
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
 * An entity description of a physical space.
 * 
 * @author Keith M. Hughes
 */
trait PhysicalSpaceSensedEntityDescription extends SensedEntityDescription {
  
  /**
   * The type of the physical space, if known.
   */
  val physicalSpaceType: Option[String]
  
  /**
   * External IDs for all spaces this space is contained in.
   * 
   * The containing spaces will usually be the spaces that directly contain this one.
   */
  val containedIn: Set[String]
  
  /**
   * External IDs for all spaces this space is directly connected to.
   * 
   * The containing spaces will usually be the spaces that directly contain this one.
   */
  val directlyConnectedTo: Set[String]
}

/**
 * The standard physical space entity description.
 * 
 * @author Keith M. Hughes
 */
class SimplePhysicalSpaceSensedEntityDescription(
    id: String, 
    externalId: String, 
    displayName: String, 
    description: String, 
    override val physicalSpaceType: Option[String],
    override val containedIn: Set[String],
    override val directlyConnectedTo: Set[String]
    )
    extends SimpleEntityDescription(id, externalId, displayName, description)
    with PhysicalSpaceSensedEntityDescription {
}
