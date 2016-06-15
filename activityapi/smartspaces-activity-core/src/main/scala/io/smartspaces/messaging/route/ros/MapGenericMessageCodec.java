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

package io.smartspaces.messaging.route.ros;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

/**
 * The base class for ROS GenericMessage coders and decoders.
 * 
 * @author Keith M. Hughes
 */
public class MapGenericMessageCodec {

  /**
   * The JSON mapper for message translation.
   */
  protected static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The character set for the generic message encoding.
   */
  protected Charset charset = Charsets.UTF_8;
}
