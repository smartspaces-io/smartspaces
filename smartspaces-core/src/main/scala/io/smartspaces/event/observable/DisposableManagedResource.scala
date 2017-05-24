/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.event.observable;

import io.smartspaces.resource.managed.BaseManagedResource
import io.reactivex.disposables.Disposable

/**
 * A managed resource that will shut down a reactive disposable.
 * 
 * @author Keith M. Hughes
 */
class DisposableManagedResource(private val disposable: Disposable) extends BaseManagedResource {
  override def shutdown(): Unit = {
    disposable.dispose()
  }
}