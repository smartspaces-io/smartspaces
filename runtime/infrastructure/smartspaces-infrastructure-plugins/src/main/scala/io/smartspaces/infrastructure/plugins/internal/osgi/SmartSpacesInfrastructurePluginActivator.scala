/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.infrastructure.plugins.internal.osgi

import io.smartspaces.infrastructure.plugins.InfrastructurePluginManager
import io.smartspaces.infrastructure.plugins.StandardInfrastructurePluginManager
import io.smartspaces.service.comm.network.zeroconf.StandardZeroconfService
import io.smartspaces.system.osgi.SmartSpacesOsgiBundleActivator

/**
 * The OSGi activator for the infrastructure plugins.
 * 
 * @author Keith M. Hughes
 */
class SmartSpacesInfrastructurePluginActivator extends SmartSpacesOsgiBundleActivator {
  override def allRequiredServicesAvailable(): Unit = {
    val pluginsManager = new StandardInfrastructurePluginManager(getSmartSpacesEnvironment)
    addManagedResource(pluginsManager)
    registerOsgiFrameworkService(classOf[InfrastructurePluginManager].getName, pluginsManager)
  }
}