/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.system.core.configuration;

/**
 * Core configuration properties for the system.
 *
 * @author Keith M. Hughes
 */
public interface CoreConfiguration {

  /**
   * Configuration property containing the Smart Spaces version.
   */
  String CONFIGURATION_SMARTSPACES_VERSION = "smartspaces.version";

  /**
   * Property containing the Smart Spaces root directory. This will be an
   * absolute path.
   */
  String CONFIGURATION_SMARTSPACES_BASE_INSTALL_DIR = "smartspaces.rootdir";

  /**
   * Property containing the Smart Spaces runtime location. This will be an
   * absolute path.
   */
  String CONFIGURATION_SMARTSPACES_RUNTIME_DIR = "smartspaces.runtime";

  /**
   * Property containing the Smart Spaces home directory.
   *
   * <p>
   * This directory is the directory that will contain one or more Smart Spaces
   * containers, such as a master and a controller.
   */
  String CONFIGURATION_SMARTSPACES_HOME = "smartspaces.home";

  /**
   * The operating system Smart Spaces is running on.
   */
  String CONFIGURATION_SMARTSPACES_PLATFORM_OS = "smartspaces.platform.os";

  /**
   * The value of the smartspaces configuration property value when it is Linux.
   */
  String CONFIGURATION_VALUE_PLATFORM_OS_LINUX = "linux";

  /**
   * The value of the smartspaces configuration property value when it is OSX.
   */
  String CONFIGURATION_VALUE_PLATFORM_OS_OSX = "osx";

  /**
   * The value of the smartspaces configuration property value when it is OSX.
   */
  String CONFIGURATION_VALUE_PLATFORM_OS_WINDOWS = "windows";

  /**
   * The value of the smartspaces configuration property value when it is
   * unknown.
   */
  String CONFIGURATION_VALUE_PLATFORM_OS_UNKNOWN = "unknown";

  /**
   * The platform-specific file separator character.
   */
  String CONFIGURATION_PLATFORM_FILE_SEPARATOR = "platform.file.separator";

  /**
   * The value for any boolean configurations that should be true.
   */
  String CONFIGURATION_VALUE_TRUE = "true";

  /**
   * The value for any boolean configurations that should be false.
   */
  String CONFIGURATION_VALUE_FALSE = "false";
}
