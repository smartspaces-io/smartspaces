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

package io.smartspaces.resource.repository;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.resource.SimpleNamedVersionedResource;
import io.smartspaces.resource.NamedVersionedResourceCollection;
import io.smartspaces.resource.NamedVersionedResourceWithData;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.managed.ManagedResource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * A storage manager for the Smart Spaces activity repository.
 *
 * @author Keith M. Hughes
 */
public interface ResourceRepositoryStorageManager extends ManagedResource {

  /**
   * Get the name the resource has in the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return the fully qualified name of the resource
   */
  String getRepositoryResourceName(ResourceCategory category, String name, Version version);

  /**
   * Does the repository contain a resource?
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource to be checked
   * @param version
   *          the version of the resource to be checked
   *
   * @return {@code true} if the repository contains the resource
   */
  boolean containsResource(ResourceCategory category, String name, Version version);

  /**
   * Stage a resource.
   *
   * <p>
   * The resource stream is closed.
   * 
   * @param resourceStream
   *          a stream of the incoming resource
   *
   * @return an opaque handle on the resource, do not make any assumptions on
   *         this handle, it can change
   */
  String stageResource(InputStream resourceStream);

  /**
   * Remove a staged activity from the manager.
   *
   * @param stageHandle
   *          The handle which was returned by
   *          {@link #stageResource(InputStream)}
   */
  void removeStagedReource(String stageHandle);

  /**
   * Get an {@link InputStream} for the description file in the staged activity.
   *
   * @param descriptorFileName
   *          name of the descriptor file
   * @param stageHandle
   *          the handle which was returned by
   *          {@link #stageResource(InputStream)}
   *
   * @return the input stream for the description file for the requested staged
   *         activity
   *
   * @throws SmartSpacesException
   *           if the stage handle is invalid or the activity contains no
   *           description file
   */
  InputStream getStagedResourceDescription(String descriptorFileName, String stageHandle)
      throws SmartSpacesException;
  
  /**
   * Get the name and version of the staged file.
   *
   * @param stageHandle
   *          the handle which was returned by
   *          {@link #stageResource(InputStream)}
   *
   * @return the name and version of the resource
   *
   * @throws SmartSpacesException
   *           if the stage handle is invalid or the activity contains no
   *           description file
   */
  SimpleNamedVersionedResource getNameVersionResource(String stageHandle) throws SmartSpacesException;

  /**
   * Get an {@link InputStream} for the entire staged bundle.
   *
   * @param stageHandle
   *          staging handle for which to get the data stream
   *
   * @return the input stream for the staged bundle
   *
   * @throws SmartSpacesException
   *           if the bundle can not be accessed, or the handle is invalid
   */
  InputStream getStagedResourceStream(String stageHandle) throws SmartSpacesException;

  /**
   * Commit a staged resource to the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   * @param stageHandle
   *          the staging handle for the resource
   */
  void commitResource(ResourceCategory category, String name, Version version, String stageHandle);

  /**
   * Get a stream for a given resource.
   *
   * <p>
   * Closing the stream is the responsibility of the caller.
   *
   * @param category
   *          the category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return the input stream for the resource, or {@code null} if no such
   *         resource
   */
  InputStream getResourceStream(ResourceCategory category, String name, Version version);

  /**
   * Create an output stream for writing a new resource into the repository.
   *
   * @param category
   *          category of the resource
   * @param name
   *          the name of the resource
   * @param version
   *          the version of the resource
   *
   * @return stream to use for writing the resource
   */
  OutputStream newResourceOutputStream(ResourceCategory category, String name, Version version);

  /**
   * Get all the resources available in a give category.
   *
   * @param category
   *          the category
   *
   * @return all resources in the given category with a URI for the resource
   */
  NamedVersionedResourceCollection<NamedVersionedResourceWithData<URI>> getAllResources(
      ResourceCategory category);

  /**
   * Get the base location for files from a give category.
   *
   * @param category
   *          the category
   *
   * @return the base location
   */
  File getBaseLocation(ResourceCategory category);
}
