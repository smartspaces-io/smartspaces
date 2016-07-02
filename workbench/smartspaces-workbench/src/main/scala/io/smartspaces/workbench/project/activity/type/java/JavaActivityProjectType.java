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

package io.smartspaces.workbench.project.activity.type.java;

import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTemplate;
import io.smartspaces.workbench.project.activity.ActivityProject;
import io.smartspaces.workbench.project.activity.ActivityProjectTemplate;
import io.smartspaces.workbench.project.activity.builder.java.JavaActivityProjectBuilder;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.JavaEclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.java.JvmProjectType;

import com.google.common.collect.Lists;

/**
 * A Simple Java activity project type.
 *
 * @author Keith M. Hughes
 */
public class JavaActivityProjectType extends JvmProjectType {

  /**
   * Name for the builder.
   */
  public static final String BUILDER_NAME = "java";

  @Override
  public String getProjectTypeName() {
    return ActivityProject.PROJECT_TYPE_NAME;
  }

  @Override
  public boolean isProperType(Project project) {
    return ActivityProject.PROJECT_TYPE_NAME.equals(project.getType())
        && BUILDER_NAME.equals(project.getLanguage());
  }

  @Override
  public ProjectBuilder newBuilder() {
    return new JavaActivityProjectBuilder();
  }

  @Override
  public ProjectTemplate newProjectTemplate() {
    return new ActivityProjectTemplate();
  }

  @Override
  public EclipseIdeProjectCreatorSpecification getEclipseIdeProjectCreatorSpecification() {
    return new JavaEclipseIdeProjectCreatorSpecification(
        Lists.newArrayList(JvmProjectType.SOURCE_MAIN_JAVA),
        Lists.newArrayList(JvmProjectType.SOURCE_MAIN_TESTS));
  }
}