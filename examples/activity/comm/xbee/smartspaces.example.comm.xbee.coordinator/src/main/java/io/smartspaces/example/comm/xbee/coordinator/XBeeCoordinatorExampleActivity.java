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

package io.smartspaces.example.comm.xbee.coordinator;

import io.smartspaces.activity.impl.BaseActivity;
import io.smartspaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.RxIoSampleXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.TxStatusXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.XBeeAddress64;
import io.smartspaces.service.comm.serial.xbee.XBeeApiConstants;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import io.smartspaces.service.comm.serial.xbee.XBeeResponseListenerSupport;
import io.smartspaces.util.ByteUtils;

/**
 * A simple Smart Spaces Java-based activity which listens for events from an
 * XBee radio configured as a Coordinator.
 *
 * <p>
 * This example sends a couple of AP commands to the local radio, sends a
 * Transmit packet to a remote radio, and will display IO sample frames if sent
 * from the remote radio.
 *
 * @author Keith M. Hughes
 */
public class XBeeCoordinatorExampleActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the serial port.
   */
  public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT =
      "space.hardware.serial.port";

  /**
   * The name of the config property for obtaining the 64 bit address of the
   * endpoint radio.
   */
  public static final String CONFIGURATION_PROPERTY_XBEE_REMOTE_ADDRESS64 = "xbee.remote.address64";

  /**
   * The XBee endpoint.
   */
  private XBeeCommunicationEndpoint xbee;

  @Override
  public void onActivitySetup() {
    XBeeCommunicationEndpointService service =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            XBeeCommunicationEndpointService.SERVICE_NAME);

    String portName =
        getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

    xbee = service.newXBeeCommunicationEndpoint(portName, getLog());

    xbee.addListener(new XBeeResponseListenerSupport() {

      @Override
      public void onAtLocalXBeeResponse(XBeeCommunicationEndpoint endpoint,
          AtLocalResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getCommandData()));
      }

      @Override
      public void
          onRxXBeeResponse(XBeeCommunicationEndpoint endpoint, RxResponseXBeeFrame response) {
        getLog().info(response);
      }

      @Override
      public void onTxStatusXBeeResponse(XBeeCommunicationEndpoint endpoint,
          TxStatusXBeeFrame response) {
        getLog().info(response);
      }

      @Override
      public void onAtRemoteXBeeResponse(XBeeCommunicationEndpoint endpoint,
          AtRemoteResponseXBeeFrame response) {
        getLog().info(response);
        getLog().info(ByteUtils.toHexString(response.getCommandData()));
      }

      @Override
      public void onRxIoSampleXBeeResponse(XBeeCommunicationEndpoint endpoint,
          RxIoSampleXBeeFrame response) {
        getLog().info(response);
      }
    });

    // Let IS manage the connection, which means we don't have to start it or
    // shut it down.
    addManagedResource(xbee);
  }

  @Override
  public void onActivityActivate() {
    // Ask the local radio what version of the AP protocol it is using.
    //
    // Using the escape code for the frame ID to make sure escapes are
    // happening properly.
    xbee.newAtLocalRequestXBeeFrame(XBeeApiConstants.AT_COMMAND_AP, 0x7d).write(xbee);

    getLog().info("Wrote AT command");

    // Send a TX packet to the remote radio. It will have frame ID 3.
    XBeeAddress64 remoteAddress =
        xbee.newXBeeAddress64(getConfiguration().getRequiredPropertyString(
            CONFIGURATION_PROPERTY_XBEE_REMOTE_ADDRESS64));
    xbee.newTxRequestXBeeFrame(remoteAddress, 0x03, 0, 0).add16(1234).write(xbee);

    getLog().info("Wrote TX request");
  }
}
