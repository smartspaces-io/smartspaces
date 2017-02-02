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
 * A mapper between entity IDs.
 *
 * <p>
 * The mapping is one way, it is not presumed that the IDs mapped to are unique
 * to a given key.
 *
 * <p>
 * Implementations are thread safe.
 *
 * @author Keith M. Hughes
 */
trait EntityMapper {

  /**
   * Put in a mapping from one entity to another.
   *
   * @param entityIdFrom
   *          the ID of the entity from
   * @param entityIdTo
   *          the ID of the entity to
   */
  def put(entityIdFrom: String, entityIdTo: String): Unit

  /**
   * Get the mapping of one entity.
   *
   * @param entityIdFrom
   *          the ID of the entity from
   *
   * @return on option for the ID of the entity to
   */
  def get(entityIdFrom: String): Option[String]
}