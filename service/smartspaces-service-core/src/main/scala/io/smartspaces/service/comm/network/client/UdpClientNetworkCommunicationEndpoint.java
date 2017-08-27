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

import io.smartspaces.messaging.MessageSender;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.service.comm.network.WriteableUdpPacket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A communication endpoint for UDP clients.
 *
 * @author Keith M. Hughes
 */
public interface UdpClientNetworkCommunicationEndpoint extends ManagedResource {

  /**
   * Get the byte order of the client.
   *
   * @return the byte order of the client
   */
  ByteOrder getByteOrder();

  /**
   * Get a byte buffer for the message in the proper endianness for the client.
   *
   * @param message
   *          the message to be wrapped by the buffer
   *
   * @return a new byte buffer
   */
  ByteBuffer newByteBuffer(byte[] message);

  /**
   * Create a new message sender for a given remote address.
   * 
   * @param remoteAddress
   *          the remote address
   * 
   * @return the new message sender
   */
  MessageSender<byte[]> newMessageSender(InetSocketAddress remoteAddress);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param message
   *          message in the packet
   */
  void sendMessage(InetSocketAddress remoteAddress, byte[] message);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param message
   *          message in the packet
   * @param length
   *          number of bytes to send from the array
   */
  void sendMessage(InetSocketAddress remoteAddress, byte[] message, int length);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param message
   *          message in the packet
   * @param offset
   *          position of the first byte in the array to send
   * @param length
   *          number of bytes to send from the array
   */
  void sendMessage(InetSocketAddress remoteAddress, byte[] message, int offset, int length);

  /**
   * Create a new UDP packet.
   *
   * <p>
   * The packet will be of a dynamic size.
   *
   * @return UDP packet of the proper endian
   */
  WriteableUdpPacket newDynamicWriteableUdpPacket();

  /**
   * Create a new UDP packet.
   *
   * @param size
   *          size of the packet
   *
   * @return UDP packet of the proper endian
   */
  WriteableUdpPacket newWriteableUdpPacket(int size);

  /**
   * Add a listener to the endpoint.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(UdpClientNetworkCommunicationEndpointListener listener);

  /**
   * Remove a listener from the endpoint.
   *
   * <p>
   * A noop if the listener wasn't previously added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(UdpClientNetworkCommunicationEndpointListener listener);
}
