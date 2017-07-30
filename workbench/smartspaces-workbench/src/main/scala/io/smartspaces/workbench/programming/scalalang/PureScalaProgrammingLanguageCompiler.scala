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

import io.smartspaces.SmartSpacesException
import io.smartspaces.workbench.programming.ProgrammingLanguageCompiler
import io.smartspaces.workbench.project.ProjectTaskContext

import java.io.File

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.tools.nsc.Global
import scala.tools.nsc.Settings

/**
 * The Compiler for Scala projects that only runs the Scala compiler.
 *
 * @author Keith M. Hughes
 */
class PureScalaProgrammingLanguageCompiler extends ProgrammingLanguageCompiler {

  override def compile(context: ProjectTaskContext, compilationBuildDirectory: File, classpath: java.util.List[File], compilationFiles: java.util.List[File],
    compilerOptions: java.util.List[String]): Unit = {

    def errorDisplay(s: String): Unit = {
      context.getLog().formatError("Error during scala compile: %s", s)
    }
    val settings = new Settings(errorDisplay)

    // Create a classpath with colon separated strings of file paths
    val classPathList = classpath.asScala.map(_.getAbsolutePath).mkString(":")

    val sourceFiles = compilationFiles.asScala.map(_.getAbsolutePath).toList

    val args = List("-classpath", classPathList, "-d", compilationBuildDirectory.getAbsolutePath)
    val (_, nonSettingsArgs) = settings.processArguments(args, true)
    if (nonSettingsArgs.nonEmpty) {
      throw new SmartSpacesException("Unknown args in Scala compiler settings")
    }

    val compiler = new Global(settings)
    val run = new compiler.Run
    run compile sourceFiles
  }
}