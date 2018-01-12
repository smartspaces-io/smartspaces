/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime.standalone.messaging;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;
import io.smartspaces.util.data.mapper.JsonDataMapper;
import io.smartspaces.util.data.mapper.StandardJsonDataMapper;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.google.common.base.Preconditions;

/**
 * Router that specifically uses multicast to send messages.
 *
 * @author Trevor Pering
 */
public class MulticastStandaloneRouter implements StandaloneRouter {
  /**
   * Maximum message size allowed. Not sure what happens if you try to make this
   * number larger.
   */
  static final int DEFAULT_RECEIVE_BUFFER_SIZE = 6000;

  /**
   * Configuration parameter for the receive buffer size.
   */
  static final String CONFIGURATION_NAME_MULTICAST_RECEIVE_BUFFER_SIZE =
      "standalone.multicast.receive.buffer.size";

  /**
   * Json mapper for message conversion.
   */
  private static final JsonDataMapper MAPPER = StandardJsonDataMapper.INSTANCE;

  /**
   * The multicast port to use.
   */
  private final int port = 6789;

  /**
   * The bound multicast socket.
   */
  private MulticastSocket multicastSocket;

  /**
   * Segment id to use for multicast communication.
   */
  private String segmentId;

  /**
   * Inet group for multicast communication.
   */
  private InetAddress group;

  /**
   * Receive buffer.
   */
  private byte[] receiveBuf;

  /**
   * Datagram packet.
   */
  private DatagramPacket datagramPacket;

  /**
   * Create a new multicast router from the given configuration.
   *
   * @param configuration
   *          configuration to use for this router
   */
  public MulticastStandaloneRouter(Configuration configuration) {
    int receiveBufferSize =
        configuration.getPropertyInteger(CONFIGURATION_NAME_MULTICAST_RECEIVE_BUFFER_SIZE,
            DEFAULT_RECEIVE_BUFFER_SIZE);
    receiveBuf = new byte[receiveBufferSize];
    datagramPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
  }

  @Override
  public void startup() {
    try {
      segmentId = System.getProperty("user.name");
      group = InetAddress.getByName("228.5.6.7");
      multicastSocket = new MulticastSocket(port);
      multicastSocket.joinGroup(group);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException("Could not startup MulticastRouter", e);
    }
  }

  @Override
  public void shutdown() {
    multicastSocket.close();
    multicastSocket = null;
  }

  @Override
  public boolean isRunning() {
    return multicastSocket != null && !multicastSocket.isClosed();
  }

  @Override
  public void send(MessageMap messageObject) {
    try {
      messageObject.put(StandaloneMessageRouter.SEGMENT_KEY, segmentId);
      String packetData = MAPPER.toString(messageObject);

      DatagramPacket packet =
          new DatagramPacket(packetData.getBytes(), packetData.length(), group, port);
      int length = packet.getLength();
      Preconditions.checkState(length < receiveBuf.length, "Attempting to send message size "
          + length);
      multicastSocket.send(packet);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException("While sending multicast message", e);
    }
  }

  @Override
  public MessageMap receive() {
    try {
      Preconditions.checkState(isRunning(), "Router is no longer running");

      multicastSocket.receive(datagramPacket);

      Preconditions.checkState(datagramPacket.getLength() < receiveBuf.length,
          "Received max length message %d, dropping: ", datagramPacket.getLength());

      String allMessage = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
      MessageMap messageObject = MessageMap.fromString(allMessage);
      if (!segmentId.equals(messageObject.get("segment"))) {
        return null;
      }
      return messageObject;
    } catch (Exception e) {
      throw new SimpleSmartSpacesException("While receiving multicast message", e);
    }
  }
}
