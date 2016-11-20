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

package io.smartspaces.communications.network

import io.smartspaces.SmartSpacesException
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.BaseSupportedService

import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.enumerationAsScalaIterator

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

/**
 * The standard implementation of the mDNS service.
 * 
 * @author Keith M. Hughes
 */
class StandardMdnsService extends BaseSupportedService with MdnsService with IdempotentManagedResource {

  /**
   * The mDNS object that handles the network services.
   */
  var jmdns: JmDNS = null

  /**
   * The listener for service events.
   */
  val serviceListener = new ServiceListener() {
    override def serviceAdded(event: ServiceEvent): Unit = {
      handleServiceAdded(event)
    }

    override def serviceRemoved(event: ServiceEvent): Unit = {}

    override def serviceResolved(event: ServiceEvent): Unit = {}
  }

  /**
   * The simple callbacks for service lookups.
   */
  val simpleServiceCallbacks = new ConcurrentHashMap[String, (String, Int) => Unit]()

  override def onStartup(): Unit = {
    jmdns = JmDNS.create(InetAddress.getByName(getIpAddress()))
  }

  override def onShutdown(): Unit = {
    try {
      jmdns.close()
    } catch {
      case e: IOException => getSpaceEnvironment.getLog.error("Could not close mDNS service", e)
    }
  }

  override def getName(): String = {
    MdnsService.SERVICE_NAME
  }

  override def addSimpleDiscovery(serviceName: String, callback: (String, Int) => Unit): Unit = {
    println("Adding " + serviceName)
    simpleServiceCallbacks.put(serviceName, callback)
    
    jmdns.addServiceListener(serviceName, serviceListener)
  }

  private def handleServiceAdded(event: ServiceEvent): Unit = {
    println(event.getType)
    val simpleServiceCallback = simpleServiceCallbacks.get(event.getType)
    println( simpleServiceCallback)
    if (simpleServiceCallback != null) {
      println("In callback")
      try {
      val services = jmdns.list(event.getType)
      val hostname = services(0).getHostAddresses()(0)
      val port = services(0).getPort
      
      simpleServiceCallback(hostname, port)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def getIpAddress(): String = {
    val interfacesToIgnore = Set("docker0", "wlan0")
    try {
      val foo =
        NetworkInterface.getNetworkInterfaces.foreach { (intf) =>
          System.out.println("Checking " + intf.getName())
          if (!intf.isLoopback() && !interfacesToIgnore.contains(intf.getName())) {
            intf.getInetAddresses.foreach { (address) =>
              if (address.isInstanceOf[Inet4Address]) {
                return address.getHostAddress()
              }
            }
          }
        }
    } catch {
      case e: SocketException => System.out.println(" (error retrieving network interface list)")
    }

    throw new SmartSpacesException("Cannot obtain an IP address for the mDNS service")
  }
}