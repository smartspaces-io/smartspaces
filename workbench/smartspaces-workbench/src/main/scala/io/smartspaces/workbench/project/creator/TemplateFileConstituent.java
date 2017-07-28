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
import io.smartspaces.workbench.project.BaseProjectTemplate;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectContext;
import io.smartspaces.workbench.project.constituent.BaseProjectConstituent;
import io.smartspaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import io.smartspaces.workbench.project.constituent.ContentProjectConstituent;
import io.smartspaces.workbench.project.constituent.ProjectConstituent;
import io.smartspaces.workbench.project.constituent.ProjectConstituentBuilder;
import io.smartspaces.workbench.project.constituent.ProjectConstituentBuilderFactory;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.File;
import java.util.Map;

/**
 * A file template specification.
 *
 * @author Trevor Pering
 */
public class TemplateFileConstituent extends BaseProjectConstituent {

  /**
   * Project type for a template file constituent.
   */
  public static final String TYPE_NAME = "templateFile";

  /**
   * THe number of evaluation passes used for writing project templates.
   */
  public static final int TEMPLATE_WRITE_EVALUATION_PASSES = 2;

  /**
   * The input path.
   */
  private String template;

  /**
   * Output path.
   */
  private String output;

  /**
   * Create am empty template instance.
   */
  public TemplateFileConstituent() {
  }

  /**
   * Create a new file template with supplied parameters.
   *
   * @param output
   *          output path
   * @param template
   *          input path
   */
  public TemplateFileConstituent(String output, String template) {
    this.output = output;
    this.template = template;
  }

  /**
   * @return file input template path
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Set the template source for this template.
   *
   * @param template
   *          template source specification
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @return output path
   */
  public String getOutput() {
    return output;
  }

  /**
   * Set the template output specification.
   *
   * @param output
   *          template output
   */
  public void setOutput(String output) {
    this.output = output;
  }

  @Override
  public void processConstituent(Project project, ProjectContext context) {
    ProjectCreationContext projectCreationContext = (ProjectCreationContext) context;
    FreemarkerTemplater templater = context.getWorkbenchTaskContext().getWorkbench().getTemplater();
    Map<String, Object> templateData = projectCreationContext.getTemplateData();

    String outPath = templater.processStringTemplate(templateData, getOutput());
    File outFile = fileSupport.newFile(outPath);
    if (!outFile.isAbsolute()) {
      String newBasePath = (String) templateData.get(BaseProjectTemplate.BASE_DIRECTORY_VARIABLE);
      outFile = fileSupport.newFile(fileSupport.newFile(newBasePath), outFile.getPath());
    }

    String inPath = templater.processStringTemplate(templateData, getTemplate());
    File inFile = fileSupport.newFile(inPath);
    if (!inFile.isAbsolute()) {
      inPath =
          fileSupport.newFile(projectCreationContext.getSpecificationBase(), inPath)
              .getAbsolutePath();
    }

    templater.writeTemplate(templateData, outFile, inPath, TEMPLATE_WRITE_EVALUATION_PASSES);
  }

  /**
   * Factory for the constituent components.
   */
  public static class TemplateFileConstituentFactory implements ProjectConstituentBuilderFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new TemplateFileConstituentBuilder();
    }
  }

  /**
   * Builder class for new templates..
   */
  private static class TemplateFileConstituentBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Namespace namespace, Element element,
        Project project) {
      if (!TYPE_NAME.equals(element.getName())) {
        throw new SimpleSmartSpacesException("Bad element name " + element.getName());
      }
      TemplateFileConstituent templateFileConstituent = new TemplateFileConstituent();

      String sourcePath =
          element.getAttributeValue(ContentProjectConstituent.SOURCE_FILE_ATTRIBUTE);
      if (sourcePath == null) {
        throw new SimpleSmartSpacesException("Template specification has no sourceFile");
      }
      templateFileConstituent.setTemplate(sourcePath);

      String destinationFile =
          element.getAttributeValue(ContentProjectConstituent.DESTINATION_FILE_ATTRIBUTE);
      if (destinationFile == null) {
        throw new SimpleSmartSpacesException("Template specification has no destinationFile");
      }
      templateFileConstituent.setOutput(destinationFile);
      return templateFileConstituent;
    }
  }
}
