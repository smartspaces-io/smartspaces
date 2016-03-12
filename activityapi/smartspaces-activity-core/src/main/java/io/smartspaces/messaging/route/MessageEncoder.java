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

package io.smartspaces.messaging.route;

/**
 * A encoder for messages.
 * 
 * <p>
 * Instances are threadsafe and can decode multiple messages simultaneously.
 * 
 * @param <I>
 *          the type of internal messages
 * @param <O>
 *          the type of outgoing messages
 * 
 * @author Keith M. Hughes
 */
public interface MessageEncoder<I, O> {

  /**
   * Encode a message to go out.
   * 
   * @param out
   *          the outgoing message
   * 
   * @return the encoded message
   */
  O encode(I out);
}
