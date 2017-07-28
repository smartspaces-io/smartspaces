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

package io.smartspaces.workbench.project.assembly;

import static io.smartspaces.workbench.project.constituent.AssemblyComponentProjectConstituent.PACK_FORMAT_ATTRIBUTE;
import static io.smartspaces.workbench.project.constituent.AssemblyComponentProjectConstituent.ZIP_PACK_FORMAT;
import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.builder.BaseProjectBuilder;

import java.io.File;

/**
 * A resource project builder.
 *
 * @author Trevor Pering
 */
public class AssemblyProjectBuilder extends BaseProjectBuilder {

  /**
   * File extension for an assembly artifact.
   */
  public static final String ASSEMBLY_FILE_EXTENSION = "zip";

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void build(Project project, ProjectTaskContext context)
      throws SmartSpacesException {
    AssemblyProject aproject = (AssemblyProject) project;
    String packFormat = project.getAttribute(PACK_FORMAT_ATTRIBUTE);
    if (!ZIP_PACK_FORMAT.equals(packFormat)) {
      throw new SimpleSmartSpacesException(String.format(
          "Project '%s' attribute was '%s', must be '%s'", PACK_FORMAT_ATTRIBUTE, packFormat,
          ZIP_PACK_FORMAT));
    }

    File stagingDirectory = context.getStagingDirectory();
    fileSupport.directoryExists(stagingDirectory);

    context.processGeneratedResources(stagingDirectory);
    context.processSources(stagingDirectory);

    writeResourceMap(aproject, stagingDirectory, context);

    assemble(aproject, stagingDirectory, context);
  }

  /**
   * Assemble the necessary resources into the target assembly bundle.
   *
   * @param project
   *          the project to build
   * @param stagingDirectory
   *          the project staging directory
   * @param context
   *          the context to use when building the activity
   */
  protected void assemble(AssemblyProject project, File stagingDirectory, ProjectTaskContext context) {
    // TODO(keith): Move this into the archiver and don't do it in here.
    File buildDirectory = context.getRootBuildDirectory();
    File assemblyFile = getBuildDestinationFile(project, buildDirectory, ASSEMBLY_FILE_EXTENSION);
    fileSupport.zip(assemblyFile, stagingDirectory);
  }
}
