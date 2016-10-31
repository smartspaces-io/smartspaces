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

package io.smartspaces.service.core.internal.osgi;

import io.smartspaces.osgi.service.SmartSpacesServiceOsgiBundleActivator;
import io.smartspaces.service.comm.network.client.internal.netty.NettyTcpClientNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.server.internal.netty.NettyTcpServerNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.server.internal.netty.NettyUdpServerNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.pubsub.mqtt.paho.PahoMqttCommunicationEndpointService;
import io.smartspaces.service.comm.serial.xbee.internal.SmartSpacesXBeeCommunicationEndpointService;
import io.smartspaces.service.control.opensoundcontrol.internal.SmartSpacesOpenSoundControlClientCommunicationEndpointService;
import io.smartspaces.service.control.opensoundcontrol.internal.SmartSpacesOpenSoundControlServerCommunicationEndpointService;

/**
 * The Bundle Activator for the core smartspaces services.
 *
 * @author Keith M. Hughes
 */
public class ServicesCoreOsgiBundleActivator extends SmartSpacesServiceOsgiBundleActivator {

  @Override
  protected void allRequiredServicesAvailable() {
    registerNewSmartSpacesService(new SmartSpacesXBeeCommunicationEndpointService());

    registerNewSmartSpacesService(new NettyUdpClientNetworkCommunicationEndpointService());

    registerNewSmartSpacesService(new NettyUdpServerNetworkCommunicationEndpointService());

    registerNewSmartSpacesService(new NettyTcpClientNetworkCommunicationEndpointService());

    registerNewSmartSpacesService(new NettyTcpServerNetworkCommunicationEndpointService());

    registerNewSmartSpacesService(new SmartSpacesOpenSoundControlClientCommunicationEndpointService());

    registerNewSmartSpacesService(new SmartSpacesOpenSoundControlServerCommunicationEndpointService());

    registerNewSmartSpacesService(new PahoMqttCommunicationEndpointService());
  }
}
