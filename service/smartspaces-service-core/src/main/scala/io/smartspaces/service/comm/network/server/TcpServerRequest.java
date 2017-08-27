/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

import io.smartspaces.messaging.MessageSender;

import java.net.InetSocketAddress;

/**
 * A request which has come into a {@link TcpServerNetworkCommunicationEndpoint}
 * .
 *
 * @param <T>
 *          the type of the message
 *
 * @author Keith M. Hughes
 */
public interface TcpServerRequest<T> extends MessageSender<T> {

  /**
   * Get the address of the remote connection.
   *
   * @return the address of the remote connection
   */
  InetSocketAddress getRemoteAddress();

  /**
   * Get the request to the server.
   *
   * @return the message
   */
  T getMessage();
  
  /**
   * Get the client connection for this request.
   * 
   * @return the client connection
   */
  TcpServerClientConnection<T> getClientConnection();
}
