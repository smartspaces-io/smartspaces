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

package io.smartspaces.workbench.project.test;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.java.JvmProjectExtension;

import java.io.File;

/**
 * The runner for Java tests.
 *
 * @author Keith M. Hughes
 */
public interface JavaTestRunner {

  /**
   * Run tests if there are any.
   *
   * @param jarDestinationFile
   *          the JAR file created for the project
   * @param extensions
   *          any Java extension added, can be {@code null}
   * @param context
   *          the build context for the project
   *
   * @throws SmartSpacesException
   *           the tests failed
   */
      void
      runTests(File jarDestinationFile, JvmProjectExtension extensions, ProjectTaskContext context)
          throws SmartSpacesException;
}