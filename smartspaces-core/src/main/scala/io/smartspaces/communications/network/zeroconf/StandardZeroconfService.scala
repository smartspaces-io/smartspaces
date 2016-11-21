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

package io.smartspaces.communications.network.zeroconf

import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.enumerationAsScalaIterator

import io.smartspaces.SmartSpacesException
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.BaseSupportedService
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import javax.jmdns.ServiceInfo

/*
 * The standard implementation of the mDNS service.
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

    override def serviceRemoved(event: ServiceEvent): Unit = {}

    override def serviceResolved(event: ServiceEvent): Unit = {
      handleServiceAdded(event)
    }
  }

  /**
   * The simple callbacks for service lookups.
   */
  val simpleServiceCallbacks = new ConcurrentHashMap[String, (String, Int) => Unit]()

  var ipAddress: String = null

  override def onStartup(): Unit = {
    ipAddress = getIpAddress()
    jmdns = JmDNS.create(InetAddress.getByName(ipAddress))
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
    } catch {
      case e: Exception => throw new SmartSpacesException("Unable to register new service with Zeroconf", e)
    }
  }

  override def unregisterService(serviceInfo: ZeroconfServiceInfo): Unit = {
    jmdns.unregisterService(toJmdnsServiceInfo(serviceInfo))
  }

  override def addSimpleDiscovery(serviceName: String, callback: (String, Int) => Unit): Unit = {
    simpleServiceCallbacks.put(serviceName, callback)

    jmdns.addServiceListener(serviceName, serviceListener)
  }

  private def handleServiceAdded(event: ServiceEvent): Unit = {
    val simpleServiceCallback = simpleServiceCallbacks.get(event.getType)
    if (simpleServiceCallback != null) {
      try {
        val services = jmdns.list(event.getType)
        val hostname = services(0).getHostAddresses()(0)
        val port = services(0).getPort

        simpleServiceCallback(hostname, port)
      } catch {
        case e: Exception => getSpaceEnvironment.getLog.error("Error while handing zeroconf service callback.", e)
      }
    }
  }

  def getIpAddress(): String = {
    val interfacesToIgnore = Set("docker0", "wlan0")
    try {
      val foo =
        NetworkInterface.getNetworkInterfaces.foreach { (intf) =>
          if (!intf.isLoopback() && !interfacesToIgnore.contains(intf.getName())) {
            intf.getInetAddresses.foreach { (address) =>
              if (address.isInstanceOf[Inet4Address]) {
                return address.getHostAddress()
              }
            }
          }
        }
    } catch {
      case e: SocketException => getSpaceEnvironment.getLog.error(" (error retrieving network interface list)", e)
    }

    throw new SmartSpacesException("Cannot obtain an IP address for the zeroconf service")
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