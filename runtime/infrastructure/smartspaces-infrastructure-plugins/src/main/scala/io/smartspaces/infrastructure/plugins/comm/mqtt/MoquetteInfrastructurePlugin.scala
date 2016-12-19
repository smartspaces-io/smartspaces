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

package io.smartspaces.infrastructure.plugins.comm.mqtt

import io.smartspaces.configuration.Configuration
import io.smartspaces.infrastructure.plugins.InfrastructurePlugin
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.system.SmartSpacesEnvironment

import io.moquette.server.Server
import io.moquette.server.config.IConfig
import io.moquette.BrokerConstants
import io.smartspaces.comm.network.zeroconf.ZeroconfService
import io.smartspaces.comm.network.zeroconf.ZeroconfServiceInfo
import io.smartspaces.comm.network.zeroconf.StandardZeroconfServiceInfo
import io.smartspaces.util.io.FileSupportImpl

/**
 * An infrastructure plugin to start up a Moquette MQTT broker.
 *
 * @author Keith M. Hughes
 */
class MoquetteInfrastructurePlugin(val spaceEnvironment: SmartSpacesEnvironment) extends InfrastructurePlugin with IdempotentManagedResource {

  /**
   * The Moquette broker server.
   */
  private var mqttBroker: Server = null

  /**
   * The name of the plugin.
   */
  val name = "mqtt.broker"
  
  val fileSupport = FileSupportImpl.INSTANCE

  override def onStartup(): Unit = {
    val config = new SmartSpacesMoquetteConfig(spaceEnvironment.getSystemConfiguration)

    val storageFilePath = config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME)
    val storageFile = fileSupport.newFile(storageFilePath)
    spaceEnvironment.getLog.warn(storageFile.getParentFile)
    fileSupport.directoryExists(storageFile.getParentFile)

    mqttBroker = new Server()

    mqttBroker.startServer(config)

    val host = config.getProperty(BrokerConstants.HOST_PROPERTY_NAME)
    val port = config.getProperty(BrokerConstants.PORT_PROPERTY_NAME)

    val zeroconfService: ZeroconfService = spaceEnvironment.getServiceRegistry.getRequiredService(ZeroconfService.SERVICE_NAME)
    zeroconfService.registerService(new StandardZeroconfServiceInfo("_mqtt._tcp.local", "mqtt", null, host, Integer.parseInt(port), 0, 0))
  }

  override def onShutdown(): Unit = {
    mqttBroker.stopServer()
  }
}

/**
 * A Moquette configuration interface to the SmartSpaces configuration system.
 *
 * @author Keith M. Hughes
 */
class SmartSpacesMoquetteConfig(val smartSpacesConfig: Configuration) extends IConfig {

  /**
   * A property prefix to add to all Moquette configuration names so that they scope properly
   * to Moquette.
   */
  val MOQUETTE_PROPERTY_PREFIX = "mqtt.moquette."

  override def setProperty(name: String, value: String): Unit = {
    smartSpacesConfig.setProperty(MOQUETTE_PROPERTY_PREFIX + name, value)
  }

  override def getProperty(name: String): String = {
    smartSpacesConfig.getPropertyString(MOQUETTE_PROPERTY_PREFIX + name)
  }

  override def getProperty(name: String, defaultValue: String): String = {
    smartSpacesConfig.getPropertyString(MOQUETTE_PROPERTY_PREFIX + name, defaultValue)
  }
}