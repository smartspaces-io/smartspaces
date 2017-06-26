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

package io.smartspaces.messaging.codec;

/**
 * A decoder for messages.
 * 
 * <p>
 * Instances are threadsafe and can decode multiple messages simultaneously.
 * 
 * @param <D>
 *          the type of decoded messages
 * @param <I>
 *          the type of incoming messages
 * 
 * @author Keith M. Hughes
 */
public interface MessageDecoder<D, I> {

  /**
   * Decode an incoming message.
   * 
   * @param in
   *          the incoming message
   * 
   * @return the decoded message
   */
  D decode(I in);
}