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
import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.tasks.ManagedTask
import java.util.concurrent.TimeUnit
import io.smartspaces.time.TimeFrequency
import io.smartspaces.time.TimeDelay

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

  override def addResource(resource: ManagedResource): Unit = {   
    managedResources.addResource(resource)
  }

  override def addStartedResource(resource: ManagedResource): Unit = {
    managedResources.addResource(resource)
  }
  
  override def submit(task: Runnable): ManagedTask = {
    managedTasks.submit(task)
  }

  override def schedule(task: Runnable, delay: Long, unit: TimeUnit): ManagedTask = {
    managedTasks.schedule(task, delay, unit)
  }

  override def scheduleAtFixedRate(task: Runnable,  taskFrequency: TimeFrequency): ManagedTask = {
    managedTasks.scheduleAtFixedRate(task,  taskFrequency)
  }

  override def scheduleAtFixedRate(task: Runnable, taskFrequency: TimeFrequency,
      allowTerminate: Boolean): ManagedTask = {
    managedTasks.scheduleAtFixedRate(task, taskFrequency, allowTerminate)
  }

  override def scheduleAtFixedRate(task: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ManagedTask = {
    managedTasks.scheduleAtFixedRate(task, initialDelay, period, unit)
  }

  override def scheduleAtFixedRate(task: Runnable, initialDelay: Long, period: Long,
      unit: TimeUnit, allowTerminate: Boolean): ManagedTask = {
    managedTasks.scheduleAtFixedRate(task, initialDelay, period, unit , allowTerminate)
  }

  override def scheduleWithFixedDelay(task: Runnable, taskDelay: TimeDelay): ManagedTask = {
    managedTasks.scheduleWithFixedDelay(task, taskDelay)
  }

  override def scheduleWithFixedDelay(task: Runnable, taskDelay: TimeDelay,
     allowTerminate: Boolean): ManagedTask = {
    managedTasks.scheduleWithFixedDelay(task, taskDelay, allowTerminate)
  }

  override def scheduleWithFixedDelay(task: Runnable, initialDelay: Long, delay: Long,
      unit: TimeUnit): ManagedTask = {
    managedTasks.scheduleWithFixedDelay(task, initialDelay, delay, unit)
  }

  override def scheduleWithFixedDelay(task: Runnable, initialDelay: Long, delay: Long,
      unit: TimeUnit, allowTerminate: Boolean): ManagedTask = {
    managedTasks.scheduleWithFixedDelay(task, initialDelay, delay, unit, allowTerminate)
  }
}