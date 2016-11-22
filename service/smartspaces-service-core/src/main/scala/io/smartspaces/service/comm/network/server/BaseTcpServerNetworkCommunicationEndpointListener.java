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

/**
 * A default implementation of {@link TcpServerNetworkCommunicationEndpointListener}.
 * 
 * @param <T>
 *          the message type
 *          
 * @author Keith M. Hughes
 */
public class BaseTcpServerNetworkCommunicationEndpointListener<T>
    implements TcpServerNetworkCommunicationEndpointListener<T> {

  @Override
  public void onNewTcpConnection(TcpServerNetworkCommunicationEndpoint<T> endpoint,
      TcpServerClientConnection<T> connection) {
    // Default is do nothing.
  }

  @Override
  public void onCloseTcpConnection(TcpServerNetworkCommunicationEndpoint<T> endpoint,
      TcpServerClientConnection<T> connection) {
    // Default is do nothing.
  }

  @Override
  public void onTcpRequest(TcpServerNetworkCommunicationEndpoint<T> endpoint,
      TcpServerRequest<T> request) {
    // Default is do nothing.
  }

}
