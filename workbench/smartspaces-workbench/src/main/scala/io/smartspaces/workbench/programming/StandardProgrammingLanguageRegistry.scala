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

package io.smartspaces.workbench.programming;

import _root_.scala.collection.mutable._

/**
 * A registry for programming language support.
 *
 * @author Keith M. Hughes
 */
class StandardProgrammingLanguageRegistry extends ProgrammingLanguageRegistry {

  private val supports: Map[String, ProgrammingLanguageSupport] = new HashMap[String, ProgrammingLanguageSupport]
  
  override def registerProgrammingLanguageSupport(support: ProgrammingLanguageSupport): ProgrammingLanguageRegistry = {
    supports += (support.getLanguageName() -> support)
    
    this
  }

  override def getProgrammingLanguageSupport(languageName: String): ProgrammingLanguageSupport = {
    supports.getOrElse(languageName, null)
  }
}
