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

package io.smartspaces.workbench.programming.javalang;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.programming.ProgrammingLanguageCompiler;
import io.smartspaces.workbench.project.ProjectTaskContext;

import com.google.common.collect.Lists;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

/**
 * A project java compiler which uses the Eclipse compiler.
 *
 * @author Keith M. Hughes
 */
public class EclipseJavaProgrammingLanguageCompiler implements ProgrammingLanguageCompiler {

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void compile(ProjectTaskContext context, File compilationBuildDirectory,
      List<File> classpath, List<File> compilationFiles, List<String> compilerOptions) {

    StandardJavaFileManager fileManager = null;
    try {
      JavaCompiler compiler = new EclipseCompiler();

      fileManager = compiler.getStandardFileManager(null, null, null);
      fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
          Lists.newArrayList(compilationBuildDirectory));

      Iterable<? extends JavaFileObject> compilationUnits1 =
          fileManager.getJavaFileObjectsFromFiles(compilationFiles);

      Boolean success = compiler
          .getTask(null, fileManager, null, compilerOptions, null, compilationUnits1).call();

      if (!success) {
        throw new SimpleSmartSpacesException("The Java compilation failed");
      }
    } catch (IOException e) {
      throw new SmartSpacesException("Error while compiling Java files", e);
    } finally {
      fileSupport.close(fileManager, false);
    }
  }
}
