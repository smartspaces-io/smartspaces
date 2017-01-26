/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime.standalone.osgi;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.StandardLiveActivityRuntimeComponentFactory;
import io.smartspaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import io.smartspaces.liveactivity.runtime.osgi.OsgiServiceRegistrationLiveActivityRuntimeListener;
import io.smartspaces.liveactivity.runtime.standalone.development.DevelopmentStandaloneLiveActivityRuntime;
import io.smartspaces.osgi.service.SmartSpacesServiceOsgiBundleActivator;
import io.smartspaces.spacecontroller.SpaceController;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;
import io.smartspaces.system.resources.ContainerResourceManager;

import org.apache.commons.logging.Log;

/**
 * Bundle activator for the standalone controller. This will be activated if it
 * is included in the controller bootstrap directory, but will only do anything
 * if the appropriate mode is set in configuration.
 *
 * @author Trevor Pering
 */
public class StandaloneBundleActivator extends SmartSpacesServiceOsgiBundleActivator {

  /**
   * The default value for enabling the remote monitoring.
   */
  private static final String CONFIGURATION_VALUE_MONITOR_ENABLE_DEFAULT_STANDALONE = "false";

  /**
   * OSGi service tracker for the smart spaces control.
   */
  private MyServiceTracker<SmartSpacesSystemControl> smartspacesSystemControlTracker;

  /**
   * OSGi service tracker for the smart spaces control.
   */
  private MyServiceTracker<ContainerResourceManager> containerResourceManagerTracker;

  /**
   * Space environment from launcher.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Logging instance.
   *
   * @return logging instance for this activator
   */
  public Log getLog() {
    return spaceEnvironment.getLog();
  }

  @Override
  protected void onStart() {
    smartspacesSystemControlTracker = newMyServiceTracker(SmartSpacesSystemControl.class.getName());
    containerResourceManagerTracker = newMyServiceTracker(ContainerResourceManager.class.getName());
  }

  @Override
  protected void allRequiredServicesAvailable() {
    spaceEnvironment = getSmartSpacesEnvironment();

    getLog().info("Standalone are go!");

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    systemConfiguration.setProperty(
        RemoteLiveActivityRuntimeMonitorService.CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT,
        CONFIGURATION_VALUE_MONITOR_ENABLE_DEFAULT_STANDALONE);

    String controllerMode =
        systemConfiguration.getPropertyString(
            SpaceController.CONFIGURATION_NAME_CONTROLLER_MODE, null);
    if (!DevelopmentStandaloneLiveActivityRuntime.CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE
        .equals(controllerMode)) {
      getLog().info("Not activating standalone space controller, mode is " + controllerMode);
      return;
    }

    StandardLiveActivityRuntimeComponentFactory runtimeComponentFactory =
        new StandardLiveActivityRuntimeComponentFactory(spaceEnvironment,
            containerResourceManagerTracker.getMyService());

    DevelopmentStandaloneLiveActivityRuntime runtime =
        new DevelopmentStandaloneLiveActivityRuntime(runtimeComponentFactory, spaceEnvironment,
            new OsgiServiceRegistrationLiveActivityRuntimeListener(this),
            smartspacesSystemControlTracker.getMyService());
    addManagedResource(runtime);
  }
}
