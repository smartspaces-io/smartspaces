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

package io.smartspaces.messaging.codec

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator
import io.smartspaces.util.data.mapper.JsonDataMapper
import io.smartspaces.util.data.mapper.StandardJsonDataMapper

class DynamicObjectStringCodec extends MessageCodec[DynamicObject, String] { 

  /**
   * The JSON mapper for message translation.
   */
  private val MAPPER: JsonDataMapper = StandardJsonDataMapper.INSTANCE;

  override def encode(out: DynamicObject): String = {
    return MAPPER.toString(out.asMap())
  }

  override def decode(in:String): DynamicObject = {
    val msg = MAPPER.parseObject(in)
    return new StandardDynamicObjectNavigator(msg)
  }
}