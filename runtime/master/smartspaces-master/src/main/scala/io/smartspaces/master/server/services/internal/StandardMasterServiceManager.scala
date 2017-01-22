/*
 * Copyright (C) 2016 Keith M. Hughes
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
package io.smartspaces.master.server.services.internal

import io.smartspaces.master.server.services.MasterServiceManager
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.service.comm.network.zeroconf.StandardZeroconfService
import io.smartspaces.service.comm.network.client.internal.netty.NettyTcpClientNetworkCommunicationEndpointService

/**
 * The standard implementation of the master service manager.
 */
class StandardMasterServiceManager extends MasterServiceManager {

  /**
   * The space environment for this manager.
   */
  private var spaceEnvironment: SmartSpacesEnvironment = null
  
  /**
   * The zeroconf service to add to the master.
   */
  private val services = List(new StandardZeroconfService, new NettyTcpClientNetworkCommunicationEndpointService)

  override def startup(): Unit = {
    val serviceRegistry = spaceEnvironment.getServiceRegistry
    
    services.foreach { service => 
      serviceRegistry.startupAndRegisterService(service) 
    }
  }

  override def shutdown(): Unit = {
    val serviceRegistry = spaceEnvironment.getServiceRegistry
    
    services.foreach { service => 
      serviceRegistry.shutdownAndUnregisterService(service) 
    }
  }
  
  /**
   * Set the space environment for this manager.
   * 
   * @param spaceEnvironment
   *       the space environment
   */ 
  def setSpaceEnvironment(spaceEnvironment: SmartSpacesEnvironment): Unit = {
    this.spaceEnvironment = spaceEnvironment
  }
}