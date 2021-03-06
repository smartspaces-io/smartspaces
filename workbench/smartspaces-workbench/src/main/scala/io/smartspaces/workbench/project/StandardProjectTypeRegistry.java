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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.workbench.project.activity.ActivityProject;
import io.smartspaces.workbench.project.activity.type.ActivityProjectType;
import io.smartspaces.workbench.project.assembly.AssemblyProject;
import io.smartspaces.workbench.project.assembly.AssemblyProjectType;
import io.smartspaces.workbench.project.group.GroupProjectType;
import io.smartspaces.workbench.project.library.LibraryProject;
import io.smartspaces.workbench.project.library.LibraryProjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple implemention of a {@link ProjectTypeRegistry}.
 *
 * @author Keith M. Hughes
 */
public class StandardProjectTypeRegistry implements ProjectTypeRegistry {

  /**
   * Name for the builder type of script activities.
   */
  public static final String SCRIPT_ACTIVITY_BUILDER_TYPE = "script";

  /**
   * Name for the builder type of native activities.
   */
  public static final String NATIVE_ACTIVITY_BUILDER_TYPE = "native";

  /**
   * The mapping of names to project types.
   */
  private final List<ProjectType> projectTypes = new ArrayList<>();

  /**
   * Map containing project creator factories, by project type.
   */
  private final Map<String, Class<? extends Project>> projectCreatorMap = new HashMap<>();

  /**
   * Create a basic registry for project types.
   */
  public StandardProjectTypeRegistry() {
    registerProjectType(new ActivityProjectType());
    registerProjectType(new LibraryProjectType());
    registerProjectType(new AssemblyProjectType());
    registerProjectType(new GroupProjectType());

    projectCreatorMap.put(ActivityProject.PROJECT_TYPE_NAME, ActivityProject.class);
    projectCreatorMap.put(LibraryProject.PROJECT_TYPE_NAME, LibraryProject.class);
    projectCreatorMap.put(AssemblyProject.PROJECT_TYPE_NAME, AssemblyProject.class);
    // projectCreatorMap.put(GroupProject.PROJECT_TYPE_NAME,
    // GroupProject.class);
  }

  @Override
  public ProjectType getProjectType(Project project) {
    if (project == null) {
      return null;
    }

    for (ProjectType type : projectTypes) {
      if (type.isProperType(project)) {
        return type;
      }
    }

    return null;
  }

  @Override
  public void registerProjectType(ProjectType type) {
    projectTypes.add(type);
  }

  @Override
  public <T extends Project> T newProject(String typeName) throws SmartSpacesException {
    Class<? extends Project> clazz = projectCreatorMap.get(typeName);
    if (clazz != null) {
      try {
        @SuppressWarnings("unchecked")
        T project = (T) clazz.newInstance();
        project.setType(typeName);

        return project;
      } catch (Exception e) {
        throw new SimpleSmartSpacesException(
            String.format("Could not create project class with type name %s", typeName), e);
      }
    } else {
      throw new SimpleSmartSpacesException(String.format("Unknown project type name %s", typeName));
    }
  }
}
