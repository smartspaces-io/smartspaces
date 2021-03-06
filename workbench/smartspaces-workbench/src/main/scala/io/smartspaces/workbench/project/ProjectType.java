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

package io.smartspaces.workbench.project;

import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;

/**
 * A project type.
 *
 * <p>
 * This gives the builders, creators, etc for project types.
 *
 * @author Keith M. Hughes
 */
public interface ProjectType {

  /**
   * Get the project type name for this project types.
   *
   * @return project type name
   */
  String getProjectTypeName();

  /**
   * Can this project type handle the given project?
   *
   * @param project
   *          the project to test against
   *
   * @return {@code true} if can handle this project type
   */
  boolean isProperType(Project project);

  /**
   * Get an activity builder for the project type.
   * 
   * @param projectTaskContext
   *          the context for project tasks
   *
   * @return an activity builder
   */
  ProjectBuilder newBuilder(ProjectTaskContext projectTaskContext);

  /**
   * Get a new project template for the project type.
   *
   * @return a project template
   */
  ProjectTemplate newProjectTemplate();

  /**
   * Get the specification for Eclipse project building.
   * 
   * @param projectTaskContext
   *          the context for the project task
   *
   * @return the specification for Eclipse project building
   */
  EclipseIdeProjectCreatorSpecification
      getEclipseIdeProjectCreatorSpecification(ProjectTaskContext projectTaskContext);
}
