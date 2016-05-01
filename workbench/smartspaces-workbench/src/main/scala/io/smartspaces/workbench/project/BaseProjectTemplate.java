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

package io.smartspaces.workbench.project;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.FreemarkerTemplater;
import io.smartspaces.workbench.project.constituent.ProjectConstituent;
import io.smartspaces.workbench.project.creator.ProjectCreationContext;
import io.smartspaces.workbench.project.group.GroupProjectTemplateSpecification;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Base template for any kind of project.
 *
 * @author Keith M. Hughes
 */
public class BaseProjectTemplate implements ProjectTemplate {

  /**
   * Output file for temporarily writing variables.
   */
  public static final String TEMPLATE_VARIABLES_DUMP_FORMAT = "template_variables_%s.tmp";

  /**
   * Template variable name to use for holding the base directory.
   */
  public static final String BASE_DIRECTORY_VARIABLE = "baseDirectory";

  /**
   * Templater to use for constructing the template.
   */
  private FreemarkerTemplater templater;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Process the given creation specification.
   *
   * @param context
   *          specification to process.
   */
  @Override
  public void process(ProjectCreationContext context) {
    try {
      templateSetup(context);
      onTemplateSetup(context);
      onTemplateWrite(context);
      processTemplateConstituents(context);
    } catch (Exception e) {
      String description = context.getDescription();
      int lastPath = description.lastIndexOf(File.separatorChar);
      if (lastPath >= 0) {
        description = description.substring(lastPath + 1);
      }
      File outputFile =
          fileSupport.newFile(context.getBaseDirectory(),
              String.format(TEMPLATE_VARIABLES_DUMP_FORMAT, description));
      dumpVariables(outputFile, context.getTemplateData());
      throw new SimpleSmartSpacesException("Template variables can be found in "
          + outputFile.getAbsolutePath(), e);
    }
  }

  /**
   * Setup the template as necessary for basic operation.
   *
   * @param context
   *          spec for the project
   *
   */
  private void templateSetup(ProjectCreationContext context) {
    Project project = context.getProject();

    context.addTemplateDataEntry(BASE_DIRECTORY_VARIABLE, context.getBaseDirectory()
        .getAbsolutePath());
    context.addTemplateDataEntry("internalTemplates", templater.getTemplateDirectory()
        .getAbsoluteFile());
    context.addTemplateDataEntry("spec", context);
    context.addTemplateDataEntry("project", project);
  }

  /**
   * Template is being set up. Can be overridden in a project-type specific
   * project template.
   *
   * @param context
   *          spec for the project
   */
  protected void onTemplateSetup(ProjectCreationContext context) {
    // Default is to do nothing.
  }

  /**
   * Process the defined template constituents.
   *
   * @param context
   *          context for creating the project
   */
  private void processTemplateConstituents(ProjectCreationContext context) {
    Project project = context.getProject();
    GroupProjectTemplateSpecification groupProjectTemplateSpecification =
        context.getGroupProjectTemplateSpecification();
    List<ProjectConstituent> projectConstituents =
        project != null ? project.getExtraConstituents() : groupProjectTemplateSpecification
            .getExtraConstituents();
    for (ProjectConstituent constituent : projectConstituents) {
      constituent.processConstituent(project, context);
    }
  }

  /**
   * Function called on template write. Can be overridden to provide different
   * functionality for other project types.
   *
   * @param context
   *          specification that is being written
   */
  public void onTemplateWrite(ProjectCreationContext context) {
    // Default is to do nothing.
  }

  /**
   * Dump the given variables to an output file.
   *
   * @param outputFile
   *          variable dump output file
   * @param variables
   *          variables to dump
   */
  private void dumpVariables(File outputFile, Map<String, Object> variables) {
    PrintWriter variableWriter = null;
    boolean noException = true;
    try {
      variableWriter = new PrintWriter(outputFile);
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        variableWriter.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
      }
    } catch (Exception e) {
      noException = false;
      throw new SimpleSmartSpacesException("Error writing variable dump file "
          + outputFile.getAbsolutePath(), e);
    } finally {
      fileSupport.close(variableWriter, noException);
    }
  }

  /**
   * Get the templater used by this template.
   *
   * @return templater in use
   */
  public FreemarkerTemplater getTemplater() {
    return templater;
  }

  /**
   * Set the templater to use for this project.
   *
   * @param templater
   *          templater to use
   */
  @Override
  public void setTemplater(FreemarkerTemplater templater) {
    this.templater = templater;
  }
}
