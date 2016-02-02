/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.spacecontroller.resource.deployment;

import io.smartspaces.SmartSpacesExceptionUtils;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse.ContainerResourceDeploymentCommitStatus;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentItem;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse.QueryResponseStatus;
import io.smartspaces.resource.ResourceDependency;
import io.smartspaces.resource.io.HttpCopierResourceSource;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.resources.ContainerResource;
import io.smartspaces.system.resources.ContainerResourceCollection;
import io.smartspaces.system.resources.ContainerResourceManager;
import io.smartspaces.system.resources.ContainerResourceType;
import io.smartspaces.util.web.HttpClientHttpContentCopier;
import io.smartspaces.util.web.HttpContentCopier;

import java.io.File;
import java.util.List;

/**
 * The controller manager for resource deployment.
 *
 * @author Keith M. Hughes
 */
public class ControllerContainerResourceDeploymentManager implements
    ContainerResourceDeploymentManager {

  /**
   * A directory relative to the controller's tmp data directory for
   * deployments.
   */
  public static final String TEMPORARY_SUBDIRECTORY_DEPLOYMENT = "smartspaces/deployment";

  /**
   * The container's resource manager.
   */
  private final ContainerResourceManager containerResourceManager;

  /**
   * The content copier.
   */
  private HttpContentCopier contentCopier;

  /**
   * The space environment.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * The temporary directory for resource deployments.
   */
  private File deploymentTempDirectory;

  /**
   * Construct a deployment manager.
   *
   * @param containerResourceManager
   *          the resource manager for the container
   * @param spaceEnvironment
   *          the space environment
   */
  public ControllerContainerResourceDeploymentManager(
      ContainerResourceManager containerResourceManager, SmartSpacesEnvironment spaceEnvironment) {
    this.containerResourceManager = containerResourceManager;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    contentCopier = new HttpClientHttpContentCopier();
    contentCopier.startup();

    deploymentTempDirectory =
        spaceEnvironment.getFilesystem().getTempDirectory(TEMPORARY_SUBDIRECTORY_DEPLOYMENT);
  }

  @Override
  public void shutdown() {
    if (contentCopier != null) {
      contentCopier.shutdown();
      contentCopier = null;
    }
  }

  @Override
  public ContainerResourceDeploymentQueryResponse queryResources(
      ContainerResourceDeploymentQueryRequest deploymentQuery) {
    ContainerResourceCollection currentResources = containerResourceManager.getResources();
    if (currentlySatisfiesQuery(deploymentQuery, currentResources)) {
      return new ContainerResourceDeploymentQueryResponse(deploymentQuery.getTransactionId(),
          QueryResponseStatus.SPECIFIC_QUERY_SATISFIED);
    } else {
      ContainerResourceDeploymentQueryResponse response =
          new ContainerResourceDeploymentQueryResponse(deploymentQuery.getTransactionId(),
              QueryResponseStatus.SPECIFIC_QUERY_NOT_SATISFIED);

      List<ContainerResource> allContainerResources = currentResources.getAllResources();
      // TODO(keith): place all resources into the response for dependency
      // calculations on the master.

      return response;
    }
  }

  /**
   * Query the container to see if a series of deployment requests are already
   * satisfied.
   *
   * @param deploymentQuery
   *          the query
   * @param currentResources
   *          the current resources in the container
   *
   * @return {@code true} if already satisfies the query
   */
  private boolean currentlySatisfiesQuery(ContainerResourceDeploymentQueryRequest deploymentQuery,
      ContainerResourceCollection currentResources) {
    for (ResourceDependency query : deploymentQuery.getQueries()) {
      ContainerResource resource =
          currentResources.getResource(query.getName(), query.getVersionRange());
      if (resource == null) {
        return false;
      }
    }

    return true;
  }

  @Override
  public ContainerResourceDeploymentCommitResponse commitResources(
      ContainerResourceDeploymentCommitRequest request) {
    boolean success = true;
    String detail = null;
    for (ContainerResourceDeploymentItem item : request.getItems()) {
      try {
        containerResourceManager.addResource(item
            .asContainerResource(ContainerResourceType.LIBRARY),
            new HttpCopierResourceSource(item.getResourceSourceUri(), contentCopier,
                deploymentTempDirectory));
      } catch (Throwable e) {
        success = false;
        detail = SmartSpacesExceptionUtils.getExceptionDetail(e);
        spaceEnvironment.getLog().error(
            String.format("Could not install deployment resource %s\n%s",
                item.getResourceSourceUri(), detail));

        break;
      }
    }

    return new ContainerResourceDeploymentCommitResponse(request.getTransactionId(),
        success ? ContainerResourceDeploymentCommitStatus.SUCCESS
            : ContainerResourceDeploymentCommitStatus.FAILURE, detail);
  }
}
