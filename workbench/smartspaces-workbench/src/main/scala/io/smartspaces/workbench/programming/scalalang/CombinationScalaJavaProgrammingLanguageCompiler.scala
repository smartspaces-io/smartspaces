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

package io.smartspaces.workbench.programming.scalalang

import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.workbench.programming.ProgrammingLanguageCompiler
import io.smartspaces.workbench.project.ProjectTaskContext

import com.google.common.collect.Lists

import java.io.File
import java.util.List

/**
 * The Compiler for Scala projects that runs both the Scala compiler and the Java compiler.
 *
 * @author Keith M. Hughes
 */
class CombinationScalaJavaProgrammingLanguageCompiler extends ProgrammingLanguageCompiler {

  /**
   * The Scala compiler.
   */
  val scalaCompiler: ProgrammingLanguageCompiler = new PureScalaProgrammingLanguageCompiler

  /**
   * The file support to be used.
   */
  val fileSupport: FileSupport = FileSupportImpl.INSTANCE

  override def compile(context: ProjectTaskContext, compilationBuildDirectory: File, classpath: List[File], compilationFiles: List[File],
    compilerOptions: List[String]): Unit = {
    scalaCompiler.compile(context, compilationBuildDirectory, classpath, compilationFiles,
      compilerOptions);

    // The Scala compiler looked at both Scala and Java sources. Keep only the
    // Java files.
    val javaSupport = context.getWorkbenchTaskContext.getWorkbench.getProgrammingLanguageRegistry.getProgrammingLanguageSupport("java")

    val javaSourceFiles: List[File] =
      fileSupport.filterFiles(compilationFiles, javaSupport.getSourceFileFilter());

    if (!javaSourceFiles.isEmpty()) {
      val javaClassPath: List[File] = Lists.newArrayList()

      // Classpath needs all compiled Scala classes if any
      if (compilationFiles.size() > javaSourceFiles.size()) {
        javaClassPath.add(compilationBuildDirectory)
      }
      javaClassPath.addAll(classpath)

      val javaCompilerOptions = javaSupport.getCompilerOptions(context)

      context.getLog()
        .info(String.format("Running the Java compiler with arguments %s", javaCompilerOptions));
      javaSupport.newCompiler().compile(context, compilationBuildDirectory, javaClassPath, javaSourceFiles,
        javaCompilerOptions);
    }
  }
}