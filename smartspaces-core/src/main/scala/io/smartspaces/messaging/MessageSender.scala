/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.messaging;

/**
 * A writer of messages.
 *
 * @author Keith M. Hughes
 */
trait MessageSender[F] {

  /**
   * Send a message.
   *
   * <p>
   * The message will be serialized properly for the channel.
   *
   * @param message
   *          the message to send
   */
  def sendMessage(message: F): Unit
}

/**
  * A writer of keyed messages.
  *
  * @author Keith M. Hughes
  */
trait KeyedMessageSender[K, F] {

  /**
    * Send a message.
    *
    * <p>
    * The message will be serialized properly for the channel.
    *
    * @param key
    *        the key for the message
    * @param message
    *          the message to send
    */
  def sendKeyedMessage(key: K, message: F): Unit
}
