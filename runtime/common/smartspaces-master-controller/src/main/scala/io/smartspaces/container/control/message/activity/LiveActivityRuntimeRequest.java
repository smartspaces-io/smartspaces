/*
 * Copyright (C) 2016 Keith M. Hughes
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

import io.smartspaces.container.control.message.common.ConfigurationRequest;

/**
 * A runtime request for a live activity.
 * 
 * @author Keith M. Hughes
 */
public class LiveActivityRuntimeRequest {

  /**
   * UUID for the live activity.
   */
  private String uuid;

  /**
   * The operation to be completed.
   */
  private String operation;

  /**
   * {@code} true if an operation should be forced if the controller does not
   * want to do so.
   * 
   * <p>
   * This will not work for all operations.
   */
  private boolean force;

  /**
   * The configuration object if the operation is a configuration request.
   * {@code null} otherwise.
   */
  private ConfigurationRequest configurationRequest;

  /**
   * Construct a new runtime request.
   */
  public LiveActivityRuntimeRequest() {
  }

  /**
   * Construct a new runtime request.
   * 
   * @param uuid
   *          UUID of the live activity
   * @param operation
   *          the operation to be done
   * @param force
   *          {@code} true if an operation should be forced if the controller
   *          does not want to do so
   */
  public LiveActivityRuntimeRequest(String uuid, String operation, boolean force) {
    this(uuid, operation, force, null);
  }

  /**
   * Construct a new runtime request.
   * 
   * @param uuid
   *          UUID of the live activity
   * @param operation
   *          the operation to be done
   * @param force
   *          {@code} true if an operation should be forced if the controller
   *          does not want to do so
   * @param configurationRequest
   *          the configuration request if the operation is for a configuration
   *          update
   */
  public LiveActivityRuntimeRequest(String uuid, String operation, boolean force,
      ConfigurationRequest configurationRequest) {
    this.uuid = uuid;
    this.operation = operation;
    this.force = force;
    this.configurationRequest = configurationRequest;
  }

  /**
   * Get the UUID for the live activity.
   * 
   * @return the UUID
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Set the UUID for the live activity.
   * 
   * @param uuid
   *          the UUID
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Get the operation to be completed.
   * 
   * @return the operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Set the operation to be completed.
   * 
   * @param operation
   *          the operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * Should the operation be forced?
   * 
   * @return {@code} true if the operation should be forced if the controller
   *         does not want to do so
   */
  public boolean isForce() {
    return force;
  }

  /**
   * Set whether the operation should be forced.
   * 
   * @param force
   *          {@code} true if the operation should be forced if the controller
   *          does not want to do so
   */
  public void setForce(boolean force) {
    this.force = force;
  }

  /**
   * Get the configuration request if this is a configuration operation.
   * 
   * @return the configuration request, or {@code null} if not a configuration
   *         operation
   */
  public ConfigurationRequest getConfigurationRequest() {
    return configurationRequest;
  }

  /**
   * Set the configuration request if this is a configuration operation.
   * 
   * @param configurationRequest
   *          the configuration request, or {@code null} if not a configuration
   *          operation
   */
  public void setConfigurationRequest(ConfigurationRequest configurationRequest) {
    this.configurationRequest = configurationRequest;
  }
}
