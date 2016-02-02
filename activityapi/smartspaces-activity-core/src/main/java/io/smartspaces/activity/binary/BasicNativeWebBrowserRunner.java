/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.activity.binary;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivitySystemConfiguration;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.process.NativeApplicationRunner;
import io.smartspaces.util.process.restart.LimitedRetryRestartStrategy;
import io.smartspaces.util.process.restart.RestartStrategy;

import java.text.MessageFormat;

/**
 * Basic implementation of the web browser runner.
 *
 * @author Keith M. Hughes
 */
public class BasicNativeWebBrowserRunner implements NativeWebBrowserRunner {

  /**
   * The number of times the restart strategy will attempt a restart before
   * giving up.
   */
  public static final int RESTART_STRATEGY_NUMBER_RETRIES = 4;

  /**
   * How long the browser must be running before restart is considered
   * successful in milliseconds.
   */
  public static final int RESTART_STRATEGY_SUCCESS_TIME = 4000;

  /**
   * How often the restart will sample to see if restart has happened, in
   * milliseconds.
   */
  public static final int RESTART_STRATEGY_SAMPLE_TIME = 1000;

  /**
   * Activity this browser is running under.
   */
  private final Activity activity;

  /**
   * Launcher for the browser.
   */
  private NativeActivityRunner browserRunner;

  /**
   * The file support instance to use for operating on the filesystem.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new browser runner.
   *
   * @param activity
   *          the activity the browser is running
   */
  public BasicNativeWebBrowserRunner(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void startup(String initialUrl, boolean debug) {
    browserRunner =
        activity.getActivityRuntime().getNativeActivityRunnerFactory()
            .newPlatformNativeActivityRunner(activity.getLog());

    Configuration configuration = activity.getConfiguration();

    // If the browser executable doesn't exist, we cannot start it on this
    // system.
    String browserExecutable =
        ActivitySystemConfiguration.getActivityNativeBrowserBinary(configuration);
    if (!fileSupport.exists(fileSupport.newFile(browserExecutable))) {
      throw SimpleSmartSpacesException.newFormattedException(
          "%s - cannot find web browser executable %s", getClass().getName(), browserExecutable);
    }

    browserRunner.setExecutablePath(browserExecutable);

    String commandFlags =
        MessageFormat.format(
            ActivitySystemConfiguration.getActivityNativeBrowserCommandFlags(configuration, debug),
            initialUrl);

    browserRunner.parseCommandArguments(commandFlags);

    String commandEnvironment =
        ActivitySystemConfiguration.getActivityNativeBrowserCommandEnvironment(configuration);
    if (commandEnvironment != null) {
      browserRunner.parseEnvironment(commandEnvironment);
    }

    browserRunner.setRestartStrategy(getDefaultRestartStrategy());

    browserRunner.startup();
  }

  @Override
  public RestartStrategy<NativeApplicationRunner> getDefaultRestartStrategy() {
    return new LimitedRetryRestartStrategy<NativeApplicationRunner>(
        RESTART_STRATEGY_NUMBER_RETRIES, RESTART_STRATEGY_SAMPLE_TIME,
        RESTART_STRATEGY_SUCCESS_TIME, activity.getSpaceEnvironment());
  }

  @Override
  public void shutdown() {
    if (browserRunner != null) {
      browserRunner.shutdown();

      browserRunner = null;
    }
  }

  @Override
  public boolean isRunning() {
    if (browserRunner != null) {
      return browserRunner.isRunning();
    } else {
      return false;
    }
  }

  @Override
  public NativeApplicationRunner getNativeWebBrowserRunner() {
    return browserRunner;
  }
}
