/*
 * Copyright (C) 2016 Keith M. Hughes.
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

package io.smartspaces.comm.network.zeroconf

/**
 * information about a service from Zeroconf.
 *
 * <p>
 * The Java identity of this object is only in its name.
 *
 * @author Keith M. Hughes
 */
class StandardZeroconfServiceInfo(val serviceType: String, val name: String, val subtype: String , val hostName:  String,
      val port: Int, val priority: Int,  val weight: Int)  extends ZeroconfServiceInfo {

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + name.hashCode()
    return result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj)
      return true
    if (obj == null)
      return false
    if (getClass() != obj.getClass())
      return false
    val other = obj.asInstanceOf[StandardZeroconfServiceInfo]
    if (!name.equals(other.name))
      return false
    return true
  }

  override def toString(): String = {
    return "ZeroconfRosMasterInfo [name=" + name  + ", subtype=" + subtype +
        ", hostName=" + hostName + ", port=" + port + ", priority=" + priority + ", weight=" +
        weight + "]"
  }
}
