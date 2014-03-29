/*
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

package interactivespaces.workbench.project.group;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public class GroupProjectTemplate extends BaseProjectTemplate {

  @Override
  protected void onTemplateSetup(ProjectCreationSpecification spec) {
    System.out.println("Setup template");
  }

  @Override
  public void onTemplateWrite(ProjectCreationSpecification spec) {
    int projectIndex = 0;
    GroupProject groupProject = spec.getProject();
    try {
      for (Project project : groupProject.getProjectList()) {
        projectIndex++;
        spec.getWorkbench().getProjectCreator().create(makeCreationSpecification(spec, project, groupProject));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Error while creating projectGroup, project #%d/%d", projectIndex, groupProject.getProjectList().size()), e);
    }
  }

  /**
   * Make a creation specification for the given project in the group.
   *
   *
   * @param project
   *          individual project
   *
   * @param groupProject
   * @return project creation specification
   */
  private ProjectCreationSpecification makeCreationSpecification(
      ProjectCreationSpecification groupCreationSpec, Project project, Project groupProject) {
    ProjectCreationSpecification creationSpecification = new ProjectCreationSpecification();
    creationSpecification.setProject(project);
    creationSpecification.setBaseDirectory(groupCreationSpec.getBaseDirectory());
    creationSpecification.setSpecificationBase(groupCreationSpec.getSpecificationBase());
    creationSpecification.addTemplateDataEntry("baseDirectory", groupCreationSpec.getBaseDirectory());
    creationSpecification.addTemplateDataEntry("group", groupProject);
    return creationSpecification;
  }
}

