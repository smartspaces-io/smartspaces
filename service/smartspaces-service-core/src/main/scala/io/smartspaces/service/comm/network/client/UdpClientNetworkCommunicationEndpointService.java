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

import java.nio.ByteOrder;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A communication endpoint service for UDP clients.
 *
 * @author Keith M. Hughes
 */
public interface UdpClientNetworkCommunicationEndpointService extends SupportedService {

  /**
   * Name for the service.
   */
  String SERVICE_NAME = "comm.network.udp.client";

  /**
   * Create a new UDP client endpoint.
   *
   * <p>
   * Packets will be big-endian.
   *
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  UdpClientNetworkCommunicationEndpoint newClient(ExtendedLog log);

  /**
   * Create a new UDP client endpoint.
   *
   * @param byteOrder
   *          the byte order for the endpoint
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  UdpClientNetworkCommunicationEndpoint newClient(ByteOrder byteOrder, ExtendedLog log);

  /**
   * Create a new UDP broadcast client endpoint.
   *
   * @param port
   *          the port to use
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  UdpBroadcastClientNetworkCommunicationEndpoint newBroadcastClient(int port, ExtendedLog log);
}
