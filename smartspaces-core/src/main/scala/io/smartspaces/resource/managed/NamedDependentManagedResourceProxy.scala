/*
 * Copyright (C) 2019 Keith M. Hughes
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

import java.lang.Iterable

import io.smartspaces.resource.{DependentResource, NamedResource}

/**
  * A managed resource that allows for adding a name and dependencies on an existing managed resource.
  *
  * @author Keith M. Hughes
  */
class NamedDependentManagedResourceProxy[T](
  managedResource: ManagedResource,
  name: String,
  dependencies: Iterable[T]
) extends ManagedResource with NamedResource with DependentResource[T] {
  override def startup(): Unit = {
    managedResource.startup()
  }

  def shutdown(): Unit = {
    managedResource.shutdown()
  }

  override def getName(): String = name

  override def getDependencies(): Iterable[T] = dependencies
}
