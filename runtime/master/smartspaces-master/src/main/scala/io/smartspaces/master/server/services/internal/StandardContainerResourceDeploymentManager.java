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

package io.smartspaces.master.server.services.internal;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentItem;
import io.smartspaces.master.server.services.ContainerResourceDeploymentManager;
import io.smartspaces.master.server.services.RemoteSpaceControllerClient;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.resource.NamedVersionedResourceCollection;
import io.smartspaces.resource.NamedVersionedResourceWithData;
import io.smartspaces.resource.ResourceDependency;
import io.smartspaces.resource.ResourceDependencyReference;
import io.smartspaces.resource.repository.ResourceRepositoryServer;
import io.smartspaces.resource.repository.ResourceRepositoryStorageManager;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.resources.ContainerResourceLocation;
import io.smartspaces.util.data.resource.MessageDigestResourceSignatureCalculator;
import io.smartspaces.util.data.resource.ResourceSignatureCalculator;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.uuid.JavaUuidGenerator;
import io.smartspaces.util.uuid.UuidGenerator;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard implementation of a container resource deployment manager.
 *
 * @author Keith M. Hughes
 */
public class StandardContainerResourceDeploymentManager
    implements ContainerResourceDeploymentManager {

  /**
   * The resource repository storage manager to use.
   */
  private ResourceRepositoryStorageManager resourceRepositoryStorageManager;

  /**
   * The repository server to use.
   */
  private ResourceRepositoryServer repositoryServer;

  /**
   * Generator for transaction IDs.
   */
  private final UuidGenerator transactionIdGenerator = new JavaUuidGenerator();

  /**
   * The remote controller client for sending container deployment requests.
   */
  private RemoteSpaceControllerClient remoteSpaceControllerClient;

  /**
   * The space environment to run under.
   */
  private SmartSpacesEnvironment spaceEnvironment;
  
  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * The resource signature calculator.
   */
  private ResourceSignatureCalculator resourceSignatureCalculator =
      new MessageDigestResourceSignatureCalculator();

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public Set<NamedVersionedResourceWithData<URI>>
      satisfyDependencies(Set<ResourceDependencyReference> dependencies) {
    Set<NamedVersionedResourceWithData<URI>> results = new HashSet<>();

    NamedVersionedResourceCollection<NamedVersionedResourceWithData<URI>> allResources =
        resourceRepositoryStorageManager
            .getAllResources(ResourceRepositoryStorageManager.RESOURCE_CATEGORY_CONTAINER_BUNDLE);
    for (ResourceDependency dependency : dependencies) {
      NamedVersionedResourceWithData<URI> resource =
          allResources.getResource(dependency.getName(), dependency.getVersionRange());
      if (resource != null) {
        results.add(resource);
      } else {
        String location = resourceRepositoryStorageManager
            .getBaseLocation(ResourceRepositoryStorageManager.RESOURCE_CATEGORY_CONTAINER_BUNDLE)
            .getAbsolutePath();
        throw new SimpleSmartSpacesException(String.format(
            "Could not find a resource for the dependency %s %s from the master repository %s",
            dependency.getName(), dependency.getVersionRange(), location));
      }
    }

    return results;
  }

  @Override
  public void commitResources(ActiveSpaceController controller,
      Set<NamedVersionedResourceWithData<URI>> resources) {
    String transactionId = transactionIdGenerator.newUuid();

    // TODO(keith): put in some sort of internal data structure to track all
    // requests originated from here.

    commitResources(transactionId, controller, resources);
  }

  @Override
  public void commitResources(String transactionId, ActiveSpaceController controller,
      Set<NamedVersionedResourceWithData<URI>> resources) {
    ContainerResourceDeploymentCommitRequest commitRequest =
        new ContainerResourceDeploymentCommitRequest(transactionId);
    for (NamedVersionedResourceWithData<URI> resource : resources) {
      URI resourceUri = resource.getData();
      String resourceSignature = resourceSignatureCalculator.getResourceSignature(resourceUri);
      if (resourceSignature == null) {
        resourceSignature = ContainerResourceDeploymentItem.RESOURCE_SIGNATURE_NONE;
        spaceEnvironment.getLog().formatWarn(
            "Could not calulate resource signature for resource URI %s", resourceUri.toString());
      }

      commitRequest.addItem(new ContainerResourceDeploymentItem(resource.getName(),
          resource.getVersion(), ContainerResourceLocation.USER_BOOTSTRAP, resourceSignature,
          repositoryServer.getResourceUri(
              ResourceRepositoryStorageManager.RESOURCE_CATEGORY_CONTAINER_BUNDLE,
              resource.getName(), resource.getVersion()), fileSupport.getResourceName(resourceUri)));
    }

    remoteSpaceControllerClient.commitSpaceControllerResourceDeployment(controller, commitRequest);
  }

  /**
   * Set the remote controller client to use.
   *
   * @param remoteSpaceControllerClient
   *          the remote space controller client to use
   */
  public void
      setRemoteSpaceControllerClient(RemoteSpaceControllerClient remoteSpaceControllerClient) {
    this.remoteSpaceControllerClient = remoteSpaceControllerClient;
  }

  /**
   * Set the repository storage manager.
   *
   * @param resourceRepositoryStorageManager
   *          the repository storage manager
   */
  public void setResourceRepositoryStorageManager(
      ResourceRepositoryStorageManager resourceRepositoryStorageManager) {
    this.resourceRepositoryStorageManager = resourceRepositoryStorageManager;
  }

  /**
   * Set the repository server to use.
   *
   * @param repositoryServer
   *          the repository server to use, can be {@code null}
   */
  public void setRepositoryServer(ResourceRepositoryServer repositoryServer) {
    this.repositoryServer = repositoryServer;
  }

  /**
   * Set the space environment to use.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
