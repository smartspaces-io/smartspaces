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

package io.smartspaces.master.server.remote.client.internal

import io.smartspaces.SimpleSmartSpacesException
import io.smartspaces.SmartSpacesException
import io.smartspaces.domain.basic.SpaceController
import io.smartspaces.master.server.remote.RemoteMasterServerMessages
import io.smartspaces.master.server.remote.client.RemoteMasterServerClient
import io.smartspaces.service.comm.network.zeroconf.ZeroconfService
import io.smartspaces.service.comm.network.zeroconf.ZeroconfService$
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.util.data.json.StandardJsonMapper
import io.smartspaces.util.web.HttpClientHttpContentCopier
import io.smartspaces.util.web.HttpConstants
import io.smartspaces.util.web.HttpContentCopier

import com.google.common.base.Charsets

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.HashMap
import java.util.Map
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.service.comm.network.zeroconf.BaseZeroconfNotificationListener
import io.smartspaces.service.comm.network.zeroconf.ZeroconfServiceInfo
import io.smartspaces.service.web.HttpClientRestWebClient
import io.smartspaces.service.web.RestWebClient
import io.smartspaces.resource.managed.IdempotentManagedResource

/**
 * A client for talking to a remote Master Server.
 *
 * @author Keith M. Hughes
 */
class StandardRemoteMasterServerClient(spaceEnvironment: SmartSpacesEnvironment) extends RemoteMasterServerClient with IdempotentManagedResource {

  /**
   * The number of connections for the HTTP client.
   */
  private val HTTP_CLIENT_NUMBER_CONNECTIONS = 1

  /**
   * The HTTP client for communicating with the master.
   */
  private var httpClient: RestWebClient = null

  /**
   * The network host name for the master.
   */
  private var masterHostname: String = null

  /**
   * The port that the master communication system is listening on.
   */
  private var masterCommunicationPort: Int = 0

  /**
   * The information about the controller.
   */
  private var controllerInfo: SpaceController = null

  /**
   * [[code true]] if the controller info is new and must be sent.
   */
  private var newControllerInfo = true

  override def onStartup(): Unit = {
    httpClient = new HttpClientRestWebClient(HTTP_CLIENT_NUMBER_CONNECTIONS)
    httpClient.startup()

    val config = spaceEnvironment.getSystemConfiguration

    if (config.getPropertyBoolean(SmartSpacesEnvironment.CONFIGURATION_NAME_AUTOCONFIGURE, SmartSpacesEnvironment.CONFIGURATION_VALUE_DEFAULT_AUTOCONFIGURE)) {
      val zeroconfService: ZeroconfService = spaceEnvironment.getServiceRegistry().getService(ZeroconfService.SERVICE_NAME)
      zeroconfService.addSimpleDiscovery(RemoteMasterServerMessages.ZEROCONF_MASTER_CONTROL_SERVER_SERVICE_TYPE, new BaseZeroconfNotificationListener {

        override def zeroconfServiceAdded(serviceInfo: ZeroconfServiceInfo): Unit = {
          handleMasterInfoDetected(serviceInfo)
          spaceEnvironment.getLog.info(s"Got master zeroconf at ${serviceInfo.hostName()}:${serviceInfo.port()}")
        }

        override def zeroconfServiceRemoved(serviceInfo: ZeroconfServiceInfo): Unit = {
          handleMasterInfoDisappear(serviceInfo)
          spaceEnvironment.getLog.info(s"Lost master zeroconf at ${serviceInfo.hostName()}:${serviceInfo.port()}")
        }
      })
    } else {
      masterHostname = config.getRequiredPropertyString(RemoteMasterServerMessages.CONFIGURATION_NAME_MASTER_HOST)
      masterCommunicationPort = config.getPropertyInteger(
        RemoteMasterServerMessages.CONFIGURATION_NAME_MASTER_COMMUNICATION_PORT,
        RemoteMasterServerMessages.CONFIGURATION_VALUE_DEFAULT_MASTER_COMMUNICATION_PORT)
    }
  }

  override def onShutdown(): Unit = {
    httpClient.shutdown()
    httpClient = null
  }

  override def registerSpaceController(controllerInfo: SpaceController): Unit = {
    this.controllerInfo = controllerInfo

    newControllerInfo = true

    possiblySendSpaceControllerRegistration
  }

  /**
   * The zeroconf service has been notified of the master. Update information and decide
   * whether or not to send the registration.
   */
  private def handleMasterInfoDetected(serviceInfo: ZeroconfServiceInfo): Unit = {
    // TODO(keith): Mark registration dirty if there is a change of the master host or port
    masterHostname = serviceInfo.hostName()
    masterCommunicationPort = serviceInfo.port()
    
    newControllerInfo = true

    possiblySendSpaceControllerRegistration
  }

  /**
   * The zeroconf service has been notified of the master vanishing. Update information and decide
   * whether or not to send the registration.
   */
  private def handleMasterInfoDisappear(serviceInfo: ZeroconfServiceInfo): Unit = {
    masterHostname = null
    masterCommunicationPort = -1
  }

  /**
   * Decide whether to send the controller registration or not.
   */
  private def possiblySendSpaceControllerRegistration(): Unit = {
    if (masterHostname != null && newControllerInfo) {
      sendSpaceControllerRegistration
    }
  }

  /**
   * Send the space controller registration.
   */
  private def sendSpaceControllerRegistration(): Unit = {
    try {
      val registrationData = new StandardDynamicObjectBuilder()
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_UUID,
        controllerInfo.getUuid())
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_ID,
        controllerInfo.getHostId())
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_NAME,
        controllerInfo.getHostName())
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_CONTROL_PORT,
        controllerInfo.getHostControlPort())
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_NAME,
        controllerInfo.getName())
      registrationData.setProperty(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_DESCRIPTION,
        controllerInfo.getDescription())

      val sourceUri =
        new StringBuilder().append(HttpConstants.HTTP_URL_PREFIX).append(masterHostname)
          .append(HttpConstants.URL_PORT_SEPARATOR).append(masterCommunicationPort)
          .append(RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER)
          .append(RemoteMasterServerMessages.MASTER_SPACE_CONTROLLER_METHOD_REGISTER)
          .append(HttpConstants.URL_QUERY_STRING_SEPARATOR)
          .append(RemoteMasterServerMessages.MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA)
          .append(HttpConstants.URL_QUERY_NAME_VALUE_SEPARATOR)
          .append(URLEncoder.encode(registrationData.toJson(), Charsets.UTF_8.name()))

      spaceEnvironment.getLog.info("Sending space controller registration")

      val response = httpClient.performGet(sourceUri.toString(), null)

      spaceEnvironment.getLog.info(s"Space controller registration response: ${response}")

      newControllerInfo = false
    } catch {
      case e: UnsupportedEncodingException =>
        SimpleSmartSpacesException
          .throwFormattedException("Unsupported encoding for controller registration")
      case e: SmartSpacesException =>
        throw e
    }
  }
}
