/*
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

package io.smartspaces.service.comm.network.zeroconf

/**
 * Service information for zeroconf.
 *
 * @author Keith M. Hughes
 */
trait ZeroconfServiceInfo {

  /**
   * Get the service type for the service.
   *
   * @return the service type, e.g. _mqtt._tcp.local.
   */
  def serviceType(): String 

  /**
   * Get the name for the service.
   *
   * @return the name
   */
  def name(): String 

  /**
   * Get the subtype for the service.
   *
   * @return the subtype
   */
  def subtype(): String 

  /**
   * Get the most name of the service.
   *
   * @return the host name
   */
  def hostName(): String 

  /**
   * Get the port the service listens on.
   *
   * @return the port
   */
  def port(): Int 

  /**
   * Get the priority of the server.
   *
   * <p>
   * Servers with a lower valued priority should be used before those with
   * higher priority values.
   *
   * @return the priority
   */
   def priority(): Int

  /**
   * Get the weight of the server.
   *
   * <p>
   * This is used to chose the final server to be used. If there are multiple
   * servers at the same priority, one should be picked at random weighted by
   * this value.
   *
   * @return the weight
   */
  def weight(): Int
}
