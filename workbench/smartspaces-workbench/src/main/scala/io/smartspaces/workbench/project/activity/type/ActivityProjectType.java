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

import io.smartspaces.workbench.language.ProgrammingLanguageSupport;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectFileLayout;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.ProjectTemplate;
import io.smartspaces.workbench.project.ProjectType;
import io.smartspaces.workbench.project.activity.ActivityProject;
import io.smartspaces.workbench.project.activity.ActivityProjectTemplate;
import io.smartspaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import io.smartspaces.workbench.project.activity.builder.java.JvmActivityProjectBuilder;
import io.smartspaces.workbench.project.activity.type.android.AndroidJvmProjectExtension;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.JavaEclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.NonJavaEclipseIdeProjectCreatorSpecification;

import com.google.common.collect.Lists;

/**
 * A generic activity project type with a configurable builder type.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectType implements ProjectType {

  /**
   * The extension for android projects.
   */
  private final AndroidJvmProjectExtension androidExtension = new AndroidJvmProjectExtension();

  @Override
  public String getProjectTypeName() {
    return ActivityProject.PROJECT_TYPE_NAME;
  }

  @Override
  public boolean isProperType(Project project) {
    return ActivityProject.PROJECT_TYPE_NAME.equals(project.getType());
  }

  @Override
  public ProjectBuilder newBuilder(ProjectTaskContext projectTaskContext) {
    Project project = projectTaskContext.getProject();
    String language = project.getLanguage();
    if (language == null) {
      return new BaseActivityProjectBuilder();
    } else {
      if ("android".equals(project.getPlatform())) {
        return new JvmActivityProjectBuilder(androidExtension);
      } else {
        return new JvmActivityProjectBuilder();
      }
    }
  }

  @Override
  public ProjectTemplate newProjectTemplate() {
    return new ActivityProjectTemplate();
  }

  @Override
  public EclipseIdeProjectCreatorSpecification
      getEclipseIdeProjectCreatorSpecification(ProjectTaskContext projectTaskContext) {
    Project project = projectTaskContext.getProject();
    String language = project.getLanguage();
    if (language == null) {
      // Lists.newArrayList(ProjectFileLayout.SOURCE_MAIN_RESOURCES),
      // Lists.newArrayList(ProjectFileLayout.SOURCE_TEST_RESOURCES)
      return new NonJavaEclipseIdeProjectCreatorSpecification();
    } else {
      ProgrammingLanguageSupport languageSupport = projectTaskContext.getWorkbenchTaskContext()
          .getWorkbench().getProgrammingLanguageRegistry()
          .getProgrammingLanguageSupport(projectTaskContext.getProject().getLanguage());

      if ("android".equals(project.getPlatform())) {
        return new JavaEclipseIdeProjectCreatorSpecification(
            Lists.newArrayList(languageSupport.getMainSourceDirectory()),
            Lists.newArrayList(languageSupport.getTestSourceDirectory()), androidExtension);
      } else {
        return new JavaEclipseIdeProjectCreatorSpecification(
            Lists.newArrayList(languageSupport.getMainSourceDirectory()),
            Lists.newArrayList(languageSupport.getTestSourceDirectory()));
      }
    }
  }
}
