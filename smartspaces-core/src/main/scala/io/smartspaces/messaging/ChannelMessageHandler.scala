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

package io.smartspaces.messaging

/**
 * A handler for messages coming in on channeled IO.
 * 
 * @param [M] the type of the message
 *
 * @author Keith M. Hughes
 */
trait ChannelMessageHandler[M] {
  
 /**
   * A new message has come in.
   *
   * @param channelId
   *          the ID of the channel the message came in on
   * @param message
   *          the message which has come in
   */
  def onNewMessage(channelId: String , message: M): Unit
  
}