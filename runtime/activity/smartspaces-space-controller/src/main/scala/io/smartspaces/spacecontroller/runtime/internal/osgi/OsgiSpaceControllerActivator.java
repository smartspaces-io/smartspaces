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

package io.smartspaces.spacecontroller.runtime.internal.osgi;

import org.ros.osgi.common.RosEnvironment;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.evaluation.ExpressionEvaluatorFactory;
import io.smartspaces.liveactivity.runtime.LiveActivityRuntimeComponentFactory;
import io.smartspaces.liveactivity.runtime.LiveActivityStorageManager;
import io.smartspaces.liveactivity.runtime.SimpleActivityInstallationManager;
import io.smartspaces.liveactivity.runtime.SimpleLiveActivityStorageManager;
import io.smartspaces.liveactivity.runtime.StandardLiveActivityRuntime;
import io.smartspaces.liveactivity.runtime.StandardLiveActivityRuntimeComponentFactory;
import io.smartspaces.liveactivity.runtime.alert.LoggingAlertStatusManager;
import io.smartspaces.liveactivity.runtime.configuration.BasePropertyFileLiveActivityConfigurationManager;
import io.smartspaces.liveactivity.runtime.configuration.ProductionPropertyFileLiveActivityConfigurationManager;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationManager;
import io.smartspaces.liveactivity.runtime.logging.SmartSpacesEnvironmentLiveActivityLogFactory;
import io.smartspaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import io.smartspaces.liveactivity.runtime.monitor.internal.StandardRemoteLiveActivityRuntimeMonitorService;
import io.smartspaces.liveactivity.runtime.osgi.OsgiServiceRegistrationLiveActivityRuntimeListener;
import io.smartspaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import io.smartspaces.liveactivity.runtime.repository.internal.file.FileLocalLiveActivityRepository;
import io.smartspaces.osgi.service.SmartSpacesServiceOsgiBundleActivator;
import io.smartspaces.spacecontroller.SpaceController;
import io.smartspaces.spacecontroller.resource.deployment.ContainerResourceDeploymentManager;
import io.smartspaces.spacecontroller.resource.deployment.ControllerContainerResourceDeploymentManager;
import io.smartspaces.spacecontroller.runtime.FileSystemSpaceControllerInfoPersister;
import io.smartspaces.spacecontroller.runtime.SpaceControllerActivityInstallationManager;
import io.smartspaces.spacecontroller.runtime.SpaceControllerDataBundleManager;
import io.smartspaces.spacecontroller.runtime.StandardSpaceController;
import io.smartspaces.spacecontroller.runtime.StandardSpaceControllerDataBundleManager;
import io.smartspaces.spacecontroller.runtime.comm.SimpleTcpSpaceControllerCommunicator;
import io.smartspaces.spacecontroller.runtime.configuration.StandardSpaceControllerConfigurationManager;
import io.smartspaces.spacecontroller.runtime.internal.StandardSpaceControllerActivityInstallationManager;
import io.smartspaces.spacecontroller.ui.internal.osgi.OsgiSpaceControllerShell;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesSystemControl;
import io.smartspaces.system.resources.ContainerResourceManager;
import io.smartspaces.tasks.SequentialTaskQueue;
import io.smartspaces.tasks.SimpleSequentialTaskQueue;

/**
 * An OSGi activator for an Smart Spaces space controller.
 *
 * @author Keith M. Hughes
 */
public class OsgiSpaceControllerActivator extends SmartSpacesServiceOsgiBundleActivator {

  /**
   * The default value for enabling the remote monitoring.
   */
  private static final String CONFIGURATION_VALUE_MONITOR_ENABLE_DEFAULT_CONTROLLER = "true";

  /**
   * OSGi service tracker for the smart spaces control.
   */
  private MyServiceTracker<SmartSpacesSystemControl> smartspacesSystemControlTracker;

  /**
   * OSGi service tracker for the ROS environment.
   */
  private MyServiceTracker<RosEnvironment> rosEnvironmentTracker;

  /**
   * OSGi service tracker for the expression evaluator factory.
   */
  private MyServiceTracker<ExpressionEvaluatorFactory> expressionEvaluatorFactoryTracker;

  /**
   * OSGi service tracker for the container resource manager.
   */
  private MyServiceTracker<ContainerResourceManager> containerResourceManagerTracker;

  /**
   * The space environment for this controller.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  @Override
  public void onStart() {
    smartspacesSystemControlTracker = newMyServiceTracker(SmartSpacesSystemControl.class.getName());

    rosEnvironmentTracker = newMyServiceTracker(RosEnvironment.class.getName());

    expressionEvaluatorFactoryTracker =
        newMyServiceTracker(ExpressionEvaluatorFactory.class.getName());

    containerResourceManagerTracker = newMyServiceTracker(ContainerResourceManager.class.getName());
  }

  @Override
  protected void allRequiredServicesAvailable() {
    spaceEnvironment = getsmartspacesEnvironmentTracker().getMyService();

    String controllerMode =
        spaceEnvironment.getSystemConfiguration().getPropertyString(
            SpaceController.CONFIGURATION_SMARTSPACES_CONTROLLER_MODE,
            SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE);
    if (SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE.equals(controllerMode)) {
      activateStandardSpaceController();
    } else {
      spaceEnvironment.getLog().info(
          "Not activating standard space controller, mode is " + controllerMode);
    }
  }

  /**
   * Initialize components that are necessary only to the
   * {@link StandardSpaceController}, and then instantiate and register the
   * space controller itself..
   */
  private void activateStandardSpaceController() {
    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    systemConfiguration.setProperty(
        RemoteLiveActivityRuntimeMonitorService.CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT,
        CONFIGURATION_VALUE_MONITOR_ENABLE_DEFAULT_CONTROLLER);

    SmartSpacesSystemControl spaceSystemControl = smartspacesSystemControlTracker.getMyService();
    RosEnvironment rosEnvironment = rosEnvironmentTracker.getMyService();
    ExpressionEvaluatorFactory expressionEvaluatorFactory =
        expressionEvaluatorFactoryTracker.getMyService();

    ContainerResourceManager containerResourceManager =
        containerResourceManagerTracker.getMyService();

    ContainerResourceDeploymentManager containerResourceDeploymentManager =
        new ControllerContainerResourceDeploymentManager(containerResourceManager, spaceEnvironment);
    addManagedResource(containerResourceDeploymentManager);

    LiveActivityStorageManager liveActivityStorageManager =
        new SimpleLiveActivityStorageManager(spaceEnvironment);
    addManagedResource(liveActivityStorageManager);

    LocalLiveActivityRepository liveActivityRepository =
        new FileLocalLiveActivityRepository(liveActivityStorageManager, spaceEnvironment);
    addManagedResource(liveActivityRepository);

    BasePropertyFileLiveActivityConfigurationManager liveActivityConfigurationManager =
        new ProductionPropertyFileLiveActivityConfigurationManager(expressionEvaluatorFactory,
            spaceEnvironment);

    ActivityInstallationManager activityInstallationManager =
        new SimpleActivityInstallationManager(liveActivityRepository, liveActivityStorageManager,
            spaceEnvironment);
    addManagedResource(activityInstallationManager);

    SpaceControllerActivityInstallationManager controllerActivityInstaller =
        new StandardSpaceControllerActivityInstallationManager(activityInstallationManager,
            spaceEnvironment);
    addManagedResource(controllerActivityInstaller);

    SmartSpacesEnvironmentLiveActivityLogFactory activityLogFactory =
        new SmartSpacesEnvironmentLiveActivityLogFactory(spaceEnvironment);

    SimpleTcpSpaceControllerCommunicator spaceControllerCommunicator =
        new SimpleTcpSpaceControllerCommunicator(spaceEnvironment);

    SpaceControllerDataBundleManager dataBundleManager =
        new StandardSpaceControllerDataBundleManager();
    dataBundleManager.setActivityStorageManager(liveActivityStorageManager);

    StandardSpaceControllerConfigurationManager spaceControllerConfigurationManager =
        new StandardSpaceControllerConfigurationManager(spaceEnvironment);
    addManagedResource(spaceControllerConfigurationManager);

    SequentialTaskQueue taskQueue =
        new SimpleSequentialTaskQueue(spaceEnvironment, spaceEnvironment.getLog());
    addManagedResource(taskQueue);

    LoggingAlertStatusManager alertStatusManager =
        new LoggingAlertStatusManager(spaceEnvironment.getLog());
    addManagedResource(alertStatusManager);

    LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory =
        new StandardLiveActivityRuntimeComponentFactory(spaceEnvironment, containerResourceManager);

    RemoteLiveActivityRuntimeMonitorService runtimeDebugService =
        new StandardRemoteLiveActivityRuntimeMonitorService();

    StandardLiveActivityRuntime liveActivityRuntime =
        new StandardLiveActivityRuntime(liveActivityRuntimeComponentFactory,
            liveActivityRepository, activityInstallationManager, activityLogFactory,
            liveActivityConfigurationManager, liveActivityStorageManager, alertStatusManager,
            taskQueue, runtimeDebugService, spaceEnvironment);
    addManagedResource(liveActivityRuntime);

    liveActivityRuntime.addRuntimeListener(new OsgiServiceRegistrationLiveActivityRuntimeListener(
        this));

    StandardSpaceController spaceController =
        new StandardSpaceController(controllerActivityInstaller,
            containerResourceDeploymentManager, spaceControllerCommunicator,
            new FileSystemSpaceControllerInfoPersister(), spaceSystemControl, dataBundleManager,
            spaceControllerConfigurationManager, liveActivityRuntime, taskQueue, spaceEnvironment);
    addManagedResource(spaceController);

    OsgiSpaceControllerShell controllerShell =
        new OsgiSpaceControllerShell(spaceController, spaceSystemControl, liveActivityRepository,
            getBundleContext());
    addManagedResource(controllerShell);
  }
}
