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

import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.resource.managed.ManagedResource;

/**
 * The manager for handling container resource deployment.
 *
 * @author Keith M. Hughes
 */
public interface ContainerResourceDeploymentManager extends ManagedResource {

  /**
   * Query the container to see if a series of deployment requests are already
   * satisfied.
   *
   * @param deploymentQuery
   *          the query
   *
   * @return the response for the query
   */
  ContainerResourceDeploymentQueryResponse queryResources(
      ContainerResourceDeploymentQueryRequest deploymentQuery);

  /**
   * Commit a container resource deployment.
   *
   * @param request
   *          the commit request
   *
   * @return the response for the request
   */
  ContainerResourceDeploymentCommitResponse commitResources(
      ContainerResourceDeploymentCommitRequest request);
}
