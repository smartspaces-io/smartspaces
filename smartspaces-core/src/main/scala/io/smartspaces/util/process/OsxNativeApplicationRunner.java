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

package io.smartspaces.util.process;

import io.smartspaces.system.SmartSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * A {@link NativeApplicationRunner} for OS-X.
 *
 * @author Keith M. Hughes
 */
public class OsxNativeApplicationRunner extends UnixNativeApplicationRunner {

  /**
   * Construct the runner.
   *
   * @param runnerParser
   *          the runner parser
   * @param spaceEnvironment
   *          the space environment to use
   * @param log
   *          the logger to use
   */
  public OsxNativeApplicationRunner(NativeApplicationRunnerParser runnerParser,
      SmartSpacesEnvironment spaceEnvironment, Log log) {
    super(runnerParser, spaceEnvironment, log);
  }
}
