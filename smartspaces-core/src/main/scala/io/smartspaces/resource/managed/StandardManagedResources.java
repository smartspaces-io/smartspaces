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

package io.smartspaces.resource.managed;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A standard implementation of {@link ManagedResources}.
 *
 * @author Keith M. Hughes
 */
public class StandardManagedResources implements ManagedResources {

  /**
   * The managed resources.
   */
  private final List<ManagedResource> resources = new ArrayList<>();

  /**
   * Logger for the managed resources.
   */
  private final ExtendedLog log;

  /**
   * {@code true} if the collection has been officially started.
   */
  private boolean started;

  /**
   * Construct a new managed resource collection.
   *
   * @param log
   *          the log for the collection
   */
  public StandardManagedResources(ExtendedLog log) {
    this.log = log;
  }

  @Override
  public void addResource(ManagedResource resource) {
    if (started) {
      try {
        // Will only add if starts up properly
        resource.startup();
      } catch (Throwable e) {
        throw new SmartSpacesException("Could not start up managed resource", e);
      }
    }
    
    addStartedResource(resource);
  }

  @Override
  public synchronized void addStartedResource(ManagedResource resource) {
    resources.add(resource);
  }

  @Override
  public synchronized List<ManagedResource> getResources() {
    return Collections.unmodifiableList(resources);
  }

  @Override
  public synchronized void clear() {
    resources.clear();
  }

  @Override
  public synchronized void startupResources() {
    List<ManagedResource> startedResources = new ArrayList<>();

    for (ManagedResource resource : resources) {
      try {
        resource.startup();

        startedResources.add(resource);
      } catch (Throwable e) {
        shutdownResources(startedResources);

        throw new SmartSpacesException("Could not start up all managed resources", e);
      }
    }

    started = true;
  }

  @Override
  public synchronized void shutdownResources() {
    shutdownResources(resources);
  }

  @Override
  public synchronized void shutdownResourcesAndClear() {
    shutdownResources();
    clear();
  }

  /**
   * Shut down the specified resources.
   *
   * @param resources
   *          some resources to shut down
   */
  private void shutdownResources(List<ManagedResource> resources) {
    for (ManagedResource resource : Lists.reverse(resources)) {
      try {
        resource.shutdown();
      } catch (Throwable e) {
        log.error("Could not shut down resource", e);
      }
    }
  }
}
