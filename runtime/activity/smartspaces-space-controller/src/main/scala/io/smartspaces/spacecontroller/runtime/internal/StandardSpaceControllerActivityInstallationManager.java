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

package io.smartspaces.spacecontroller.runtime.internal;

import io.smartspaces.SmartSpacesExceptionUtils;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResult;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResult.LiveActivityDeleteStatus;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResult.LiveActivityDeploymentStatus;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationManager;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationManager.RemoveActivityResult;
import io.smartspaces.spacecontroller.runtime.SpaceControllerActivityInstallationManager;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.Date;

/**
 * The controller side of the installation manager for Smart Spaces live
 * activities.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceControllerActivityInstallationManager implements
    SpaceControllerActivityInstallationManager {

  /**
   * The Spaces environment being used.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * Manages deployment of activities.
   */
  private final ActivityInstallationManager activityInstallationManager;

  /**
   * Construct a controller activity installation manager.
   *
   * @param activityInstallationManager
   *          the installation manager for an activity
   * @param spaceEnvironment
   *          the space environment to run under
   */
  public StandardSpaceControllerActivityInstallationManager(
      ActivityInstallationManager activityInstallationManager,
      SmartSpacesEnvironment spaceEnvironment) {
    this.activityInstallationManager = activityInstallationManager;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
  }

  @Override
  public LiveActivityDeploymentResult handleDeploymentRequest(
      LiveActivityDeploymentRequest request) {
    String activityUri = request.getActivitySourceUri();
    String uuid = request.getUuid();

    // This will be usually set to the current possible error status.
    LiveActivityDeploymentStatus status = null;
    boolean success = true;

    Date installedDate = null;

    try {
      status = LiveActivityDeploymentStatus.FAILURE_COPY;
      activityInstallationManager.copyActivity(uuid, activityUri);

      status = LiveActivityDeploymentStatus.FAILURE_UNPACK;
      installedDate =
          activityInstallationManager.installActivity(uuid, request.getIdentifyingName(),
              request.getVersion());

      status = LiveActivityDeploymentStatus.SUCCESS;
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Could not install live activity %s", uuid), e);

      success = false;
    } finally {
      activityInstallationManager.removePackedActivity(uuid);
    }

    return createDeployResult(request, status, success, installedDate);
  }

  /**
   * Create the status for the deployment.
   *
   * @param request
   *          the deployment request
   * @param status
   *          final status
   * @param success
   *          if the deployment was successful
   * @param installedDate
   *          date to mark it if successful
   * @return an appropriately filled out deployment status
   */
  private LiveActivityDeploymentResult createDeployResult(LiveActivityDeploymentRequest request,
		  LiveActivityDeploymentStatus status, boolean success, Date installedDate) {

    long timeDeployed = 0;
    if (installedDate != null) {
      timeDeployed = installedDate.getTime();
    }

    return new LiveActivityDeploymentResult(request.getTransactionId(), request.getUuid(),
        status, null, timeDeployed);
  }

  @Override
  public LiveActivityDeleteResult handleDeleteRequest(LiveActivityDeleteRequest request) {
    RemoveActivityResult result = RemoveActivityResult.FAILURE;

    String statusDetail = null;

    try {
      result = activityInstallationManager.removeActivity(request.getUuid());
    } catch (Throwable e) {
      statusDetail = SmartSpacesExceptionUtils.getExceptionDetail(e);

      spaceEnvironment.getLog().error(
          String.format("Could not delete live activity %s\n%s", request.getUuid(), statusDetail));
    }

    return createDeleteResponse(request, result, statusDetail);
  }

  /**
   * Create the delete response to the request.
   *
   * @param request
   *          the deletion request
   * @param result
   *          result of the deletion request
   * @param statusDetail
   *          status detail for the response
   *
   * @return the response to be sent back
   */
  public LiveActivityDeleteResult createDeleteResponse(LiveActivityDeleteRequest request,
      RemoveActivityResult result, String statusDetail) {
    LiveActivityDeleteStatus status;
    switch (result) {
      case SUCCESS:
        status = LiveActivityDeleteStatus.SUCCESS;
        break;

      case DOESNT_EXIST:
        status = LiveActivityDeleteStatus.DOESNT_EXIST;
        break;

      default:
        status = LiveActivityDeleteStatus.FAILURE;
    }

    return new LiveActivityDeleteResult(request.getUuid(), status, spaceEnvironment
        .getTimeProvider().getCurrentTime(), statusDetail);
  }
}
