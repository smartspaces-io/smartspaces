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

package io.smartspaces.spacecontroller.runtime;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.container.control.message.StandardMasterSpaceControllerCodec;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.spacecontroller.SpaceController;
import io.smartspaces.system.SmartSpacesEnvironment;

/**
 * Base implementation for a space controller.
 *
 * @author Trevor Pering
 */
public abstract class BaseSpaceController implements SpaceController {

  /**
   * Information about the controller.
   */
  private final SimpleSpaceController controllerInfo = new SimpleSpaceController();

  /**
   * The Smart Spaces environment being run under.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * Construct a controller with the given space environment.
   *
   * @param spaceEnvironment
   *          space environment to use
   */
  public BaseSpaceController(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    getSpaceEnvironment().getLog().info("Controller starting up");
    obtainControllerInfo();

    setEnvironmentValues();
  }

  @Override
  public void shutdown() {
    getSpaceEnvironment().getLog().info("Controller shutting down");
  }

  @Override
  public SmartSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  /**
   * Get controller information from the configs.
   */
  private void obtainControllerInfo() {
    Configuration systemConfiguration = getSpaceEnvironment().getSystemConfiguration();

    controllerInfo.setUuid(systemConfiguration.getPropertyString(CONFIGURATION_NAME_CONTROLLER_UUID));
    controllerInfo
        .setName(systemConfiguration.getPropertyString(CONFIGURATION_NAME_CONTROLLER_NAME, ""));
    controllerInfo.setDescription(
        systemConfiguration.getPropertyString(CONFIGURATION_NAME_CONTROLLER_DESCRIPTION, ""));
    controllerInfo.setHostId(
        systemConfiguration.getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOSTID));
    controllerInfo.setHostName(systemConfiguration
        .getPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_NAME));
    controllerInfo.setHostControlPort(systemConfiguration.getPropertyInteger(
        SpaceController.CONFIGURATION_NAME_CONTROLLER_HOST_CONTROL_PORT,
        StandardMasterSpaceControllerCodec.CONTROLLER_SERVER_PORT));
  }

  @Override
  public SimpleSpaceController getControllerInfo() {
    return controllerInfo;
  }

  /**
   * Set values in the space environment that the controller provides.
   */
  private void setEnvironmentValues() {
    // getSpaceEnvironment().setValue(ENVIRONMENT_CONTROLLER_NATIVE_RUNNER,
    // getNativeActivityRunnerFactory());
  }
}
