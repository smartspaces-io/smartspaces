/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.util.resource;

import java.util.List;

/**
 * A collection of {@link ManagedResource} instances.
 *
 * <p>
 * The collection will start up and shut down the resources when it is started
 * up and shut down. Do not worry about these lifecycle events.
 *
 * @author Keith M. Hughes
 */
public interface ManagedResources {

  /**
   * Add a new resource to the collection.
   *
   * @param resource
   *          the resource to add
   */
  void addResource(ManagedResource resource);

  /**
   * Get a list of the currently managed resources.
   *
   * @return list of managed resources
   */
  List<ManagedResource> getResources();

  /**
   * Clear all resources from the collection.
   *
   * <p>
   * The collection is cleared. No lifecycle methods are called on the
   * resources.
   */
  void clear();

  /**
   * Attempt to startup all resources in the manager.
   *
   * <p>
   * If all resources don't start up, all resources that were started will be
   * shut down.
   *
   * <p>
   * Do not call {@link #shutdownResources()} or
   * {@link #shutdownResourcesAndClear()} if an exception is thrown out of this
   * method.
   */
  void startupResources();

  /**
   * Shut down all resources.
   *
   * <p>
   * This will make a best attempt. A shutdown will be attempted on all
   * resources, even if some throw an exception.
   */
  void shutdownResources();

  /**
   * Shut down all resources and clear from the collection.
   *
   * <p>
   * This will make a best attempt. A shutdown will be attempted on all
   * resources, even if some throw an exception.
   */
  void shutdownResourcesAndClear();
}
