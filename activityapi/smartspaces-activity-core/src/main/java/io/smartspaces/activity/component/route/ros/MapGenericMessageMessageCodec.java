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

package io.smartspaces.activity.component.route.ros;

import io.smartspaces.activity.component.route.MessageRouterSupportedMessageTypes;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.ros.RosPublishers;

import com.google.common.base.Charsets;
import org.jboss.netty.buffer.ChannelBuffers;
import smartspaces_msgs.GenericMessage;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A codec between a map and a Generic Message.
 * 
 * @author Keith M. Hughes
 */
public class MapGenericMessageMessageCodec
    implements MessageCodec<Map<String, Object>, GenericMessage> {

  /**
   * The JSON mapper for message translation.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The character set for the generic message encoding.
   */
  private Charset charset = Charsets.UTF_8;

  /**
   * The message factory for {@link GenericMessage} instances.
   */
  private RosPublishers<GenericMessage> messageFactory;

  /**
   * Construct a new codec.
   * 
   * @param messageFactory
   *          the message factory for the ROS message
   */
  public MapGenericMessageMessageCodec(RosPublishers<GenericMessage> messageFactory) {
    this.messageFactory = messageFactory;
  }

  @Override
  public Map<String, Object> decode(GenericMessage in) {
    Map<String, Object> msg = MAPPER.parseObject(new String(in.getMessage().toString(charset)));
    return msg;
  }

  @Override
  public GenericMessage encode(Map<String, Object> out) {
    GenericMessage message = messageFactory.newMessage();
    message.setType(MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE);
    message.setMessage(ChannelBuffers.wrappedBuffer(MAPPER.toString(out).getBytes(charset)));

    return message;

  }
}
