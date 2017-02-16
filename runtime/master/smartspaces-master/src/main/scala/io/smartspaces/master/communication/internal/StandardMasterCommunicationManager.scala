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

package io.smartspaces.master.communication.internal

import io.smartspaces.master.communication.MasterCommunicationManager
import io.smartspaces.master.server.remote.RemoteMasterServerMessages
import io.smartspaces.service.web.server.WebServer
import io.smartspaces.service.web.server.internal.netty.NettyWebServer
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.ServiceNotification
import io.smartspaces.service.comm.network.zeroconf.ZeroconfService
import io.smartspaces.service.comm.network.zeroconf.StandardZeroconfServiceInfo
import io.smartspaces.master.communication.MasterCommunicationHandler

import java.util.List

import scala.collection.JavaConversions._

/**
 * The standard manager for master communications.
 *
 * @author Keith M. Hughes
 */
class StandardMasterCommunicationManager extends MasterCommunicationManager with IdempotentManagedResource {

  /**
   * The name given to the master communication server.
   */
  private val MASTER_COMMUNICATION_SERVER_NAME = "master"

  /**
   * The web server hosting the web socket connection.
   */
  private var webServer: WebServer = null
  
  /**
   * The master communication handlers.
   */
  private var handlers: List[MasterCommunicationHandler] = null

  /**
   * The zeroconf service name for the master control.
   */
  val zeroconfName = "Master Control"

  /**
   * The space environment.
   */
  private var spaceEnvironment: SmartSpacesEnvironment = null

  override def onStartup(): Unit = {
    val config = spaceEnvironment.getSystemConfiguration()
    val host = config.getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_ADDRESS)
    val port = config.getPropertyInteger(
        RemoteMasterServerMessages.CONFIGURATION_NAME_MASTER_COMMUNICATION_PORT,
        RemoteMasterServerMessages.CONFIGURATION_VALUE_DEFAULT_MASTER_COMMUNICATION_PORT)

    webServer =
      new NettyWebServer(spaceEnvironment.getExecutorService, spaceEnvironment.getLog)
    webServer.setServerName(MASTER_COMMUNICATION_SERVER_NAME)
    webServer.setPort(port)

    webServer.startup()
    
    if (handlers != null) {
      handlers.foreach { handler => handler.register(this) }
    }

    val masterControlServerServiceType = config.getPropertyString(
        RemoteMasterServerMessages.CONFIGURATION_NAME_ZEROCONF_MASTER_CONTROL_SERVER_SERVICE_TYPE, 
        RemoteMasterServerMessages.CONFIGURATION_VALUE_DEFAULT_ZEROCONF_MASTER_CONTROL_SERVER_SERVICE_TYPE)

    spaceEnvironment.getServiceRegistry.addServiceNotificationListener(ZeroconfService.SERVICE_NAME, new ServiceNotification[ZeroconfService]() {
      override def onServiceAvailable(zeroconfService: ZeroconfService): Unit = {
        zeroconfService.registerService(new StandardZeroconfServiceInfo(masterControlServerServiceType, zeroconfName, null, host, port, 0, 0))
      }
    })
  }

  override def onShutdown(): Unit = {
    webServer.shutdown()
    webServer = null
  }

  override def getWebServer(): WebServer = {
    return webServer
  }
  
  override def registerHander(handler: MasterCommunicationHandler): Unit = {
    handler.register(this)
  }

  /**
   * Set the handlers.
   *
   * @param handlers
   *          the handlers
   */
  def setHandlers(handlers: List[MasterCommunicationHandler]): Unit = {
    this.handlers = handlers
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  def setSpaceEnvironment(spaceEnvironment: SmartSpacesEnvironment): Unit = {
    this.spaceEnvironment = spaceEnvironment
  }
}
