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

package io.smartspaces.resource.repository.internal;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.basic.Resource;
import io.smartspaces.domain.basic.pojo.SimpleActivity;
import io.smartspaces.domain.support.ActivityDescription;
import io.smartspaces.domain.support.ActivityDescriptionReader;
import io.smartspaces.domain.support.ActivityUtils;
import io.smartspaces.domain.support.JdomActivityDescriptionReader;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.ResourceRepository;
import io.smartspaces.resource.SimpleNamedVersionedResource;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.repository.ResourceCategory;
import io.smartspaces.resource.repository.ResourceRepositoryManager;
import io.smartspaces.resource.repository.ResourceRepositoryStorageManager;
import io.smartspaces.util.data.resource.MessageDigestResourceSignatureCalculator;
import io.smartspaces.util.data.resource.ResourceSignatureCalculator;

import com.google.common.io.Closeables;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The standard implementation of the {@link ResourceRepositoryManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardResourceRepositoryManager implements ResourceRepositoryManager {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for resources.
   */
  private ResourceRepository resourceRepository;

  /**
   * Storage manager for the repository.
   */
  private ResourceRepositoryStorageManager repositoryStorageManager;

  /**
   * The calculator for resource signatures.
   */
  private ResourceSignatureCalculator resourceSignatureCalculator =
      new MessageDigestResourceSignatureCalculator();

  @Override
  public Resource addBundleResource(InputStream resourceStream) {
    String stageHandle = repositoryStorageManager.stageResource(resourceStream);
    try {
      SimpleNamedVersionedResource resource =
          repositoryStorageManager.getNameVersionResource(stageHandle);

      // TODO(keith): Might want to edit what it gives to the import so
      // this may need to move.
      Resource finalResource = resourceRepository.getResourceByNameAndVersion(resource.getName(),
          resource.getVersion().toString());
      if (finalResource == null) {
        finalResource = resourceRepository.newResource();
        finalResource.setIdentifyingName(resource.getName());
        finalResource.setVersion(resource.getVersion().toString());
      }

      // TODO(peringknife): Use appropriate TimeProvider for this.
      finalResource.setLastUploadDate(new Date());

      finalResource.setBundleContentHash(calculateBundleContentHash(stageHandle));

      resourceRepository.saveResource(finalResource);

      repositoryStorageManager.commitResource(ResourceCategory.RESOURCE_CATEGORY_CONTAINER_BUNDLE,
          resource.getName(), resource.getVersion(), stageHandle);

      return finalResource;
    } finally {
      repositoryStorageManager.removeStagedReource(stageHandle);
    }
  }

  @Override
  public Activity addActivity(InputStream activityStream) {
    String stageHandle = repositoryStorageManager.stageResource(activityStream);
    InputStream activityDescriptionStream = null;
    try {
      activityDescriptionStream =
          repositoryStorageManager.getStagedResourceDescription("activity.xml", stageHandle);
      ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
      ActivityDescription activityDescription = reader.readDescription(activityDescriptionStream);

      String identifyingName = activityDescription.getIdentifyingName();
      Version version = Version.parseVersion(activityDescription.getVersion());

      // TODO(keith): Might want to edit what it gives to the import so
      // this may need to move.
      Activity finalActivity = activityRepository.getActivityByNameAndVersion(identifyingName,
          activityDescription.getVersion());
      if (finalActivity == null) {
        finalActivity = activityRepository.newActivity();
        finalActivity.setIdentifyingName(identifyingName);
        finalActivity.setVersion(activityDescription.getVersion());
      }

      ActivityUtils.copy(activityDescription, finalActivity);

      // TODO(peringknife): Use appropriate TimeProvider for this.
      finalActivity.setLastUploadDate(new Date());

      copyDependencies(activityDescription, finalActivity);

      finalActivity.setBundleContentHash(calculateBundleContentHash(stageHandle));

      activityRepository.saveActivity(finalActivity);

      repositoryStorageManager.commitResource(ResourceCategory.RESOURCE_CATEGORY_ACTIVITY,
          identifyingName, version, stageHandle);

      return finalActivity;
    } finally {
      Closeables.closeQuietly(activityDescriptionStream);
      repositoryStorageManager.removeStagedReource(stageHandle);
    }

  }

  @Override
  public String calculateBundleContentHash(ResourceCategory category, String identifyingName,
      Version version) {
    InputStream inputStream = null;
    try {
      inputStream = repositoryStorageManager.getResourceStream(category, identifyingName, version);
      String bundleSignature = resourceSignatureCalculator.getResourceSignature(inputStream);
      inputStream.close();

      return bundleSignature;
    } catch (Throwable e) {
      throw SmartSpacesException.newFormattedException(e,
          "Could not calculate bundle hash for %s:%s:%s", category, identifyingName, version);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  /**
   * Get the bundle content hash for a staged bundle.
   * 
   * @param stagingHandle
   *          the staging handle
   * 
   * @return the hash
   */
  private String calculateBundleContentHash(String stagingHandle) {
    InputStream inputStream = null;
    try {
      inputStream = repositoryStorageManager.getStagedResourceStream(stagingHandle);
      String bundleSignature = resourceSignatureCalculator.getResourceSignature(inputStream);
      inputStream.close();

      return bundleSignature;
    } catch (Throwable e) {
      throw SmartSpacesException.newFormattedException(e,
          "Could not calculate bundle hash for staged resource %s", stagingHandle);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  /**
   * Copy all activity dependencies into the final activity.
   *
   * @param activityDescription
   *          the activity which is being brought in
   * @param finalActivity
   *          the activity stored in the database
   */
  private void copyDependencies(SimpleActivity activityDescription, Activity finalActivity) {
    List<ActivityDependency> finalDependencies = new ArrayList<>();
    for (ActivityDependency dependency : activityDescription.getDependencies()) {
      ActivityDependency newDependency = activityRepository.newActivityDependency();

      newDependency.setIdentifyingName(dependency.getIdentifyingName());
      newDependency.setMinimumVersion(dependency.getMinimumVersion());
      newDependency.setMaximumVersion(dependency.getMaximumVersion());
      newDependency.setRequired(dependency.isRequired());

      finalDependencies.add(newDependency);
    }

    finalActivity.setDependencies(finalDependencies);
  }

  /**
   * Set the activity repository to use.
   *
   * @param activityRepository
   *          the activity repository
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Set the resource repository to use.
   *
   * @param resourceRepository
   *          the resource repository
   */
  public void setResourceRepository(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  /**
   * Set the repository storage manager to use.
   *
   * @param repositoryStorageManager
   *          the repository storage manager
   */
  public void
      setRepositoryStorageManager(ResourceRepositoryStorageManager repositoryStorageManager) {
    this.repositoryStorageManager = repositoryStorageManager;
  }
}
