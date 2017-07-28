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

package io.smartspaces.workbench.project.constituent;

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.workbench.project.Project
import io.smartspaces.workbench.project.ProjectContext

import org.jdom2.Element
import org.jdom2.Namespace

/**
 * Interface for project constituents.
 *
 * @author Trevor Pering
 */
trait ProjectConstituent {

  /**
   * Process the needed constituent for the project.
   *
   * @param project
   *          the project being built
   * @param context
   *          project context in which to process the constituent
   */
  def processConstituent(project: Project, context: ProjectContext): Unit
}

/**
 * Factory for project constituent builders.
 *
 * @author Keith M. Hughes
 */
trait ProjectConstituentBuilderFactory {

  /**
   * Get the type name of this project constituent.
   *
   * @return constituent type name
   */
  def getName(): String

  /**
   * Create a new builder.
   *
   * @return the new builder
   */
  def newBuilder(): ProjectConstituentBuilder
}

/**
 * Builder interface for creating new constituent instances.
 */
trait ProjectConstituentBuilder {

  /**
   * Get a new constituent of the appropriate type.
   *
   * @param namespace
   *          XML namespace for all elements
   * @param constituentElement
   *          project file definition element
   * @param project
   *          the project description being built
   *
   * @return new project object or {@code null} if there were errors
   */
  def buildConstituentFromElement(namespace: Namespace, constituentElement: Element,
    project: Project): ProjectConstituent

  /**
   * Were there errors?
   *
   * @return {@code true} if there were errors
   */
  def hasErrors(): Boolean

  /**
   * Set the logging provider for use by the builder.
   *
   * @param log
   *          logging provider to use
   */
  def setLog(log: ExtendedLog)
}

