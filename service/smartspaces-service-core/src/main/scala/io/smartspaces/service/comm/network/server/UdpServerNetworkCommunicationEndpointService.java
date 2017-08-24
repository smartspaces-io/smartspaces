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

package io.smartspaces.service.comm.network.server;

import java.nio.ByteOrder;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A communication endpoint service for UDP servers.
 *
 * @author Keith M. Hughes
 */
public interface UdpServerNetworkCommunicationEndpointService extends SupportedService {

  /**
   * Name for the service.
   */
  String SERVICE_NAME = "comm.network.udp.server";

  /**
   * Create a new UDP server endpoint.
   *
   * <p>
   * Packets will be big-endian.
   *
   * @param serverPort
   *          port the server will listen to
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  UdpServerNetworkCommunicationEndpoint newServer(int serverPort, ExtendedLog log);

  /**
   * Create a new UDP server endpoint.
   *
   * @param serverPort
   *          port the server will listen to
   * @param byteOrder
   *          byte ordering for packets
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  UdpServerNetworkCommunicationEndpoint newServer(int serverPort, ByteOrder byteOrder, ExtendedLog log);
}
