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

package io.smartspaces.controller.client.master.internal;

import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.master.server.services.ActiveLiveActivity;
import io.smartspaces.resource.Version;

/**
 * The deployment request for an activity from the master side.
 *
 * @author Keith M. Hughes
 */
public class MasterActivityDeploymentRequestTracker {

  /**
   * The transaction ID for the deployment.
   */
  private final String transactionId;
  
  /**
   * The live activity being deployed.
   */
  private final ActiveLiveActivity liveActivity;

  /**
   * Any query for resource deployment needs.
   */
  private ContainerResourceDeploymentQueryRequest resourceDeploymentQuery;

  /**
   * Current status of the deployment.
   */
  private MasterActivityDeploymentRequestStatus status;

  /**
   * Time that deployment began.
   */
  private final long deploymentBeginTime;

  /**
   * The last time a status update came in.
   */
  private long lastStatusUpdateTime;

  /**
   * The deployment request.
   */
  private final LiveActivityDeploymentRequest deploymentRequest;

  /**
   * Construct a new request.
   *
   * @param liveActivity
   *          the live activity to deploy
   * @param transactionId
   *          transaction ID for the deployment
   * @param activitySourceUri
   *          URI for loading the activity
   * @param deploymentBeginTime
   *          at what time the deployment started
   */
  public MasterActivityDeploymentRequestTracker(ActiveLiveActivity liveActivity,
      String transactionId, String activitySourceUri, long deploymentBeginTime) {
    deploymentRequest =
        new LiveActivityDeploymentRequest(transactionId, liveActivity.getLiveActivity().getUuid(),
            liveActivity.getLiveActivity().getActivity().getIdentifyingName(),
            Version.parseVersion(liveActivity.getLiveActivity().getActivity().getVersion()),
            activitySourceUri);
    this.transactionId = transactionId;
    this.liveActivity = liveActivity;
    this.deploymentBeginTime = deploymentBeginTime;
  }

  /**
   * Get the transaction ID for the request.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Get the live activity that is the target of this request.
   *
   * @return the live activity
   */
  public ActiveLiveActivity getLiveActivity() {
    return liveActivity;
  }

  /**
   * Get the deployment request for the tracker.
   * 
   * @return the deployment request
   */
  public LiveActivityDeploymentRequest getDeploymentRequest() {
    return deploymentRequest;
  }

  /**
   * Update the status of the deployment request.
   *
   * @param newStatus
   *          the new status
   * @param statusUpdateTime
   *          time of the update
   */
  public void updateStatus(MasterActivityDeploymentRequestStatus newStatus, long statusUpdateTime) {
    status = newStatus;
    lastStatusUpdateTime = statusUpdateTime;
  }

  /**
   * Get the query for any resources needed by the activity.
   *
   * @return the query, can be {@code null} if none
   */
  public ContainerResourceDeploymentQueryRequest getResourceDeploymentQuery() {
    return resourceDeploymentQuery;
  }

  /**
   * Set the query for any resources needed by the activity.
   *
   * @param resourceDeploymentQuery
   *          the query
   */
  public void
      setResourceDeploymentQuery(ContainerResourceDeploymentQueryRequest resourceDeploymentQuery) {
    this.resourceDeploymentQuery = resourceDeploymentQuery;
  }

  /**
   * Get the current status of the deploy.
   *
   * @return the current status of the deploy
   */
  public MasterActivityDeploymentRequestStatus getStatus() {
    return status;
  }

  /**
   * Get the time the deploy began.
   *
   * @return the time the deploy began
   */
  public long getDeploymentBeginTime() {
    return deploymentBeginTime;
  }

  /**
   * Get the last time the status was updated.
   *
   * @return the last time the status was updated
   */
  public long getLastStatusUpdateTime() {
    return lastStatusUpdateTime;
  }
}
