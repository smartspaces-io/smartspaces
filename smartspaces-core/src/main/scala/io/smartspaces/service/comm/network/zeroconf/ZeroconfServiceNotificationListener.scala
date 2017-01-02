/*
 * Copyright (C) 2017 Keith M. Hughes
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

/**
 * A notification of a zeroconf service status.
 * 
 * @author Keith M. Hughes
 */
trait ZeroconfServiceNotificationListener {
  
  /**
   * A service has been added.
   * 
   * @param serviceInfo
   *         information about the service added
   */
  def zeroconfServiceAdded(serviceInfo: ZeroconfServiceInfo): Unit
  
  /**
   * A service has been removed.
   * 
   * @param serviceInfo
   *         information about the service removed
   */
  def zeroconfServiceRemoved(serviceInfo: ZeroconfServiceInfo): Unit
}