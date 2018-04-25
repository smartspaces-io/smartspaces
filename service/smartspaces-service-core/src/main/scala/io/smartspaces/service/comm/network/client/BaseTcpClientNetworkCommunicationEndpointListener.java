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

package io.smartspaces.service.comm.network.client;

/**
 * A default implementation of
 * {@link TcpClientNetworkCommunicationEndpointListener}.
 * 
 * @param <T>
 *          the message type
 * 
 * @author Keith M. Hughes
 */
public class BaseTcpClientNetworkCommunicationEndpointListener<T>
    implements TcpClientNetworkCommunicationEndpointListener<T> {

  @Override
  public void onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<T> endpoint) {
    // Default is do nothing.
  }

  @Override
  public void onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<T> endpoint) {
    // Default is do nothing.
  }

  @Override
  public void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<T> endpoint, T message) {
    // Default is do nothing.
  }
}
