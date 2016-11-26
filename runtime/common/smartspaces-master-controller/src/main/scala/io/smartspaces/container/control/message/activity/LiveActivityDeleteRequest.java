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
 * Request for a deletion of a live activity.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityDeleteRequest {

  /**
   * UUID of the live activity to be deleted.
   */
  private String uuid;

  /**
   * Identifying name of the live activity to delete.
   */
  private String identifyingName;

  /**
   * Version of the live activity to delete.
   */
  private String version;

  /**
   * {@code true} if the deletion should be forced.
   */
  private boolean force;

  /**
   * Construct the request.
   */
  public LiveActivityDeleteRequest() {
  }

  /**
   * Construct the request.
   *
   * @param uuid
   *          UUID of the live activity
   * @param identifyingName
   *          identifying name of the live activity
   * @param version
   *          version of the live activity
   * @param force
   *          {@code true} if the deletion should be forced
   */
  public LiveActivityDeleteRequest(String uuid, String identifyingName, String version,
      boolean force) {
    this.uuid = uuid;
    this.identifyingName = identifyingName;
    this.version = version;
    this.force = force;
  }

  /**
   * Get the UUID of the live activity.
   *
   * @return the uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Set the UUID of the live activity.
   *
   * @param uuid
   *          the uuid
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Get the identifying name of the live activity.
   *
   * @return the identifying name
   */
  public String getIdentifyingName() {
    return identifyingName;
  }

  /**
   * Set the identifying name of the live activity.
   *
   * @param identifyingName
   *          the identifying name
   */
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  /**
   * Get the version of the activity.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set the version of the activity.
   *
   * @param version
   *          the version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Should the deletion be forced.
   *
   * @return {@code true} if the deletion should be forced
   */
  public boolean isForce() {
    return force;
  }

  /**
   * Set if the deletion should be forced.
   *
   * @param force
   *          {@code true} if the deletion should be forced
   */
  public void setForce(boolean force) {
    this.force = force;
  }
}
