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

import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.resource.managed.ManagedResources
import io.smartspaces.tasks.ManagedTasks

/**
 * A managed scope that controls resources and tasks as a unit.
 *
 * <p>
 * Managed resources will be started when the scope is started. Both resources and tasks will be shut down
 * when the scope is shut down.
 *
 * @author Keith M. Hughes
 */
trait ManagedScope extends ManagedResource with ManagedTasks {

  /**
   * The collection of managed tasks for the scope.
   */
  val managedTasks: ManagedTasks

  /**
   * The collection of managed resources for the scope.
   */
  val managedResources: ManagedResources
  
  /**
   * Add a new resource to the  managed resources collection.
   * 
   * <p>
   * If {@link #startupResources()} has already been called, the resource
   * will be immediately started and will only be added to the collection if it starts
   * properly.
   *
   * @param resource
   *          the resource to add
   */
  def addResource(resource: ManagedResource): Unit

  /**
   * Add a resource to the managed resources collection that has already been started.
   *
   * @param resource
   *          the resource to add
   */
  def addStartedResource(resource: ManagedResource): Unit
}
