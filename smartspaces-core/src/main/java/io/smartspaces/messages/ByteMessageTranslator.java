/*
 * Copyright (C) 2014 Keith M. Hughes.
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

package io.smartspaces.messages;

import io.smartspaces.SmartSpacesException;

/**
 * Translate byte messages into other message types.
 *
 * @param <T>
 *          the destination message type
 *
 * @author Keith M. Hughes
 */
public interface ByteMessageTranslator<T> {

  /**
   * Translate raw message data into the destination message type.
   *
   * @param message
   *          the raw message data
   *
   * @return the destination message
   *
   * @throws SmartSpacesException
   */
  T translate(byte[] message) throws SmartSpacesException;
}
