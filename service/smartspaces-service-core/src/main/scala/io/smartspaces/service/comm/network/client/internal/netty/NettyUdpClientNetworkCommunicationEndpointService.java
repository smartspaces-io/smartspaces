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

package io.smartspaces.service.comm.network.client.internal.netty;

import java.nio.ByteOrder;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;

/**
 * Netty implementation of the
 * {@link UdpClientNetworkCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class NettyUdpClientNetworkCommunicationEndpointService extends BaseSupportedService
    implements UdpClientNetworkCommunicationEndpointService {

  @Override
  public String getName() {
    return UdpClientNetworkCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public UdpClientNetworkCommunicationEndpoint newClient(ExtendedLog log) {
    return newClient(ByteOrder.BIG_ENDIAN, log);
  }

  @Override
  public UdpClientNetworkCommunicationEndpoint newClient(ByteOrder byteOrder, ExtendedLog log) {
    return new NettyUdpClientNetworkCommunicationEndpoint(byteOrder, getSpaceEnvironment()
        .getExecutorService(), log);
  }

  @Override
  public UdpBroadcastClientNetworkCommunicationEndpoint newBroadcastClient(int port, ExtendedLog log) {
    return new NettyUdpBroadcastClientNetworkCommunicationEndpoint(port, getSpaceEnvironment()
        .getExecutorService(), log);
  }
}
