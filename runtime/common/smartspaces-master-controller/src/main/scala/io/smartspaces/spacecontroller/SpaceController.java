/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.spacecontroller;

import io.smartspaces.activity.binary.NativeActivityRunnerFactory;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.resource.ManagedResource;

/**
 * A controller for Smart Spaces activities.
 *
 * <p>
 * This controller runs on a given machine and controls a group of activities on
 * that machine.
 *
 * @author Keith M. Hughes
 */
public interface SpaceController extends ManagedResource {

  /**
   * Environment value giving the controller's
   * {@link NativeActivityRunnerFactory}.
   */
  String ENVIRONMENT_CONTROLLER_NATIVE_RUNNER = "spacecontroller.native.runner";

  /**
   * Configuration property giving the UUID of the controller.
   */
  String CONFIGURATION_CONTROLLER_UUID = "smartspaces.spacecontroller.uuid";

  /**
   * Configuration property giving the name of the controller.
   */
  String CONFIGURATION_CONTROLLER_NAME = "smartspaces.spacecontroller.name";

  /**
   * Configuration property giving the description of the controller.
   */
  String CONFIGURATION_CONTROLLER_DESCRIPTION = "smartspaces.spacecontroller.description";

  /**
   * Specification for standard controller mode.
   */
  String CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE = "standard";

  /**
   * Configuration property name for controller mode.
   */
  String CONFIGURATION_SMARTSPACES_CONTROLLER_MODE = "smartspaces.spacecontroller.mode";

  /**
   * Get the Smart Spaces environment.
   *
   * @return the space environment
   */
  SmartSpacesEnvironment getSpaceEnvironment();

  /**
   * Get information about the controller.
   *
   * @return information about the controller
   */
  SimpleSpaceController getControllerInfo();
}
