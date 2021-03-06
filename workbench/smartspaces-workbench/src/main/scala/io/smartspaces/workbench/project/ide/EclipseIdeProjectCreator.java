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

package io.smartspaces.workbench.project.ide;

import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.FreemarkerTemplater;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTaskContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Create an eclipse project.
 *
 * @author Keith M. Hughes
 */
public class EclipseIdeProjectCreator {

  /**
   * Where the template for the Eclipse project file is.
   */
  private static final String TEMPLATE_FILEPATH_ECLIPSE_PROJECT = "ide/eclipse/project.ftl";

  /**
   * What the Eclipse project file will be called.
   */
  private static final String FILENAME_PROJECT_FILE = ".project";

  /**
   * The Templater.
   */
  private final FreemarkerTemplater templater;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new project creator.
   *
   * @param templater
   *          the templater to use
   */
  public EclipseIdeProjectCreator(FreemarkerTemplater templater) {
    this.templater = templater;
  }

  /**
   * Create the IDE project.
   *
   * @param project
   *          project creating the IDE version for param spec the specification
   *          giving details about the IDE build
   * @param context
   *          the build context
   * @param spec
   *          the specification for the IDE project
   */
  public void createProject(Project project, ProjectTaskContext context,
      EclipseIdeProjectCreatorSpecification spec) {
    try {
      // Create the freemarkerContext hash
      Map<String, Object> freemarkerContext = new HashMap<>();
      freemarkerContext.put("project", project);

      spec.addSpecificationData(project, context, freemarkerContext);

      writeProjectFile(project, freemarkerContext);

      spec.writeAdditionalFiles(project, context, freemarkerContext, templater);
    } catch (Exception e) {
      context.getWorkbenchTaskContext().handleError("Error while creating eclipse project", e);
    }
  }

  /**
   * Write the project file.
   *
   * @param project
   *          project to write
   * @param freemarkerContext
   *          the Freemarker context
   *
   * @throws Exception
   *           something bad happened
   */
  private void writeProjectFile(Project project, Map<String, Object> freemarkerContext)
      throws Exception {
    templater.writeTemplate(freemarkerContext,
        fileSupport.newFile(project.getBaseDirectory(), FILENAME_PROJECT_FILE),
        TEMPLATE_FILEPATH_ECLIPSE_PROJECT);
  }
}
