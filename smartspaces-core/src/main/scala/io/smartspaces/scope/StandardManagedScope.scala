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

package io.smartspaces.scope

import io.smartspaces.resource.managed.ManagedResources
import io.smartspaces.tasks.InternalManagedTasks
import io.smartspaces.tasks.ManagedTasks

/**
 * The standard managed scope object.
 *
 * @author Keith M. Hughes
 */
class StandardManagedScope(override val managedResources: ManagedResources, override val managedTasks: ManagedTasks) extends ManagedScope {

  override def startup(): Unit = {
    managedResources.startupResources()
  }

  override def shutdown(): Unit = {
    managedTasks.asInstanceOf[InternalManagedTasks].shutdownAll()
    
    managedResources.shutdownResourcesAndClear()
  }
}