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

/**
 * A registry for programming language support.
 *
 * @author Keith M. Hughes
 */
trait ProgrammingLanguageRegistry {

  /**
   * Register a language support for a given language.
   *
   * @param support
   *          the support to register
   *
   * @return this registry
   */
  def registerProgrammingLanguageSupport(support: ProgrammingLanguageSupport): ProgrammingLanguageRegistry

  /**
   * Get the language support for a given language.
   *
   * @param languageName
   *          the name of the language
   *
   * @return the language support or {@code null} if none with the given
   *         language name
   */
  def getProgrammingLanguageSupport(languageName: String): ProgrammingLanguageSupport
}
