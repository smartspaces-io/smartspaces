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

package io.smartspaces.service.comm.network.server;

import io.smartspaces.messaging.MessageWriter;

/**
 * A connection for a client to a TCP server.
 *
 * @param <T>
 *          the message type
 * 
 * @author Keith M. Hughes
 */
public interface TcpServerClientConnection<T> extends MessageWriter<T> {

  /**
   * Get the connection ID for the connection.
   * 
   * @return the connection ID
   */
  String getConnectionId();

  /**
   * Is the connection open?
   * 
   * @return {@code true} if the connection is open
   */
  boolean isOpen();
  
  /**
   * Close the connection.
   */
  void close();
}
