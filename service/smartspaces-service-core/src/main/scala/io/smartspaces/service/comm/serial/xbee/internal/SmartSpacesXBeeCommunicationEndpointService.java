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

package io.smartspaces.service.comm.serial.xbee.internal;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.serial.SerialCommunicationEndpointService;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;

/**
 * An XBee communications endpoint service using the Smart Spaces XBee library.
 *
 * @author Keith M. Hughes
 */
public class SmartSpacesXBeeCommunicationEndpointService extends BaseSupportedService implements
    XBeeCommunicationEndpointService {

  @Override
  public String getName() {
    return XBeeCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public XBeeCommunicationEndpoint newXBeeCommunicationEndpoint(String portName, ExtendedLog log) {
    SerialCommunicationEndpointService serialService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            SerialCommunicationEndpointService.SERVICE_NAME);

    return new SmartSpacesXBeeCommunicationEndpoint(serialService.newSerialEndpoint(portName),
        getSpaceEnvironment().getExecutorService(), log);
  }
}
