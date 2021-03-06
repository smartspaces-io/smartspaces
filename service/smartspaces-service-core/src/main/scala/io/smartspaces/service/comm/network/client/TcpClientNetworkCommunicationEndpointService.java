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

import java.net.InetAddress;
import java.nio.charset.Charset;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A service for {@link TcpClientNetworkCommunicationEndpoint} instances.
 *
 * @author Keith M. Hughes
 */
public interface TcpClientNetworkCommunicationEndpointService extends SupportedService {

  /**
   * Name for the service.
   */
  String SERVICE_NAME = "comm.network.tcp.client";

  /**
   * Create a new TCP client endpoint.
   *
   * @param delimiters
   *          the delimiters for messages
   * @param charset
   *          the character set for the strings
   * @param remoteHost
   *          the remote host to attach to
   * @param remotePort
   *          port on the remote host to connect to
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  TcpClientNetworkCommunicationEndpoint<String> newStringClient(byte[][] delimiters,
      Charset charset, String remoteHost, int remotePort, ExtendedLog log);

  /**
   * Create a new TCP client endpoint.
   *
   * @param delimiters
   *          the delimiters for messages
   * @param charset
   *          the character set for the strings
   * @param remoteHost
   *          the remote host to attach to
   * @param remotePort
   *          port on the remote host to connect to
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  TcpClientNetworkCommunicationEndpoint<String> newStringClient(byte[][] delimiters,
      Charset charset, InetAddress remoteHost, int remotePort, ExtendedLog log);
}
