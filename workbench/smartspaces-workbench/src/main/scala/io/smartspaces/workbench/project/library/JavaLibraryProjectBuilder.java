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

package io.smartspaces.workbench.project.library;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.builder.BaseProjectBuilder;
import io.smartspaces.workbench.project.java.JvmJarAssembler;
import io.smartspaces.workbench.project.java.ProgrammingLanguageCompiler;
import io.smartspaces.workbench.project.java.StandardJvmJarAssembler;
import io.smartspaces.workbench.project.test.IsolatedClassloaderJavaTestRunner;
import io.smartspaces.workbench.project.test.JavaTestRunner;

import java.io.File;

/**
 * A Java library project builder.
 *
 * @author Keith M. Hughes
 */
public class JavaLibraryProjectBuilder extends BaseProjectBuilder<LibraryProject> {

  /**
   * File extension to give the build artifact.
   */
  private static final String JAR_FILE_EXTENSION = "jar";

  /**
   * The compiler for Java JARs.
   */
  private final JvmJarAssembler jarAssembler = new StandardJvmJarAssembler();

  /**
   * File support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void build(LibraryProject project, ProjectTaskContext context)
      throws SmartSpacesException {
    File buildDirectory = context.getBuildDirectory();
    File compilationFolder = getOutputDirectory(buildDirectory);
    
    File jarDestinationFile = getBuildDestinationFile(project, buildDirectory, JAR_FILE_EXTENSION);

    // The resources go to the compilation folder. They will then be in the
    // right place for creating the JAR file.
    context.processGeneratedResources(compilationFolder);
    context.processResources(compilationFolder);

    jarAssembler.buildJar(jarDestinationFile, compilationFolder, null, project.getContainerInfo(),
        context);
    runTests(jarDestinationFile, context);
    context.addGeneratedArtifact(jarDestinationFile);
  }

  /**
   * Run any tests for the project.
   *
   * @param jarDestinationFile
   *          the destination file for the built project
   * @param context
   *          the project build context
   *
   * @throws SmartSpacesException
   *           the tests failed
   */
  private void runTests(File jarDestinationFile, ProjectTaskContext context)
      throws SmartSpacesException {
    JavaTestRunner runner = new IsolatedClassloaderJavaTestRunner();

    runner.runTests(jarDestinationFile, null, context);
  }

  /**
   * Create the output directory for the library compilation.
   *
   * @param buildDirectory
   *          the root of the build folder
   *
   * @return the output directory for building
   */
  private File getOutputDirectory(File buildDirectory) {
    File outputDirectory =
        fileSupport.newFile(buildDirectory, ProgrammingLanguageCompiler.BUILD_DIRECTORY_CLASSES_MAIN);
    fileSupport.directoryExists(outputDirectory);

    return outputDirectory;
  }
}
