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

package io.smartspaces.workbench.programming

import io.smartspaces.workbench.project.ProjectTaskContext

import _root_.java.io.File
import _root_.java.util.List

/**
 * A Java compiler for Smart Spaces projects.
 *
 * @author Keith M. Hughes
 */
trait ProgrammingLanguageCompiler {

  /**
   * Compile a set of Java files.
   *
   * @param context
   *          the context for the compile task
   * @param compilationBuildDirectory
   *          the build folder for compilation artifacts
   * @param classpath
   *          the class path for the compilation
   * @param compilationFiles
   *          the compilation files for the compile
   * @param compilerOptions
   *          any options for the compiler
   */
  def compile(context: ProjectTaskContext, compilationBuildDirectory: File, classpath: List[File],
    compilationFiles: List[File], compilerOptions: List[String]): Unit
}
