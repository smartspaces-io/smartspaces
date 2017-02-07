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

package io.smartspaces.workbench.language;

import io.smartspaces.workbench.project.ProjectTaskContext;

import _root_.java.io.File;
import _root_.java.io.FileFilter;
import _root_.java.util.List;

/**
 * Useful global methods for programming language support.
 */
object ProgrammingLanguageSupport {

}

/**
 * Support for a programming language.
 *
 * @author Keith M. Hughes
 */
trait ProgrammingLanguageSupport {

  /**
   * Get the name of the language supported.
   *
   * @return the language name
   */
  def getLanguageName(): String

  /**
   * Get all compiler options to be used.
   *
   * @param context
   *            the build context
   *
   * @return the complete compiler options
   */
  def getCompilerOptions(context: ProjectTaskContext): List[String]

  /**
   * Get a list of files to compile.
   *
   * @param baseSourceDirectory
   *            the base directory to scan for sources from
   * @param files
   *            the list of files to add to
   */
  def getCompilationFiles(baseSourceDirectory: File, files: List[File]): Unit

  /**
   * Get the file filter for the language.
   *
   * @return  the file filter
   */
  def getSourceFileFilter(): FileFilter

  /**
   * Get a list of relative file paths that are non-source.
   *
   * @param baseSourceDirectory
   *            the base directory to scan for sources from
   *            
   * @return the list of files to add to
   */
  def getNonSourceFiles(baseSourceDirectory: File): List[String]

  /**
   * Get the file filter for non language files.
   *
   * @return  the file filter
   */
  def getNonSourceFileFilter(): FileFilter

  /**
   * Create a new compiler for the language.
   *
   * @return a new compiler
   */
  def newCompiler(): ProgrammingLanguageCompiler

  /**
   * Get the main directory where the language stores its source files.
   */
  def getMainSourceDirectory(): String

  /**
   * Get the main directory where the language stores its source test files.
   */
  def getTestSourceDirectory(): String

  /**
   * Get the main directory where the language stores its generated source files.
   */
  def getMainGeneratedSourceDirectory(): String

  /**
   * Get the main directory where the language stores its generated source test files.
   */
  def getTestGeneratedSourceDirectory(): String
}
