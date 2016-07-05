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

package io.smartspaces.workbench.project.group;

import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.ProjectTemplate;
import io.smartspaces.workbench.project.ProjectType;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.NonJavaEclipseIdeProjectCreatorSpecification;

/**
 * A project type for group projects, which are essentially a collection of
 * other projects.
 *
 * @author Trevor Pering
 */
public class GroupProjectType implements ProjectType {

  @Override
  public String getProjectTypeName() {
    return GroupProjectTemplateSpecification.PROJECT_TYPE_NAME;
  }

  @Override
  public boolean isProperType(Project project) {
    return GroupProjectTemplateSpecification.PROJECT_TYPE_NAME.equals(project.getType());
  }

  @Override
  public ProjectBuilder newBuilder(ProjectTaskContext projectTaskContext) {
    return null; // new GroupProjectBuilder();
  }

  @Override
  public ProjectTemplate newProjectTemplate() {
    return new GroupProjectTemplate();
  }

  @Override
  public EclipseIdeProjectCreatorSpecification
      getEclipseIdeProjectCreatorSpecification(ProjectTaskContext projectTaskContext) {
    return new NonJavaEclipseIdeProjectCreatorSpecification();
  }
}
