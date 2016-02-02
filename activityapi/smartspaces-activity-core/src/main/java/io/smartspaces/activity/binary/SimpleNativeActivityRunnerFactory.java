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

package io.smartspaces.activity.binary;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.configuration.CoreConfiguration;
import io.smartspaces.util.process.NativeApplicationRunnerParser;
import io.smartspaces.util.process.StandardNativeApplicationRunnerParser;

import org.apache.commons.logging.Log;

/**
 * A factory for receiving native activity launchers.
 *
 * @author Keith M. Hughes
 */
public class SimpleNativeActivityRunnerFactory implements NativeActivityRunnerFactory {

  /**
   * The runner parser to use.
   */
  private NativeApplicationRunnerParser runnerParser;

  /**
   * The Interactive Spaces environment being run under.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Construct a new factory.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public SimpleNativeActivityRunnerFactory(SmartSpacesEnvironment spaceEnvironment) {
    this(new StandardNativeApplicationRunnerParser(), spaceEnvironment);
  }

  /**
   * Construct a new factory.
   *
   * @param runnerParser
   *          the runner parser to use
   * @param spaceEnvironment
   *          the space environment to use
   */
  public SimpleNativeActivityRunnerFactory(NativeApplicationRunnerParser runnerParser,
      SmartSpacesEnvironment spaceEnvironment) {
    this.runnerParser = runnerParser;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public NativeActivityRunner newPlatformNativeActivityRunner(Log log) {
    String os =
        spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(
            CoreConfiguration.CONFIGURATION_SMARTSPACES_PLATFORM_OS);

    if (CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_LINUX.equals(os)) {
      return new LinuxNativeActivityRunner(runnerParser, spaceEnvironment, log);
    } else if (CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_OSX.equals(os)) {
      return new OsxNativeActivityRunner(runnerParser, spaceEnvironment, log);
    } else if (CoreConfiguration.CONFIGURATION_VALUE_PLATFORM_OS_WINDOWS.equals(os)) {
      return new WindowsNativeActivityRunner(runnerParser, spaceEnvironment, log);
    } else {
      throw new SimpleSmartSpacesException("Cannot create native activity launcher. Unknown OS "
          + os);
    }
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
