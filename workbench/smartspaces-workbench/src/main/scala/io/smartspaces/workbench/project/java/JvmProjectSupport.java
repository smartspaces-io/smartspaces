/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.workbench.project.java;

import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.tasks.WorkbenchTaskContext;

import java.io.File;
import java.util.List;

public interface JvmProjectSupport {

  /**
   * The configuration name for the project bootstrap classpath.
   */
  String CONFIGURATION_NAME_PROJECT_CLASSPATH_BOOTSTRAP = "project.classpath.bootstrap";

  /**
   * The configuration name for the project additional (non-bootstrap)
   * classpath.
   */
  String CONFIGURATION_NAME_PROJECT_CLASSPATH_ADDITIONS = "project.classpath.additions";

  /**
   * The extras component for testing support.
   */
  String TESTING_EXTRAS_COMPONENT = "testing";

  /**
   * Get a classpath that would be used at runtime for the project.
   *
   * @param needsDynamicArtifacts
   *          {@code true} if needs artifacts from the dynamic projects
   * @param projectTaskContext
   *          the project build context
   * @param classpath
   *          the classpath list to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbenchTaskContext
   *          the workbench task context
   */
  void getRuntimeClasspath(boolean needsDynamicArtifacts, ProjectTaskContext projectTaskContext,
      List<File> classpath, JvmProjectExtension extension,
      WorkbenchTaskContext workbenchTaskContext);

  /**
   * Get a classpath that would be used as part of the project for the project.
   *
   * <p>
   * This includes runtime classes.
   *
   * @param needsDynamicArtifacts
   *          {@code true} if needs artifacts from the dynamic projects
   * @param projectTaskContext
   *          the project build context
   * @param classpath
   *          the classpath to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param wokbenchTaskContext
   *          the workbench task context
   */
  void getProjectClasspath(boolean needsDynamicArtifacts, ProjectTaskContext projectTaskContext,
      List<File> classpath, JvmProjectExtension extension,
      WorkbenchTaskContext wokbenchTaskContext);

}