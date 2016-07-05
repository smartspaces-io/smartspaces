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

package io.smartspaces.workbench.project.creator;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.workbench.FreemarkerTemplater;
import io.smartspaces.workbench.SmartSpacesWorkbench;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTemplate;
import io.smartspaces.workbench.project.ProjectType;
import io.smartspaces.workbench.project.group.GroupProjectTemplate;
import io.smartspaces.workbench.project.group.GroupProjectTemplateSpecification;

/**
 * A {@link ProjectCreator} implementation.
 *
 * @author Keith M. Hughes
 */
public class ProjectCreatorImpl implements ProjectCreator {

  /**
   * Templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final SmartSpacesWorkbench workbench;

  /**
   * Create a basic instance.
   *
   * @param workbench
   *          containing workbench
   * @param templater
   *          templater to use
   */
  public ProjectCreatorImpl(SmartSpacesWorkbench workbench, FreemarkerTemplater templater) {
    this.workbench = workbench;
    this.templater = templater;
  }

  @Override
  public void create(ProjectCreationContext context) {
    Project project = context.getProject();
    GroupProjectTemplateSpecification groupProjectTemplateSpecification =
        context.getGroupProjectTemplateSpecification();

    if (project != null) {
      createProject(context, project);
    } else if (groupProjectTemplateSpecification != null) {
      createGroupProject(context);
    } else {
      throw new SimpleSmartSpacesException("Context has neither Project nor GroupProject");
    }
  }

  /**
   * Create a project.
   *
   * @param context
   *          context for creation
   * @param project
   *          project to create
   */
  private void createProject(ProjectCreationContext context, Project project) {
    ProjectType projectType = workbench.getProjectTypeRegistry().getProjectType(project);
    if (projectType == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Invalid type for project type/builder %s/%s", project.getType(),
          project.getLanguage()));
    }
    ProjectTemplate projectTemplate = projectType.newProjectTemplate();
    projectTemplate.setTemplater(templater);
    projectTemplate.process(context);
  }

  /**
   * Create a group project.
   *
   * @param context
   *          context for creation
   */
  private void createGroupProject(ProjectCreationContext context) {
    ProjectTemplate projectTemplate = new GroupProjectTemplate();
    projectTemplate.setTemplater(templater);
    projectTemplate.process(context);
  }
}
