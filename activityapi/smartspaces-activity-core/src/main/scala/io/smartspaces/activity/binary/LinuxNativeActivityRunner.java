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

import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.process.LinuxNativeApplicationRunner;
import io.smartspaces.util.process.NativeApplicationRunnerParser;

import org.apache.commons.logging.Log;

/**
 * A {@link NativeActivityRunner} for Linux systems.
 *
 * @author Keith M. Hughes
 */
public class LinuxNativeActivityRunner extends LinuxNativeApplicationRunner implements
    NativeActivityRunner {

  /**
   * Create a new activity runner for linux.
   *
   * @param runnerParser
   *          the runner parser to use
   * @param spaceEnvironment
   *          environment to use
   * @param log
   *          logger for logging
   */
  public LinuxNativeActivityRunner(NativeApplicationRunnerParser runnerParser,
      SmartSpacesEnvironment spaceEnvironment, Log log) {
    super(runnerParser, spaceEnvironment, log);
  }
}
