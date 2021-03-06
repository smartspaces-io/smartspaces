/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.example.activity.comm.network.tcp.client.hello;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.activity.impl.BaseActivity;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.base.Charsets;

/**
 * A Smart Spaces Java-based activity that demonstrates using a TCP client.
 *
 * <p>
 * A simple test message is sent and a text response will be printed.
 *
 * @author Keith M. Hughes
 */
public class HelloTcpClientActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the TCP server host.
   */
  public static final String CONFIGURATION_PROPERTY_TCP_SERVER_HOST = "space.comm.tcp.server.host";

  /**
   * The default value for the TCP server host.
   */
  public static final String CONFIGURATION_PROPERTY_DEFAULT_TCP_SERVER_PORT = "127.0.0.1";

  /**
   * The name of the config property for obtaining the TCP server port.
   */
  public static final String CONFIGURATION_PROPERTY_TCP_SERVER_PORT = "space.comm.tcp.server.port";

  /**
   * The terminators for the end of a message.
   */
  public static final byte[][] MESSAGE_TERMINATORS = new byte[][] { new byte[] { '\n' } };

  /**
   * The TCP client.
   */
  private TcpClientNetworkCommunicationEndpoint<String> tcpClient;

  @Override
  public void onActivitySetup() {
    TcpClientNetworkCommunicationEndpointService communicationEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            TcpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    String remoteTcpServerHost =
        getConfiguration().getPropertyString(CONFIGURATION_PROPERTY_TCP_SERVER_HOST,
            CONFIGURATION_PROPERTY_DEFAULT_TCP_SERVER_PORT);
    int remoteTcpServerPort =
        getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_TCP_SERVER_PORT);

    try {
      InetAddress remoteTcpServerHostAddress = InetAddress.getByName(remoteTcpServerHost);
      tcpClient =
          communicationEndpointService.newStringClient(MESSAGE_TERMINATORS, Charsets.UTF_8,
              remoteTcpServerHostAddress, remoteTcpServerPort, getLog());
      tcpClient.addListener(new TcpClientNetworkCommunicationEndpointListener<String>() {

        @Override
        public void
            onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<String> endpoint0) {
          getLog().info("Successfully connected with TCP server");
        }

        @Override
        public void onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
          getLog().info("Connection with TCP server closed");
        }

        @Override
        public void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<String> endpoint,
            String response) {
          handleTcpMessage(response);
        }
      });
      addManagedResource(tcpClient);
    } catch (UnknownHostException e) {
      throw new SimpleSmartSpacesException(String.format("Could not get host %s",
          remoteTcpServerHost), e);
    }
  }

  @Override
  public void onActivityActivate() {
    tcpClient.sendMessage("Hey server, I just activated\n");
  }

  @Override
  public void onActivityDeactivate() {
    tcpClient.sendMessage("Hey server, I just deactivated\n");
  }

  /**
   * Handle the TCP response that has come in.
   *
   * @param response
   *          the response
   */
  private void handleTcpMessage(String response) {
    getLog().info(String.format("TCP client got response from server: %s", response));
  }
}
