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

package io.smartspaces.service.comm.network.server.internal.netty;

import java.nio.ByteOrder;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointService;

/**
 * A Netty based {@link UdpServerNetworkCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class NettyUdpServerNetworkCommunicationEndpointService extends BaseSupportedService
    implements UdpServerNetworkCommunicationEndpointService {

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public UdpServerNetworkCommunicationEndpoint newServer(int serverPort, ExtendedLog log) {
    return newServer(serverPort, ByteOrder.BIG_ENDIAN, log);
  }

  @Override
  public UdpServerNetworkCommunicationEndpoint newServer(int serverPort, ByteOrder byteOrder,
      ExtendedLog log) {
    return new NettyUdpServerNetworkCommunicationEndpoint(serverPort, byteOrder,
        getSpaceEnvironment().getExecutorService(), log);
  }
}
