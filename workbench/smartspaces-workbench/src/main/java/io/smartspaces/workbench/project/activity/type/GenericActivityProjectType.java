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

package io.smartspaces.workbench.project.activity.type;

import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTemplate;
import io.smartspaces.workbench.project.activity.ActivityProject;
import io.smartspaces.workbench.project.activity.ActivityProjectTemplate;
import io.smartspaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.JavaEclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.java.JavaProjectType;

import com.google.common.collect.Lists;

/**
 * A generic activity project type with a configurable builder type.
 *
 * @author Keith M. Hughes
 */
public class GenericActivityProjectType implements ProjectType {

  /**
   * Builder type of this generic activity project type.
   */
  private final String builderType;

  /**
   * Construct a new instance.
   *
   * @param builderType
   *          the builder type for this activity
   */
  public GenericActivityProjectType(String builderType) {
    this.builderType = builderType;
  }

  @Override
  public String getProjectTypeName() {
    return ActivityProject.PROJECT_TYPE_NAME;
  }

  @Override
  public boolean isProperType(Project project) {
    return ActivityProject.PROJECT_TYPE_NAME.equals(project.getType())
        && builderType.equals(project.getBuilderType());
  }

  @Override
  public ProjectBuilder newBuilder() {
    return new BaseActivityProjectBuilder();
  }

  @Override
  public ProjectTemplate newProjectTemplate() {
    return new ActivityProjectTemplate();
  }

  @Override
  public EclipseIdeProjectCreatorSpecification getEclipseIdeProjectCreatorSpecification() {
    return new JavaEclipseIdeProjectCreatorSpecification(
        Lists.newArrayList(ProjectType.SOURCE_MAIN_RESOURCES),
        Lists.newArrayList(JavaProjectType.SOURCE_MAIN_TESTS));
  }
}