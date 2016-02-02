/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectContext;

import java.io.File;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.Maps;

/**
 * An assembly resource for a {@link io.smartspaces.workbench.project.Project}.
 *
 * <p>
 * The default constituent processing places the content in the build staging
 * directory.
 *
 * @author Trevor Pering
 */
public class AssemblyComponentProjectConstituent extends BaseContentProjectConstituent {

  /**
   * Create a new project assembly constituent from a string.
   *
   * @param input
   *          specification string
   *
   * @return parsed constituent
   */
  public static AssemblyComponentProjectConstituent fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > 2) {
      throw new SimpleSmartSpacesException("Extra parts when parsing assembly: " + input);
    }
    AssemblyComponentProjectConstituent constituent = new AssemblyComponentProjectConstituent();
    constituent.sourceFile = parts[0];
    constituent.destinationDirectory = parts.length > 1 ? parts[1] : null;
    return constituent;
  }

  /**
   * Project type for an assembly resource.
   */
  public static final String TYPE_NAME = "assembly";

  /**
   * Pack format attribute name.
   */
  public static final String PACK_FORMAT_ATTRIBUTE = "packFormat";

  /**
   * Pack format type for zip files.
   */
  public static final String ZIP_PACK_FORMAT = "zip";

  /**
   * A file to be copied.
   */
  private String sourceFile;

  /**
   * The directory to which contents will be copied.
   *
   * <p>
   * This directory will be relative to the project's installed folder.
   */
  private String destinationDirectory;

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    File baseDirectory = project.getBaseDirectory();
    File sourceZipFile = context.getProjectTargetFile(baseDirectory, sourceFile);

    File outputDirectory = context.getProjectTargetFile(stagingDirectory, destinationDirectory);
    context.getLog().info(
        String.format("Extracting assembly '%s' into '%s'...", sourceZipFile.getAbsoluteFile(),
            outputDirectory.getAbsoluteFile()));
    fileSupport.directoryExists(outputDirectory);
    fileSupport.unzip(sourceZipFile, outputDirectory, context.getResourceFileCollector());
  }

  @Override
  public Map<String, String> getAttributeMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put(PACK_FORMAT_ATTRIBUTE, ZIP_PACK_FORMAT);
    map.put(SOURCE_FILE_ATTRIBUTE, sourceFile);
    map.put(DESTINATION_DIRECTORY_ATTRIBUTE, destinationDirectory);
    return map;
  }

  /**
   * Factory for creating new assembly resources.
   */
  public static class ProjectAssemblyConstituentBuilderFactory implements
      ProjectConstituentBuilderFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new AssemblyProjectConstituentBuilder();
    }
  }

  /**
   * Set the assembly source file.
   *
   * @param sourceFile
   *          assembly source path
   */
  public void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  /**
   * Set the destination directory for assembly expansion.
   *
   * @param destinationDirectory
   *          directory to receive contents
   */
  public void setDestinationDirectory(String destinationDirectory) {
    this.destinationDirectory = destinationDirectory;
  }

  /**
   * Builder class for creating new assembly resources.
   */
  private static class AssemblyProjectConstituentBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Namespace namespace,
        Element resourceElement, Project project) {
      String packFormat = resourceElement.getAttributeValue(PACK_FORMAT_ATTRIBUTE);
      if (!ZIP_PACK_FORMAT.equals(packFormat)) {
        addError(String.format("Pack format '%s' not supported (currently must be '%s')",
            packFormat, ZIP_PACK_FORMAT));
      }
      String sourceFile = resourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
      String destinationDirectory =
          resourceElement.getAttributeValue(DESTINATION_DIRECTORY_ATTRIBUTE);

      if (destinationDirectory == null) {
        destinationDirectory = ".";
      }

      if (sourceFile == null) {
        addError("Assembly has no source");
      }

      if (hasErrors()) {
        return null;
      } else {
        AssemblyComponentProjectConstituent assembly = new AssemblyComponentProjectConstituent();

        assembly.setDestinationDirectory(destinationDirectory);
        assembly.setSourceFile(sourceFile);

        return assembly;
      }
    }
  }
}
