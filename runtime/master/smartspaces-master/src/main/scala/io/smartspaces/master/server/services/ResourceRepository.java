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

package io.smartspaces.master.server.services;

import io.smartspaces.domain.basic.Resource;
import io.smartspaces.expression.FilterExpression;

import java.util.List;

/**
 * A repository for resource domain objects.
 *
 * @author Keith M. Hughes
 */
public interface ResourceRepository {

  /**
   * Create a new resource.
   *
   * @return the new resource instance, it will not be saved in the repository
   */
  Resource newResource();

  /**
   * Get the number of resources in the repository.
   *
   * @return the number of resources in the repository
   */
  long getNumberResources();

  /**
   * Get all resources in the repository.
   *
   * @return all resources in the repository
   */
  List<Resource> getAllResources();

  /**
   * Get all resources in the repository that match the filter.
   *
   * @param filter
   *          the filter
   *
   * @return all resources in the repository matching the filter
   */
  List<Resource> getResources(FilterExpression filter);

  /**
   * Get a resource by its ID.
   *
   * @param id
   *          the ID of the desired resource
   *
   * @return the resource with the given ID or {@code null} if no such resource
   */
  Resource getResourceById(String id);

  /**
   * Get a resource by its identifying name and version.
   *
   * @param identifyingName
   *          the identifying name of the desired resource
   * @param version
   *          the version of the desired resource
   *
   * @return the resource with the given name and version or {@code null} if no
   *         such resource
   */
  Resource getResourceByNameAndVersion(String identifyingName, String version);

  /**
   * Save a resource in the repository.
   *
   * <p>
   * Is used both to save a new resource into the repository for the first time
   * or to update edits to the resource.
   *
   * @param resource
   *          the resource to save
   *
   * @return the persisted resource, use this one going forward
   */
  Resource saveResource(Resource resource);

  /**
   * Delete a resource in the repository.
   *
   * @param resource
   *          the resource to delete
   */
  void deleteResource(Resource resource);
}
