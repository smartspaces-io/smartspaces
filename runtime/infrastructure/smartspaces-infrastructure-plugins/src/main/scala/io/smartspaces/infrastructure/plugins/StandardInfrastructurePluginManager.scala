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

package io.smartspaces.infrastructure.plugins

import io.smartspaces.infrastructure.plugins.comm.mqtt.MoquetteInfrastructurePlugin
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.system.SmartSpacesEnvironment

import scala.collection.mutable.HashMap

/**
 * The standard infrastructure plugins manager.
 *
 * @author Keith M. Hughes
 */
class StandardInfrastructurePluginManager(spaceEnvironment: SmartSpacesEnvironment) extends InfrastructurePluginManager with IdempotentManagedResource {

  /**
   * The map of plugin names to the plugins.
   */
  private val nameToPlugin = new HashMap[String, InfrastructurePlugin]

  override def onStartup(): Unit = { 
    
    // TODO(keith): Sucky place for this, determine better location
    addPlugin(new MoquetteInfrastructurePlugin(spaceEnvironment))
  }

  override def onShutdown(): Unit = {
    for (plugin <- nameToPlugin.values) {
      try {
        plugin.shutdown
      } catch {
        case e: Throwable => spaceEnvironment.getLog.error("Error shutting down infrastructure component", e)
      }
    }
  }

  override def addPlugin(plugin: InfrastructurePlugin): Unit = {
    plugin.startup()
    nameToPlugin.put(plugin.name, plugin)
  }
}