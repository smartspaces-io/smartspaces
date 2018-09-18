/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.configuration

import java.io.IOException
import io.smartspaces.SmartSpacesException
import java.io.FileReader
import java.util.Properties

import scala.collection.JavaConverters._
import java.io.File
import io.smartspaces.util.io.FileSupportImpl
import java.io.FileFilter

/**
 * A collection of configuration loaders.
 *
 * @author Keith M. Hughes
 */
object ConfigurationLoader {

  /**
   * The file support to use.
   */
  val fileSupport = FileSupportImpl.INSTANCE

  /**
   * Load the configuration from a filepath.
   * 
   * @param configFilePath
   *        path to the config file
   * @param configuration
   *        the configuration to load into
   */
  def loadConfigFromFiles(configFilePath: String, configuration: Configuration): Unit = {
    loadConfigFromFiles(fileSupport.newFile(configFilePath), configuration)
  }

 /**
   * Load the configuration from a file.
   * 
   * @param configFile
   *        the file, can be a directory with conf files or a conf file
   * @param configuration
   *        the configuration to load into
   */
  def loadConfigFromFiles(configFile: File, configuration: Configuration): Unit = {
    if (fileSupport.isDirectory(configFile)) {
      val confFiles = fileSupport.collectFiles(configFile, new FileFilter() {

        override def accept(pathname: File): Boolean = {
          pathname.isFile() && pathname.getName().endsWith(".conf");
        }
      }, true)

      confFiles.asScala.foreach { confFile =>
        loadPropertiesFile(confFile, configuration)
      }
    } else {
      loadPropertiesFile(configFile, configuration)
    }
  }

  /**
   * Load a configuration from a properties file.
   *
   * @param configFile
   *        the configuration file, can be a directory containing config files
   * @param configuration
   *        the configuration to place the values in
   */
  def loadPropertiesFile(configFile: File, configuration: Configuration): Unit = {
    val properties = new Properties()

    var reader: FileReader = null
    try {
      reader = new FileReader(configFile)
      properties.load(reader)

      properties.entrySet.asScala.foreach { entry =>
        configuration.setProperty(entry.getKey.toString(), entry.getValue.toString)
      }
    } catch {
      case e: Exception =>
        throw new SmartSpacesException(s"Cannot read configuration file ${configFile}", e);
    } finally {
      if (reader != null) {
        try {
          reader.close()
        } catch {
          case e: Exception =>
          // Don't care
        }
      }
    }

  }

}