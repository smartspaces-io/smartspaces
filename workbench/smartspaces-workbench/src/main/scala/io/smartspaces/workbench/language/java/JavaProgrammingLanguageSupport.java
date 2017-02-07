/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.workbench.language.java;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.language.ProgrammingLanguageCompiler;
import io.smartspaces.workbench.language.ProgrammingLanguageSupport;
import io.smartspaces.workbench.project.ProjectFileLayout;
import io.smartspaces.workbench.project.ProjectTaskContext;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Support for the Java programming language.
 * 
 * @author Keith M. Hughes
 */
public class JavaProgrammingLanguageSupport implements ProgrammingLanguageSupport {

  /**
   * The name of the language.
   */
  public static final String LANGUAGE_NAME = "java";

  /**
   * The main source directory for the language.
   */
  public static final String MAIN_SOURCE_DIRECTORY =
      ProjectFileLayout.SOURCE_MAIN_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The test source directory for the language.
   */
  public static final String TEST_SOURCE_DIRECTORY =
      ProjectFileLayout.SOURCE_TEST_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The main generated source directory for the language.
   */
  public static final String MAIN_GENERATED_SOURCE_DIRECTORY =
      ProjectFileLayout.SOURCE_GENERATED_MAIN_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The test generated source directory for the language.
   */
  public static final String TEST_GENERATED_SOURCE_DIRECTORY =
      ProjectFileLayout.SOURCE_GENERATED_TEST_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The default version of Java that items are compiled for.
   */
  public static final String JAVA_VERSION_DEFAULT = "1.7";

  /**
   * Configuration property for adding options to the JavaC compiler.
   */
  public static final String CONFIGURATION_NAME_BUILDER_JAVA_COMPILEFLAGS =
      "smartspaces.workbench.builder.java.compileflags";

  /**
   * Configuration property for adding options to the JavaC compiler.
   */
  public static final String CONFIGURATION_NAME_BUILDER_JAVA_VERSION =
      "smartspaces.workbench.builder.java.version";

  /**
   * The file extension for a Java source file.
   */
  public static final String FILE_EXTENSION_JAVA_SOURCE = ".java";

  /**
   * The Java compiler flag that specifies which version of the Java runtime the
   * compiler should target.
   */
  private static final String JAVA_COMPILER_FLAG_TARGET_VERSION = "-target";

  /**
   * The Java compiler flag that specifies which version of Java source
   * compatibility the compiler should insist on.
   */
  private static final String JAVA_COMPILER_FLAG_SOURCE_VERSION = "-source";

  /**
   * The file filter for Java source files.
   */
  private static final FileFilter SOURCE_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return file.getName().endsWith(FILE_EXTENSION_JAVA_SOURCE) && !file.isHidden();
    }
  };

  /**
   * The file filter for non-Java source files.
   */
  private static final FileFilter NONSOURCE_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return !file.getName().endsWith(FILE_EXTENSION_JAVA_SOURCE) && !file.isHidden();
    }
  };

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public String getLanguageName() {
    return LANGUAGE_NAME;
  }

  @Override
  public List<String> getCompilerOptions(ProjectTaskContext context) {
    List<String> options = new ArrayList<>();

    Configuration config = context.getProject().getConfiguration();

    String javaVersion =
        config.getPropertyString(CONFIGURATION_NAME_BUILDER_JAVA_VERSION, JAVA_VERSION_DEFAULT).trim();
    options.add(JAVA_COMPILER_FLAG_SOURCE_VERSION);
    options.add(javaVersion);
    options.add(JAVA_COMPILER_FLAG_TARGET_VERSION);
    options.add(javaVersion);

    String extraOptions = config.getPropertyString(CONFIGURATION_NAME_BUILDER_JAVA_COMPILEFLAGS);
    if (extraOptions != null) {
      String[] optionComponents = extraOptions.trim().split("\\s+");
      for (String optionComponent : optionComponents) {
        options.add(optionComponent);
      }
    }

    return options;
  }

  @Override
  public void getCompilationFiles(File baseSourceDirectory, List<File> files) {
    files.addAll(fileSupport.collectFiles(baseSourceDirectory, SOURCE_FILE_FILTER, true));
  }

  @Override
  public FileFilter getSourceFileFilter() {
    return SOURCE_FILE_FILTER;
  }

  @Override 
  public List<String> getNonSourceFiles(File baseSourceDirectory) {
    return fileSupport.collectRelativeFilePaths(baseSourceDirectory, NONSOURCE_FILE_FILTER, true);
  }

  @Override 
  public FileFilter getNonSourceFileFilter() {
    return NONSOURCE_FILE_FILTER;
  }
  
  @Override
  public ProgrammingLanguageCompiler newCompiler() {
    return new EclipseJavaProgrammingLanguageCompiler();
  }

  @Override
  public String getMainSourceDirectory() {
    return MAIN_SOURCE_DIRECTORY;
  }

  @Override
  public String getTestSourceDirectory() {
    return TEST_SOURCE_DIRECTORY;
  }

  @Override
  public String getMainGeneratedSourceDirectory() {
    return MAIN_GENERATED_SOURCE_DIRECTORY;
  }

  @Override
  public String getTestGeneratedSourceDirectory() {
    return TEST_GENERATED_SOURCE_DIRECTORY;
  }
}
