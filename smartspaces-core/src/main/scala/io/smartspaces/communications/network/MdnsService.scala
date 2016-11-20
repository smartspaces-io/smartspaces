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

import io.smartspaces.resource.managed.ManagedResource

/**
 * A service for giving mDNS functionality.
 * 
 * @author Keith M. Hughes
 */
object MdnsService {
  
  /**
   * The service name for the mDNS service
   */
  val SERVICE_NAME = "mdns"
}

/**
 * A service for giving mDNS functionality.
 * 
 * @author Keith M. Hughes
 */
trait MdnsService extends ManagedResource {

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