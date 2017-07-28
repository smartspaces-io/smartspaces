/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
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

package io.smartspaces.service.comm.network.zeroconf

import io.smartspaces.SmartSpacesException
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.BaseSupportedService
import io.smartspaces.system.SmartSpacesEnvironment

import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

/*
 * The standard implementation of the mDNS (Zeroconf) service.
 *
 * @author Keith M. Hughes
 */
class StandardZeroconfService extends BaseSupportedService with ZeroconfService with IdempotentManagedResource {

  /**
   * The mDNS object that handles the network services.
   */
  var jmdns: JmDNS = null

  /**
   * The listener for service events.
   */
  val serviceListener = new ServiceListener() {
    override def serviceAdded(event: ServiceEvent): Unit = {
      // Required to force serviceResolved to be called
      // again (after the first search)
      jmdns.requestServiceInfo(event.getType(), event.getName(), 1)
    }

    override def serviceRemoved(event: ServiceEvent): Unit = {
      handleServiceRemoved(event)
    }

    override def serviceResolved(event: ServiceEvent): Unit = {
      handleServiceAdded(event)
    }
  }

  /**
   * The listeners for service lookups.
   *
   * <p>
   * TODO(keith): Must become a multimap.
   */
  val simpleServiceListeners = new ConcurrentHashMap[String, ZeroconfServiceNotificationListener]()

  var ipAddress: String = null

  override def onStartup(): Unit = {
    try {
      ipAddress = getSpaceEnvironment.getSystemConfiguration.getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_ADDRESS)
      getSpaceEnvironment.getLog.info(s"Starting up zeroconf at address ${ipAddress}")
      
      jmdns = JmDNS.create(InetAddress.getByName(ipAddress))
    } catch {
      case e: Throwable => getSpaceEnvironment.getLog.error("Could not start Zeroconf service", e)
    }
  }

  override def onShutdown(): Unit = {
    try {
      jmdns.unregisterAllServices()
      jmdns.close()
    } catch {
      case e: IOException => getSpaceEnvironment.getLog.error("Could not close Zeroconf service", e)
    }
  }

  override def getName(): String = {
    ZeroconfService.SERVICE_NAME
  }

  override def registerService(serviceInfo: ZeroconfServiceInfo): Unit = {
    try {
      jmdns.registerService(toJmdnsServiceInfo(serviceInfo))

      getSpaceEnvironment.getLog.formatInfo("Registered zeroconf service type %s at port %s", serviceInfo.serviceType, serviceInfo.port.toString())
    } catch {
      case e: Exception => throw new SmartSpacesException("Unable to register new service with Zeroconf", e)
    }
  }

  override def unregisterService(serviceInfo: ZeroconfServiceInfo): Unit = {
    jmdns.unregisterService(toJmdnsServiceInfo(serviceInfo))
  }

  override def addSimpleDiscovery(serviceName: String, listener: ZeroconfServiceNotificationListener): Unit = {
    simpleServiceListeners.put(serviceName, listener)

    jmdns.addServiceListener(serviceName, serviceListener)

    getSpaceEnvironment.getLog.info(s"Added zeroconf service discovery for ${serviceName}")
  }

  private def handleServiceAdded(event: ServiceEvent): Unit = {
    getSpaceEnvironment.getLog.info(s"Got zeroconf service added event ${event.getType}")
    val simpleServiceListener = simpleServiceListeners.get(event.getType)
    if (simpleServiceListener != null) {
      try {
        val services = jmdns.list(event.getType)
        val hostname = services(0).getHostAddresses()(0)
        val port = services(0).getPort

        val info = event.getInfo
        val serviceInfo = new StandardZeroconfServiceInfo(info.getType, info.getName, info.getSubtype, info.getHostAddress,
          info.getPort, info.getPriority, info.getWeight)

        simpleServiceListener.zeroconfServiceAdded(serviceInfo)
      } catch {
        case e: Throwable => getSpaceEnvironment.getLog.error("Error while handing zeroconf service callback.", e)
      }
    }
  }

  private def handleServiceRemoved(event: ServiceEvent): Unit = {
    getSpaceEnvironment.getLog.info(s"Got zeroconf service removed event ${event.getType}")
    val simpleServiceCallback = simpleServiceListeners.get(event.getType)
    //    if (simpleServiceCallback != null) {
    //      try {
    //        val services = jmdns.list(event.getType)
    //        val hostname = services(0).getHostAddresses()(0)
    //        val port = services(0).getPort
    //
    //        simpleServiceCallback(hostname, port)
    //      } catch {
    //        case e: Throwable => getSpaceEnvironment.getLog.error("Error while handing zeroconf service callback.", e)
    //      }
    //    }
  }

  /**
   * Convert a JmDNS service info object into a SmartSpaces zeroconf service object.
   *
   * @param info
   *          the JmDNS service information
   *
   * @return the zeroconf service object
   */
  private def toSmartSpacesServiceInfo(serviceInfo: ServiceInfo): StandardZeroconfServiceInfo = {
    val hostName = serviceInfo.getHostAddresses()(0)

    return new StandardZeroconfServiceInfo(serviceInfo.getType, serviceInfo.getName(), serviceInfo.getSubtype, hostName, serviceInfo.getPort, serviceInfo.getPriority,
      serviceInfo.getWeight)
  }

  /**
   * Convert a SmartSpaces zeroconf service object into a JmDNS service info object.
   *
   * @param masterInfo
   *          the ROS Master information
   *
   * @return a properly configured JmDNS service info descriptor for the master.
   */
  private def toJmdnsServiceInfo(serviceInfo: ZeroconfServiceInfo): ServiceInfo = {
    return ServiceInfo.create(serviceInfo.serviceType, serviceInfo.name, serviceInfo.subtype,
      serviceInfo.port, serviceInfo.priority, serviceInfo.weight, "")
  }

}