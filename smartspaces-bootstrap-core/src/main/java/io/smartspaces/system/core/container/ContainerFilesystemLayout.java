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

package io.smartspaces.system.core.container;

/**
 * The filesystem layout for an Smart Spaces container.
 *
 * @author Keith M. Hughes
 */
public class ContainerFilesystemLayout {

  /**
   * The folder where Smart Spaces run files are kept, e.g. pid, etc.
   */
  public static final String FOLDER_SMARTSPACES_RUN = "run";

  /**
   * The folder where Smart Spaces system files as needed for bootstrap
   * are kept.
   */
  public static final String FOLDER_SMARTSPACES_SYSTEM = "lib/system/jvm";

  /**
   * Subdirectory which will contain the bootstrap bundles.
   */
  public static final String FOLDER_SYSTEM_BOOTSTRAP = "bootstrap";

  /**
   * Folder default for the container configs.
   */
  public static final String FOLDER_DEFAULT_CONFIG = "config";

  /**
   * Subdirectory which will contain the smartspaces configs, relative to
   * the root config folder.
   */
  public static final String FOLDER_CONFIG_smartspaces = "smartspaces";

  /**
   * Subdirectory which will contain the local environment, relative to the root
   * config folder.
   */
  public static final String FOLDER_CONFIG_ENVIRONMENT = "environment";

  /**
   * Subdirectory which will contain additional bundles for bootstrap.
   */
  public static final String FOLDER_USER_BOOTSTRAP = "startup";
}
