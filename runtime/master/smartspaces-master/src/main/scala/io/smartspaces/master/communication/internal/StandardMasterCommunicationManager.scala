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
   * The zeroconf service name for the master control.
   */
  val zeroconfName = "Master Control"

  /**
   * The space environment.
   */
  private var spaceEnvironment: SmartSpacesEnvironment = null

  override def onStartup(): Unit = {
    val port =
      spaceEnvironment.getSystemConfiguration().getPropertyInteger(
        RemoteMasterServerMessages.CONFIGURATION_NAME_MASTER_COMMUNICATION_PORT,
        RemoteMasterServerMessages.CONFIGURATION_VALUE_DEFAULT_MASTER_COMMUNICATION_PORT)

    webServer =
      new NettyWebServer(spaceEnvironment.getExecutorService, spaceEnvironment.getLog)
    webServer.setServerName(MASTER_COMMUNICATION_SERVER_NAME)
    webServer.setPort(port)

    webServer.startup()

    var host = "localhost"
    spaceEnvironment.getServiceRegistry.addServiceNotificationListener(ZeroconfService.SERVICE_NAME, new ServiceNotification[ZeroconfService]() {
      override def onServiceAvailable(zeroconfService: ZeroconfService): Unit = {
        zeroconfService.registerService(new StandardZeroconfServiceInfo(RemoteMasterServerMessages.ZEROCONF_MASTER_CONTROL_SERVER_SERVICE_TYPE, zeroconfName, null, host, port, 0, 0))
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
