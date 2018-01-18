/*
 * Copyright (C) 2017 Keith M. Hughes
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
 * The type of a physical space.
 *
 * This would be, for example, a living room or bathroom.
 *
 * @author Keith M. Hughes
 */
trait PhysicalSpaceTypeDescription extends DisplayableDescription {

  /**
   * The ID of the physical space type.
   */
  val id: String

  /**
   * The external ID of the physical space type.
   */
  val externalId: String
}

/**
 * Simple implementation of a physical space type.
 * 
 * @author Keith M. Hughes
 */
case class SimplePhysicalSpaceTypeDescription(
  override val id: String,
  override val externalId: String,
  override val displayName: String,
  override val displayDescription: Option[String]) extends PhysicalSpaceTypeDescription

