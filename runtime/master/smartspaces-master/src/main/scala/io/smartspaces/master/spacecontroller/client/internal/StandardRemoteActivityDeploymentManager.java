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

package io.smartspaces.master.spacecontroller.client.internal;

import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult.LiveActivityDeploymentStatus;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.master.server.services.ContainerResourceDeploymentManager;
import io.smartspaces.master.server.services.RemoteSpaceControllerClient;
import io.smartspaces.master.server.services.internal.RemoteSpaceControllerClientListenerCollection;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.master.spacecontroller.client.RemoteActivityDeploymentManager;
import io.smartspaces.resource.NamedVersionedResourceWithData;
import io.smartspaces.resource.ResourceDependencyReference;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.VersionRange;
import io.smartspaces.resource.repository.ResourceCategory;
import io.smartspaces.resource.repository.ResourceRepositoryServer;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.uuid.JavaUuidGenerator;
import io.smartspaces.util.uuid.UuidGenerator;

import com.google.common.collect.Maps;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The standard remote activity installer.
 *
 * <p>
 * This manager handles the master end of installing activities to a remote
 * endpoint.
 *
 * @author Keith M. Hughes
 */
public class StandardRemoteActivityDeploymentManager implements RemoteActivityDeploymentManager {

  /**
   * The client for making calls to a remote space controller.
   */
  private RemoteSpaceControllerClient remoteSpaceControllerClient;

  /**
   * Server for activity repository.
   */
  private ResourceRepositoryServer repositoryServer;

  /**
   * The manager for deploying container resources.
   */
  private ContainerResourceDeploymentManager containerResourceDeploymentManager;

  /**
   * Generator for transaction IDs.
   */
  private final UuidGenerator transactionIdGenerator = new JavaUuidGenerator();

  /**
   * Mapping of transaction IDs to the deployment trackers.
   */
  private final Map<String, MasterActivityDeploymentRequestTracker> deploymentTrackers =
      Maps.newConcurrentMap();

  /**
   * The listeners for remote events.
   */
  private RemoteSpaceControllerClientListenerCollection remoteSpaceControllerClientListeners;

  /**
   * {@code true} if should send dependencies every time.
   */
  private final boolean alwaysSendDependencies = true;

  /**
   * The space environment to use.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void deployLiveActivity(ActiveLiveActivity activeLiveActivity) {

    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    Activity activity = liveActivity.getActivity();

    String transactionId = transactionIdGenerator.newUuid();

    MasterActivityDeploymentRequestTracker tracker = new MasterActivityDeploymentRequestTracker(
        activeLiveActivity, transactionId,
        repositoryServer.getResourceUri(ResourceCategory.RESOURCE_CATEGORY_ACTIVITY,
            activity.getIdentifyingName(), Version.parseVersion(activity.getVersion())),
        spaceEnvironment.getTimeProvider().getCurrentTime());

    List<? extends ActivityDependency> dependencies = activity.getDependencies();
    if (dependencies != null && !dependencies.isEmpty()) {
      ContainerResourceDeploymentQueryRequest query =
          new ContainerResourceDeploymentQueryRequest(transactionId);
      for (ActivityDependency dependency : dependencies) {
        query.addQuery(new ResourceDependencyReference(dependency.getIdentifyingName(),
            new VersionRange(Version.parseVersionIncludeNull(dependency.getMinimumVersion()),
                Version.parseVersionIncludeNull(dependency.getMaximumVersion()), false)));
      }
      tracker.setResourceDeploymentQuery(query);
    }

    beginDeployment(tracker);
  }

  @Override
  public void handleLiveDeployResult(LiveActivityDeploymentResult response) {
    MasterActivityDeploymentRequestTracker tracker =
        deploymentTrackers.get(response.getTransactionId());

    if (tracker != null) {
      spaceEnvironment.getLog().info(String.format(
          "Got activity deployment status with transaction ID %s", response.getTransactionId()));

      if (tracker.getStatus() == MasterActivityDeploymentRequestStatus.DEPLOYING_ACTIVITY) {
        finalizeActivityDeployment(tracker, response);
      } else {
        spaceEnvironment.getLog()
            .warn(String.format(
                "The activity deployment status with transaction ID %s was in inconsistent state %s: %s",
                response.getTransactionId(), response.getStatus(), response.getStatusDetail()));
      }
    } else {
      spaceEnvironment.getLog()
          .warn(String.format("Got activity deployment status with unknown transaction ID %s",
              response.getTransactionId()));
    }
  }

  /**
   * Finalize the activity deployment.
   *
   * @param tracker
   *          the deployment request
   * @param response
   *          the response to send
   */
  private void finalizeActivityDeployment(MasterActivityDeploymentRequestTracker tracker,
      LiveActivityDeploymentResult response) {
    updateDeploymentStatus(tracker, MasterActivityDeploymentRequestStatus.DEPLOYMENT_COMPLETE);

    deploymentTrackers.remove(response.getTransactionId());

    remoteSpaceControllerClientListeners.signalActivityDeployStatus(response.getUuid(), response);
  }

  /**
   * Update the deployment status.
   *
   * @param tracker
   *          the request being updated
   * @param status
   *          the new status
   */
  private void updateDeploymentStatus(MasterActivityDeploymentRequestTracker tracker,
      MasterActivityDeploymentRequestStatus status) {
    tracker.updateStatus(status, spaceEnvironment.getTimeProvider().getCurrentTime());
  }

  @Override
  public boolean
      handleResourceDeploymentQueryResponse(ContainerResourceDeploymentQueryResponse response) {
    MasterActivityDeploymentRequestTracker tracker =
        deploymentTrackers.get(response.getTransactionId());

    if (tracker != null) {
      spaceEnvironment.getLog()
          .info(String.format(
              "Got resource deployment query response as part of a live activity deployment with transaction ID %s",
              response.getTransactionId()));

      switch (response.getStatus()) {
        case SPECIFIC_QUERY_SATISFIED:
          deployActivity(tracker);

          break;
        case SPECIFIC_QUERY_NOT_SATISFIED:
          // TODO(keith): Now try and satisfy the dependencies and commit them.

          break;
        default:
          spaceEnvironment.getLog()
              .warn(String.format(
                  "Got resource deployment query response for live activity deployment with "
                      + "transaction ID %s has inconsistent status %s",
                  response.getTransactionId(), response.getStatus()));
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean
      handleResourceDeploymentCommitResponse(ContainerResourceDeploymentCommitResponse response) {
    MasterActivityDeploymentRequestTracker tracker =
        deploymentTrackers.get(response.getTransactionId());

    if (tracker != null) {
      spaceEnvironment.getLog()
          .info(String.format(
              "Got resource deployment commit response as part of a live activity deployment with transaction ID %s",
              response.getTransactionId()));

      switch (response.getStatus()) {
        case SUCCESS:
          deployActivity(tracker);

          break;
        case FAILURE:
          finalizeActivityDeployment(tracker,
              new LiveActivityDeploymentResult(tracker.getTransactionId(),
                  tracker.getDeploymentRequest().getUuid(),
                  LiveActivityDeploymentStatus.STATUS_FAILURE_DEPENDENCIES_NOT_COMMITTED,
                  response.getDetail(), spaceEnvironment.getTimeProvider().getCurrentTime()));

          break;
        default:
          spaceEnvironment.getLog()
              .warn(String.format(
                  "Got resource deployment commit response for live activity deployment with "
                      + "transaction ID %s has inconsistent status %s",
                  response.getTransactionId(), response.getStatus()));
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public void deleteLiveActivity(ActiveLiveActivity activeLiveActivity) {
    LiveActivity liveActivity = activeLiveActivity.getLiveActivity();
    LiveActivityDeleteRequest request = new LiveActivityDeleteRequest(liveActivity.getUuid(),
        liveActivity.getActivity().getIdentifyingName(), liveActivity.getActivity().getVersion(),
        false);

    remoteSpaceControllerClient.deleteLiveActivity(activeLiveActivity, request);
  }

  /**
   * Begin the deployment of an activity.
   *
   * @param tracker
   *          the deployment request
   */
  private void beginDeployment(MasterActivityDeploymentRequestTracker tracker) {
    deploymentTrackers.put(tracker.getTransactionId(), tracker);

    ContainerResourceDeploymentQueryRequest query = tracker.getResourceDeploymentQuery();
    if (query != null) {
      if (shouldSendDependencies(tracker)) {
        Set<NamedVersionedResourceWithData<URI>> dependencyResults =
            containerResourceDeploymentManager.satisfyDependencies(query.getQueries());

        updateDeploymentStatus(tracker,
            MasterActivityDeploymentRequestStatus.SATISFYING_DEPENDENCIES);
        containerResourceDeploymentManager.commitResources(tracker.getTransactionId(),
            tracker.getLiveActivity().getActiveController(), dependencyResults);
      } else {
        // Has dependencies so must query them.
        updateDeploymentStatus(tracker,
            MasterActivityDeploymentRequestStatus.QUERYING_DEPENDENCIES);
        remoteSpaceControllerClient.querySpaceControllerResourceDeployment(
            tracker.getLiveActivity().getActiveController(), query);
      }
    } else {
      // No dependencies so can start.
      deployActivity(tracker);
    }
  }

  /**
   * Should the dependencies be sent regardless?
   *
   * @param tracker
   *          the requested deployment
   *
   * @return {@code true} if the dependencies should be sent regardless
   */
  private boolean shouldSendDependencies(MasterActivityDeploymentRequestTracker tracker) {
    // TODO(keith): Add something where we look for things like dev qualifiers.
    return alwaysSendDependencies;
  }

  /**
   * Do the actual activity deployment.
   *
   * @param tracker
   *          the deployment request
   */
  private void deployActivity(MasterActivityDeploymentRequestTracker tracker) {
    updateDeploymentStatus(tracker, MasterActivityDeploymentRequestStatus.DEPLOYING_ACTIVITY);
    remoteSpaceControllerClient.deployLiveActivity(tracker.getLiveActivity(),
        tracker.getDeploymentRequest());
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
   * Set the remote controller client to use.
   *
   * @param remoteControllerClient
   *          the remote controller client to use
   */
  public void setRemoteSpaceControllerClient(RemoteSpaceControllerClient remoteControllerClient) {
    this.remoteSpaceControllerClient = remoteControllerClient;
    remoteSpaceControllerClientListeners =
        remoteControllerClient.registerRemoteActivityDeploymentManager(this);
  }

  /**
   * Set the container resource deployment manager to use.
   *
   * @param containerResourceDeploymentManager
   *          the container resource deployment manager to use
   */
  public void setContainerResourceDeploymentManager(
      ContainerResourceDeploymentManager containerResourceDeploymentManager) {
    this.containerResourceDeploymentManager = containerResourceDeploymentManager;
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
