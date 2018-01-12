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

package io.smartspaces.activity.behavior.general

import io.smartspaces.util.data.mapper.JsonDataMapper
import io.smartspaces.util.data.mapper.StandardJsonDataMapper

import java.util.Map

/**
 * The standard implementation of JSON support for an activity.
 */
trait StandardActivityJson extends JsonActivityBehavior {

  /**
   * The JSON mapper.
   */
  private val MAPPER: JsonDataMapper = StandardJsonDataMapper.INSTANCE

  override def jsonStringify(map: Map[String, Object]): String = {
    return MAPPER.toString(map)
  }

  override def jsonParse(data: String): Map[String, Object] = {
    return MAPPER.parseObject(data)
  }
}
