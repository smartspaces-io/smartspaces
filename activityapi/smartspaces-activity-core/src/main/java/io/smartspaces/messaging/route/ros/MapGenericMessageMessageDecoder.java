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

import io.smartspaces.messaging.MessageDecoder;

import java.util.Map;

import smartspaces_msgs.GenericMessage;

/**
 * A decoder between a map and a Generic Message.
 * 
 * @author Keith M. Hughes
 */
public class MapGenericMessageMessageDecoder extends MapGenericMessageCodec implements
    MessageDecoder<Map<String, Object>, GenericMessage> {

  @Override
  public Map<String, Object> decode(GenericMessage in) {
    Map<String, Object> msg = MAPPER.parseObject(new String(in.getMessage().toString(charset)));
    return msg;
  }
}
