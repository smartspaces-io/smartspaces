/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.service.comm.serial.internal.rxtx;

import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.serial.SerialCommunicationEndpoint;
import io.smartspaces.service.comm.serial.SerialCommunicationEndpointService;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A factory for serial communication endpoints using the RXTX library.
 *
 * @author Keith M. Hughes
 */
public class RxtxSerialCommunicationEndpointService extends BaseSupportedService implements
    SerialCommunicationEndpointService {

  @Override
  public String getName() {
    return SerialCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public List<String> getSerialPorts() {
    List<String> ports = new ArrayList<>();

    @SuppressWarnings("unchecked")
    Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

    CommPortIdentifier portId = null; // will be set if port found

    while (portIdentifiers.hasMoreElements()) {
      CommPortIdentifier pid = portIdentifiers.nextElement();
      if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        ports.add(pid.getName());
      }
    }

    return ports;
  }

  @Override
  public SerialCommunicationEndpoint newSerialEndpoint(String portName) {
    return new RxtxSerialCommunicationEndpoint(portName, getSpaceEnvironment().getLog(),
        getSpaceEnvironment());
  }
}
