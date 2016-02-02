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

package io.smartspaces.workbench.project.constituent;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectContext;
import io.smartspaces.workbench.project.ProjectTaskContext;

import java.io.File;

/**
 * Support class for implementing content project constituents.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseContentProjectConstituent extends BaseProjectConstituent implements
    ContentProjectConstituent {

  @Override
  public void processConstituent(Project project, ProjectContext context) {
    File stagingDirectory = ((ProjectTaskContext) context).getStagingDirectory();
    fileSupport.directoryExists(stagingDirectory);

    processConstituent(project, stagingDirectory, context);
  }

  @Override
  public String getSourceDirectory() throws SmartSpacesException {
    return null;
  }
}
