/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.evaluation

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.scope.ManagedScope
import io.smartspaces.system.SmartSpacesEnvironment

/**
 * A factory for execution contexts.
 *
 * @author Keith M. Hughes
 */
trait ExecutionContextFactory {

  /**
   * Create a new context.
   *
   * @return the new context
   */
  def newContext(): ExecutionContext
}

/**
 * An execution context factory that uses the same managed scope, space environment, and log for
 * each context.
 *
 * @author Keith M. Hughes
 */
class FixedComponentsExecutionContextFactory(
  managedScope: ManagedScope,
  spaceEnvironment: SmartSpacesEnvironment,
  log: ExtendedLog) extends ExecutionContextFactory {

  override def newContext(): ExecutionContext = {
    new StandardExecutionContext(managedScope, spaceEnvironment, log)
  }
}
