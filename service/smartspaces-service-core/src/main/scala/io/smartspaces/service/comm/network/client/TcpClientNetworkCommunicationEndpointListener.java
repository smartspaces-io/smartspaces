/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.service.comm.network.client;

/**
 * Listener for events from a {@link TcpClientNetworkCommunicationEndpoint].
 *
 * @param <T>
 *      the type of the response
 *
 * @author Keith M. Hughes
 */
public interface TcpClientNetworkCommunicationEndpointListener<T> {
  
  /**
   * The client has made a connection to the server.
   *
   * @param endpoint
   *          endpoint hosting the connection
   */
  void onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<T> endpoint);
  
  /**
   * The connection to the server has been closed.
   *
   * @param endpoint
   *          endpoint hosting the connection
   */
  void onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<T> endpoint);  

  /**
   * A message has come in over the TCP connection.
   *
   * @param endpoint
   *          endpoint hosting the connection
   * @param message
   *          the message that has been received
   */
  void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<T> endpoint, T message);
}
