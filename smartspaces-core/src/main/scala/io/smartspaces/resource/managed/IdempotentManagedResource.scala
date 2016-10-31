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

package io.smartspaces.resource.managed

/**
 * A managed resource that can have startup and shutdown called multiple times.
 * 
 * <p>
 * This class requiries overriding {@link #onStartup()} and {@link #onShutdown()}.
 *
 * @author Keith M. Hughes
 */
trait IdempotentManagedResource extends ManagedResource {

  /**
   * The state of the resource.
   */
  protected var resourceState = ManagedResourceState.SHUTDOWN

  override def startup(): Unit = {
    this.synchronized {
      if (resourceState == ManagedResourceState.SHUTDOWN) {
        onStartup()
        
        resourceState = ManagedResourceState.STARTED
      }
    }
  }

  /**
   * Perform the actual startup.
   */
  def onStartup(): Unit = {
    // Default is do nothing
  }

  override def shutdown(): Unit = {
   this.synchronized {
      if (resourceState == ManagedResourceState.STARTED) {
        onShutdown()
        
        resourceState = ManagedResourceState.SHUTDOWN
      }
    }
  }

  /**
   * Perform the actual shutdown.
   */
  def onShutdown(): Unit = {
    // Default is do nothing
  }
}

/**
 * The state of the managed resource.
 * 
 * @author Keith M. Hughes
 */
object ManagedResourceState extends Enumeration {
  type ManagedResourceState = Value

  val STARTED = Value
  val SHUTDOWN = Value
}
