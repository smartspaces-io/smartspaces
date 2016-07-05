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

package io.smartspaces.workbench.language.scala

import io.smartspaces.workbench.language.ProgrammingLanguageSupport
import io.smartspaces.workbench.project.ProjectTaskContext
import java.util.ArrayList
import java.io.File
import java.io.FileFilter
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.util.io.FileSupport
import io.smartspaces.workbench.language.ProgrammingLanguageCompiler
import io.smartspaces.workbench.language.java.JavaProgrammingLanguageSupport
import io.smartspaces.workbench.project.ProjectFileLayout

/**
 * The programming language support for Scala projects.
 *
 * @author Keith M. Hughes
 */
object ScalaProgrammingLanguageSupport {

  /**
   * The name of the language being supported.
   */
  val LANGUAGE_NAME = "scala"

  /**
   * The main source directory for the language.
   */
  val MAIN_SOURCE_DIRECTORY = ProjectFileLayout.SOURCE_MAIN_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The main test source directory for the language.
   */
  val TEST_SOURCE_DIRECTORY = ProjectFileLayout.SOURCE_TEST_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The main generated source directory for the language.
   */
  val MAIN_GENERATED_SOURCE_DIRECTORY = ProjectFileLayout.SOURCE_GENERATED_MAIN_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The main test generated source directory for the language.
   */
  val TEST_GENERATED_SOURCE_DIRECTORY = ProjectFileLayout.SOURCE_GENERATED_TEST_PREFIX + "/" + LANGUAGE_NAME;

  /**
   * The file extension for a Java source file.
   */
  val FILE_EXTENSION_SCALA_SOURCE = ".scala";

  /**
   * A file filter for locating source files.
   */
  val SOURCE_FILE_FILTER = new FileFilter {
    override def accept(file: File): Boolean = {
      val fileName = file.getName
      (fileName.endsWith(FILE_EXTENSION_SCALA_SOURCE) || fileName.endsWith(JavaProgrammingLanguageSupport.FILE_EXTENSION_JAVA_SOURCE)) && !file.isHidden()
    }
  }

}

/**
 * The programming language support for Scala projects.
 *
 * @author Keith M. Hughes
 */

class ScalaProgrammingLanguageSupport extends ProgrammingLanguageSupport {

  /**
   * The file support to be used.
   */
  val fileSupport: FileSupport = FileSupportImpl.INSTANCE

  override def getLanguageName(): String = {
    ScalaProgrammingLanguageSupport.LANGUAGE_NAME
  }

  override def getCompilerOptions(context: ProjectTaskContext): java.util.List[String] = {
    new ArrayList[String]
  }

  override def getCompilationFiles(baseSourceDirectory: File, files: java.util.List[File]): Unit = {
    files.addAll(fileSupport.collectFiles(baseSourceDirectory, ScalaProgrammingLanguageSupport.SOURCE_FILE_FILTER, true))
  }

  override def getSourceFileFilter(): FileFilter = {
    ScalaProgrammingLanguageSupport.SOURCE_FILE_FILTER
  }

  override def newCompiler(): ProgrammingLanguageCompiler = {
    new PureScalaProgrammingLanguageCompiler
  }

  override def getMainSourceDirectory(): String = {
    ScalaProgrammingLanguageSupport.MAIN_SOURCE_DIRECTORY
  }

  override def getTestSourceDirectory(): String = {
    ScalaProgrammingLanguageSupport.TEST_SOURCE_DIRECTORY
  }

  override def getMainGeneratedSourceDirectory(): String = {
    ScalaProgrammingLanguageSupport.MAIN_GENERATED_SOURCE_DIRECTORY
  }

  override def getTestGeneratedSourceDirectory(): String = {
    ScalaProgrammingLanguageSupport.TEST_GENERATED_SOURCE_DIRECTORY
  }
}