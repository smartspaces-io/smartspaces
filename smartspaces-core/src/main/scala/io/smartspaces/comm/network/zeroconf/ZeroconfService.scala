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

package io.smartspaces.comm.network.zeroconf

import io.smartspaces.resource.managed.ManagedResource

/**
 * A service for giving mDNS functionality.
 * 
 * @author Keith M. Hughes
 */
object ZeroconfService {
  
  /**
   * The service name for the Zeroconf service
   */
  val SERVICE_NAME = "zeroconf"
}

/**
 * A service for giving Zeroconf functionality.
 * 
 * @author Keith M. Hughes
 */
trait ZeroconfService extends ManagedResource {

  /**
   * Register a new service for discovery.
   * 
   * @param serviceInfo
   *       the service to register
   */
  def registerService( serviceInfo: ZeroconfServiceInfo): Unit

  /**
   * Unregister a service for discovery.
   * 
   * @param serviceInfo
   *       the service to unregister
   */
  def unregisterService(serviceInfo: ZeroconfServiceInfo): Unit

  /**
   * Add in a simple discovery request.
   *
   * @param serviceName
   *         the name of the service to be found
   * @param callback
   *        the callback to call when the the service is discovered
   */
  def addSimpleDiscovery(serviceName: String, callback: (String, Int) => Unit): Unit
}