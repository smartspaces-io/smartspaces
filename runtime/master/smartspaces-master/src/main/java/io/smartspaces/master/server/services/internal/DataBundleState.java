/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 201s4 Google Inc.
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

/**
 * Enum that covers the various states the data capture framework can be in.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public enum DataBundleState {
  NO_REQUEST("space.controller.dataBundle.state.none"), CAPTURE_REQUESTED(
      "space.controller.dataBundle.state.capture.requested"), CAPTURE_RECEIVED(
      "space.controller.dataBundle.state.capture.received"), CAPTURE_ERROR(
      "space.controller.dataBundle.state.capture.error"), RESTORE_REQUESTED(
      "space.controller.dataBundle.state.restore.requested"), RESTORE_RECEIVED(
      "space.controller.dataBundle.state.restore.received"), RESTORE_ERROR(
      "space.controller.dataBundle.state.restore.error");

  /**
   * Description message.
   */
  private String description;

  /**
   * Create data bundle state enum.
   *
   * @param description
   *          description message
   */
  DataBundleState(String description) {
    this.description = description;
  }

  /**
   * Return the user-facing description for this state.
   *
   * @return data bundle state description
   */
  public String getDescription() {
    return description;
  }
}
