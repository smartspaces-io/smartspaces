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

import io.smartspaces.resource.Version;

/**
 * A request for an activity deployment.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityDeploymentRequest {

  /**
   * The transaction ID for this request.
   */
  private String transactionId;

  /**
   * UUID for the activity.
   */
  private String uuid;

  /**
   * Identifying name for the activity.
   */
  private String identifyingName;

  /**
   * Version of the activity.
   */
  private Version version;

  /**
   * URI for getting the activity.
   */
  private String activitySourceUri;

  /**
   * Construct a deployment request.
   */
  public LiveActivityDeploymentRequest() {
  }
  
  /**
   * Construct a deployment request.
   *
   * @param transactionId
   *          ID for the deployment transaction
   * @param uuid
   *          UUID of the live activity
   * @param identifyingName
   *          identifying name of the live activity
   * @param version
   *          version of the activity
   * @param activitySourceUri
   *          URI for obtaining the source
   */
  public LiveActivityDeploymentRequest(String transactionId, String uuid, String identifyingName,
      Version version, String activitySourceUri) {
    this.transactionId = transactionId;
    this.uuid = uuid;
    this.identifyingName = identifyingName;
    this.version = version;
    this.activitySourceUri = activitySourceUri;
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
   * Set the transaction ID for the request.
   *
   * @param transactionId
   *          the transaction ID
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the UUID for the live activity being deployed.
   *
   * @return the UUID for the live activity
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Set the UUID for the live activity being deployed.
   *
   * @param uuid
   *          the UUID for the live activity
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Get the identifying name for the live activity being deployed.
   *
   * @return the identifying name for the live activity
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Set the identifying name for the live activity being deployed.
   *
   * @param identifyingName
   *          the identifying name for the live activity
   */
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  /**
   * Get the version of the live activity being deployed.
   *
   * @return the version of the live activity
   */
  public Version getVersion() {
    return version;
  }

  /**
   * Set the version of the live activity being deployed.
   *
   * @param version
   *          the version of the live activity
   */
  public void setVersion(Version version) {
    this.version = version;
  }

  /**
   * Get the URI for obtaining the activity.
   *
   * @return the URI for obtaining the activity
   */
  public String getActivitySourceUri() {
    return activitySourceUri;
  }

  /**
   * Set the URI for obtaining the activity.
   *
   * @param activitySourceUri
   *          the URI for obtaining the activity
   */
  public void setActivitySourceUri(String activitySourceUri) {
    this.activitySourceUri = activitySourceUri;
  }

  @Override
  public String toString() {
    return "ActivityDeploymentRequest [transactionId=" + transactionId + ", uuid=" + uuid
        + ", identifyingName=" + identifyingName + ", version=" + version + ", activitySourceUri="
        + activitySourceUri + "]";
  }
}
