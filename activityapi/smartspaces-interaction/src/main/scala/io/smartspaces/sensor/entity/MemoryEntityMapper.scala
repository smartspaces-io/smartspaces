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

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * A memory based entity mapper.
 *
 * @author Keith M. Hughes
 */
class MemoryEntityMapper extends EntityMapper {

  /**
   * The map of one type of entity to another.
   */
  private val map: Map[String, String] = new HashMap

  override def put(entityIdFrom: String, entityIdTo: String): Unit = {
    this.synchronized {
      map.put(entityIdFrom, entityIdTo);
    }
  }

  override def get(entityIdFrom: String): Option[String] = {
    this.synchronized {
      map.get(entityIdFrom)
    }
  }
}
