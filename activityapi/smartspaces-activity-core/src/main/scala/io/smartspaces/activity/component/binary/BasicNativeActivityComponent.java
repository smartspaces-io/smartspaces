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

package io.smartspaces.activity.component.binary;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.binary.NativeActivityRunner;
import io.smartspaces.activity.component.ActivityComponent;
import io.smartspaces.activity.component.BaseActivityComponent;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.system.core.configuration.CoreConfiguration;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.process.NativeApplicationRunner;
import io.smartspaces.util.process.NativeApplicationRunnerListener;
import io.smartspaces.util.process.restart.RestartStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link ActivityComponent} which launches native applications.
 *
 * @author Keith M. Hughes
 */
public class BasicNativeActivityComponent extends BaseActivityComponent implements
    NativeActivityComponent {

  /**
   * Control of the native activity.
   */
  private NativeActivityRunner nativeActivity;

  /**
   * The name of the configuration flag for getting the path to the executable.
   */
  private String executablePathProperty = CONFIGURATION_ACTIVITY_EXECUTABLE;

  /**
   * The name of the configuration flag for getting the executable flags.
   */
  private String executableFlagsProperty = CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS;

  /**
   * The name of the configuration flag for getting the executable environment.
   */
  private String executableEnvironmentProperty = CONFIGURATION_ACTIVITY_EXECUTABLE_ENVIRONMENT;

  /**
   * The restart strategy to use when the runner is finally created.
   */
  private RestartStrategy<NativeApplicationRunner> restartStrategy;

  /**
   * The listeners to add to the runner when it is created.
   */
  private List<NativeApplicationRunnerListener> listeners = new ArrayList<>();

  /**
   * The file support instance to use for dealing with files.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create the component which uses the properties
   * {@link #CONFIGURATION_ACTIVITY_EXECUTABLE} and
   * {@link #CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS} for execution path, flags,
   * and environment.
   */
  public BasicNativeActivityComponent() {
  }

  /**
   * Create the component with the properties to use for execution path and
   * flags.
   *
   * <p>
   * The environment property name is left alone.
   *
   * @param executablePathProperty
   *          config property name for the executable path
   * @param executableFlagsProperty
   *          config property prefix for the executable flags
   */
  public BasicNativeActivityComponent(String executablePathProperty, String executableFlagsProperty) {
    this.executablePathProperty = executablePathProperty;
    this.executableFlagsProperty = executableFlagsProperty;
  }

  /**
   * Create the component with the properties to use for execution path and
   * flags.
   *
   * @param executablePathProperty
   *          config property name for the executable path
   * @param executableFlagsProperty
   *          config property prefix for the executable flags
   * @param executableEnvironmentProperty
   *          config property prefix for the environment flags
   */
  public BasicNativeActivityComponent(String executablePathProperty,
      String executableFlagsProperty, String executableEnvironmentProperty) {
    this.executablePathProperty = executablePathProperty;
    this.executableFlagsProperty = executableFlagsProperty;
    this.executableEnvironmentProperty = executableEnvironmentProperty;
  }

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    Activity activity = componentContext.getActivity();
    String os =
        activity
            .getSpaceEnvironment()
            .getSystemConfiguration()
            .getRequiredPropertyString(
                CoreConfiguration.CONFIGURATION_SMARTSPACES_PLATFORM_OS);

    String activityPath =
        configuration.getRequiredPropertyString(executablePathProperty + "." + os);

    ActivityFilesystem activityFilesystem = activity.getActivityFilesystem();
    File activityFile =
        fileSupport.resolveFile(activityFilesystem.getInstallDirectory(), activityPath);
    if (fileSupport.isParent(activityFilesystem.getInstallDirectory(), activityFile)) {
      if (fileSupport.exists(activityFile)) {
        if (!activityFile.canExecute()) {
          activityFile.setExecutable(true);
        }
      } else {
        throw new SimpleSmartSpacesException(String.format(
            "The native executable %s does not exist", activityPath));
      }
    } else if (!isAppAlowed(activityPath)) {
      throw new SimpleSmartSpacesException(String.format(
          "The native executable is not local to the activity and is not allowed to run %s.",
          activityPath));
    }

    nativeActivity =
        activity.getActivityRuntime().getNativeActivityRunnerFactory()
            .newPlatformNativeActivityRunner(activity.getLog());

    nativeActivity.setExecutablePath(activityFile.getAbsolutePath());

    String commandFlags = configuration.getPropertyString(executableFlagsProperty + "." + os);
    nativeActivity.parseCommandArguments(commandFlags);

    String commandEnvironment =
        configuration.getPropertyString(executableEnvironmentProperty + "." + os);
    nativeActivity.parseEnvironment(commandEnvironment);

    if (restartStrategy != null) {
      nativeActivity.setRestartStrategy(restartStrategy);
    }

    for (NativeApplicationRunnerListener listener : listeners) {
      nativeActivity.addNativeApplicationRunnerListener(listener);
    }
  }

  @Override
  public void startupComponent() {
    nativeActivity.startup();
  }

  @Override
  public void shutdownComponent() {
    if (nativeActivity != null) {
      nativeActivity.shutdown();
      nativeActivity = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    if (nativeActivity != null) {
      return nativeActivity.isRunning();
    } else {
      return false;
    }
  }

  @Override
  public NativeActivityRunner getNativeActivityRunner() {
    return nativeActivity;
  }

  @Override
  public void setRestartStrategy(RestartStrategy<NativeApplicationRunner> restartStrategy) {
    this.restartStrategy = restartStrategy;
  }

  @Override
  public void addNativeApplicationRunnerListener(NativeApplicationRunnerListener listener) {
    listeners.add(listener);
  }

  /**
   * Is the application allowable?
   *
   * @param applicationPath
   *          path to the application
   *
   * @return {@code true} if allowed to run the app
   */
  private boolean isAppAlowed(String applicationPath) {
    // TODO(keith): Put a real check in here. May want a file containing
    // allowed applications.
    return true;
  }
}
