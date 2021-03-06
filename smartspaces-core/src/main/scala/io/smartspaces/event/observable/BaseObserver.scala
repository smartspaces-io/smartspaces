/**
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

package io.smartspaces.event.observable

import io.reactivex.observers.DefaultObserver
import io.reactivex.disposables.Disposable
import io.smartspaces.resource.managed.BaseManagedResource
import io.smartspaces.resource.managed.ManagedResource

/**
 * An implementation of a reactive Observer that supplies default methods for all callbacks.
 * 
 * <p>
 * This observer is a ManagedResource, and will cancel its description when shut down.
 */
class BaseObserver[T] extends DefaultObserver[T] with ManagedResource {
  
  override def startup(): Unit = {}
  
  override def shutdown(): Unit = {
    cancel()
  }

  override def onNext(value: T): Unit = {
    // Default is do nothing
  }

  override def onError(e: Throwable): Unit = {
    // Default is do nothing
  }

  override def onComplete(): Unit = {
    // Default is do nothing
  }
}