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

package io.smartspaces.messaging.codec

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator
import io.smartspaces.util.data.mapper.JsonDataMapper
import io.smartspaces.util.data.mapper.StandardJsonDataMapper

import com.google.common.base.Charsets

import java.nio.charset.Charset

/**
 * A codec for translating between {@link DynamicObject}s and byte arrays.
 *
 * @author Keith M. Hughes
 */
object DynamicObjectByteArrayCodec {

  /**
   * The default charset for the codec.
   */
  val CHARSET_DEFAULT = Charsets.UTF_8
}

/**
 * A codec for translating between {@link DynamicObject}s and byte arrays.
 *
 * @author Keith M. Hughes
 */
class DynamicObjectByteArrayCodec(private val charset: Charset) extends MessageCodec[DynamicObject, Array[Byte]] {

  /**
   * The JSON mapper for message translation.
   */
  private val MAPPER: JsonDataMapper = StandardJsonDataMapper.INSTANCE;

  /**
   * Construct a codec that supports JSON ended in charset
   * {@link #CHARSET_DEFAULT}.
   */
  def this() = {
    this(DynamicObjectByteArrayCodec.CHARSET_DEFAULT)
  }

  override def encode(out: DynamicObject): Array[Byte] = {
    return MAPPER.toString(out.asMap()).getBytes(charset);
  }

  override def decode(in: Array[Byte]): DynamicObject = {
    val msg = MAPPER.parseObject(new String(in, charset));
    return new StandardDynamicObjectNavigator(msg)
  }
}
