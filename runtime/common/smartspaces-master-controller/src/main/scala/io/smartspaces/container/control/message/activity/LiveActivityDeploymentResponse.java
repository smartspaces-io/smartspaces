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

package io.smartspaces.container.control.message.activity;

/**
 * The response of a live activity deployment on the controller.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityDeploymentResponse {

  /**
   * Transaction ID for the deployment.
   */
  private String transactionId;

  /**
   * UUID of the live activity that was deployed.
   */
  private String uuid;

  /**
   * Status of the deployment.
   */
  private ActivityDeployStatus status;

  /**
   * Status detail of the deployment.
   */
  private String statusDetail;

  /**
   * Time that the activity was deployed.
   */
  private long timeDeployed;

  /**
   * Construct a response.
   */
  public LiveActivityDeploymentResponse() {
  }

  /**
   * Construct a response.
   *
   * @param transactionId
   *          transaction ID for the response
   * @param uuid
   *          UUID of the live activity
   * @param status
   *          status of the deployment
   * @param statusDetail
   *          detail of the status
   * @param timeDeployed
   *          time the activity was deployed
   */
  public LiveActivityDeploymentResponse(String transactionId, String uuid,
      ActivityDeployStatus status, String statusDetail, long timeDeployed) {
    this.transactionId = transactionId;
    this.uuid = uuid;
    this.status = status;
    this.statusDetail = statusDetail;
    this.timeDeployed = timeDeployed;
  }

  /**
   * Get the transaction ID for the deployment.
   *
   * @return the transaction ID for the deployment
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Set the transaction ID for the deployment.
   *
   * @param transactionId
   *          the transaction ID for the deployment
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the UUID for the live activity which was deployed.
   *
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Set the UUID for the live activity which was deployed.
   *
   * @param uuid
   *          the uuid
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Get the status of the deployment.
   *
   * @return the status
   */
  public ActivityDeployStatus getStatus() {
    return status;
  }

  /**
   * Set the status of the deployment.
   *
   * @param status
   *          the status
   */
  public void setStatus(ActivityDeployStatus status) {
    this.status = status;
  }

  /**
   * Get the detail of the deployment status.
   *
   * @return the detail, can be {@code null}
   */
  public String getStatusDetail() {
    return statusDetail;
  }

  /**
   * Set the detail of the deployment status.
   *
   * @param statusDetail
   *          the detail, can be {@code null}
   */
  public void setStatusDetail(String statusDetail) {
    this.statusDetail = statusDetail;
  }

  /**
   * Get the time of the deployment, according to the remote system.
   *
   * @return the time of the deployment
   */
  public long getTimeDeployed() {
    return timeDeployed;
  }

  /**
   * Set the time of the deployment, according to the remote system.
   *
   * @param timeDeployed
   *          the time of the deployment
   */
  public void setTimeDeployed(long timeDeployed) {
    this.timeDeployed = timeDeployed;
  }

  /**
   * State of the deployment.
   *
   * @author Keith M. Hughes
   */
  public enum ActivityDeployStatus {
    /**
     * Deployment was a success.
     */
    SUCCESS(true, "Live activity deployment was successful"),

    /**
     * Failed to copy the activity from the repository.
     */
    FAILURE_COPY(false, "The live activity failed to copy to the remote destination"),

    /**
     * Could not unpack the live activity.
     */
    FAILURE_UNPACK(false, "The live activity could not be unpacked at the remote destination"),

    /**
     * The dependencies could not be committed for the deployment.
     */
    STATUS_FAILURE_DEPENDENCIES_NOT_COMMITTED(false, "Dependencies for the live activity could not be installed");

    /**
     * {@code true} if this is a success message.
     */
    private final boolean success;

    /**
     * A more detailed description of the status.
     */
    private final String description;

    /**
     * Construct a status.
     *
     * @param success
     *          {@code true} if this is a success state
     * @param description
     *          description of the status
     */
    private ActivityDeployStatus(boolean success, String description) {
      this.success = success;
      this.description = description;
    }

    /**
     * Is this a success state?
     *
     * @return {@code true} if this is a success state
     */
    public boolean isSuccess() {
      return success;
    }

    /**
     * Get the description of the status.
     *
     * @return the description of the status
     */
    public String getDescription() {
      return description;
    }
  }
}
