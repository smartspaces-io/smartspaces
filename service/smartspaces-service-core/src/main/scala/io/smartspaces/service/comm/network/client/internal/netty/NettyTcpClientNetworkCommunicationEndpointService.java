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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;

/**
 * A Netty based {@link TcpClientNetworkCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class NettyTcpClientNetworkCommunicationEndpointService extends BaseSupportedService
    implements TcpClientNetworkCommunicationEndpointService {

  @Override
  public String getName() {
    return TcpClientNetworkCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public TcpClientNetworkCommunicationEndpoint<String> newStringClient(byte[][] delimiters,
      Charset charset, String remoteHost, int remotePort, ExtendedLog log) {
    try {
      return newStringClient(delimiters, charset, InetAddress.getByName(remoteHost), remotePort,
          log);
    } catch (UnknownHostException e) {
      throw new SmartSpacesException("TCP client server has unknown host " + remoteHost);
    }
  }

  @Override
  public TcpClientNetworkCommunicationEndpoint<String> newStringClient(byte[][] delimiters,
      Charset charset, InetAddress remoteAddr, int serverPort, ExtendedLog log) {
    int length = delimiters.length;
    ChannelBuffer[] delimiterBuffers = new ChannelBuffer[length];
    for (int i = 0; i < length; i++) {
      delimiterBuffers[i] = ChannelBuffers.wrappedBuffer(delimiters[i]);
    }

    return new NettyStringTcpClientNetworkCommunicationEndpoint(delimiterBuffers, charset,
        remoteAddr, serverPort, getSpaceEnvironment().getExecutorService(), log);
  }
}
