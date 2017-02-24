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

//import io.moquette.server.Server
//import io.moquette.server.config.IConfig
//import io.moquette.BrokerConstants
import io.smartspaces.service.comm.network.zeroconf.ZeroconfService
import io.smartspaces.service.comm.network.zeroconf.ZeroconfServiceInfo
import io.smartspaces.service.comm.network.zeroconf.StandardZeroconfServiceInfo
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.service.ServiceNotification
//import io.moquette.server.config.IResourceLoader
//import io.moquette.server.config.ClasspathResourceLoader

/**
 * An infrastructure plugin to start up a Moquette MQTT broker.
 *
 * @author Keith M. Hughes
 */
object MoquetteInfrastructurePlugin {

  /**
   * Configuration parameter name for whether the MQTT plugin should be enabled.
   */
  val CONFIGURATION_NAME_INFRASTRUCTURE_MQTT_ENABLE = "smartspaces.infrastructure.mqtt.enable";

  /**
   * Configuration parameter default value for whether the MQTT plugin should be enabled.
   */
  val CONFIGURATION_VALUE_DEFAULT_INFRASTRUCTURE_MQTT_ENABLE = false
}

/**
 * An infrastructure plugin to start up a Moquette MQTT broker.
 *
 * @author Keith M. Hughes
 */
class MoquetteInfrastructurePlugin(val spaceEnvironment: SmartSpacesEnvironment) extends InfrastructurePlugin with IdempotentManagedResource {

  /**
   * The Moquette broker server.
   */
  //private var mqttBroker: Server = null

  /**
   * The name of the plugin.
   */
  val name = "mqtt.broker"

  /**
   * The zeroconf service type for the MQTT broker.
   */
  val zeroconfServiceType = "_mqtt._tcp.local."

  /**
   * The zeroconf service name for the MQTT broker.
   */
  val zeroconfName = "mqtt"

  /**
   * [[@code true]] if the plugin is enabled.
   */
  var enabled = false

  /**
   * The file support to use.
   */
  val fileSupport = FileSupportImpl.INSTANCE

  override def onStartup(): Unit = {
    //val config = new SmartSpacesMoquetteConfig(spaceEnvironment.getSystemConfiguration)

    enabled = spaceEnvironment.getSystemConfiguration.getPropertyBoolean(MoquetteInfrastructurePlugin.CONFIGURATION_NAME_INFRASTRUCTURE_MQTT_ENABLE, MoquetteInfrastructurePlugin.CONFIGURATION_VALUE_DEFAULT_INFRASTRUCTURE_MQTT_ENABLE)

    if (enabled) {
//      val storageFilePath = config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME)
//      if (storageFilePath != null && !storageFilePath.trim().isEmpty()) {
//        val storageFile = fileSupport.newFile(storageFilePath)
//        fileSupport.directoryExists(storageFile.getParentFile)
//      }

      //mqttBroker = new Server()

      //mqttBroker.startServer(config)

      //val host = config.getProperty(BrokerConstants.HOST_PROPERTY_NAME)
      //val port = config.getProperty(BrokerConstants.PORT_PROPERTY_NAME)

      spaceEnvironment.getServiceRegistry.addServiceNotificationListener(ZeroconfService.SERVICE_NAME, new ServiceNotification[ZeroconfService]() {
        override def onServiceAvailable(zeroconfService: ZeroconfService): Unit = {
          //zeroconfService.registerService(new StandardZeroconfServiceInfo(zeroconfServiceType, zeroconfName, null, host, Integer.parseInt(port), 0, 0))
        }
      })
    }
  }

  override def onShutdown(): Unit = {
    if (enabled) {
      //mqttBroker.stopServer()
    }
  }
}

/**
 * A Moquette configuration interface to the SmartSpaces configuration system.
 *
 * @author Keith M. Hughes
 */
//class SmartSpacesMoquetteConfig(val smartSpacesConfig: Configuration) extends IConfig {
//
//  /**
//   * A property prefix to add to all Moquette configuration names so that they scope properly
//   * to Moquette.
//   */
//  val MOQUETTE_PROPERTY_PREFIX = "mqtt.moquette."
//
//  /**
//   * The resource loader for the config.
//   */
//  val resourceLoader = new ClasspathResourceLoader()
//
//  override def setProperty(name: String, value: String): Unit = {
//    smartSpacesConfig.setProperty(MOQUETTE_PROPERTY_PREFIX + name, value)
//  }
//
//  override def getProperty(name: String): String = {
//    smartSpacesConfig.getPropertyString(MOQUETTE_PROPERTY_PREFIX + name)
//  }
//
//  override def getProperty(name: String, defaultValue: String): String = {
//    smartSpacesConfig.getPropertyString(MOQUETTE_PROPERTY_PREFIX + name, defaultValue)
//  }
//
//  override def getResourceLoader(): IResourceLoader = {
//    resourceLoader
//  }
//}