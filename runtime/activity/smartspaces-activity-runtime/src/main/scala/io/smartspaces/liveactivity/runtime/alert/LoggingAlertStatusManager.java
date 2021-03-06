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

package io.smartspaces.liveactivity.runtime.alert;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.activity.ActivityStatus;
import io.smartspaces.liveactivity.runtime.LiveActivityRunner;

import org.apache.commons.logging.Log;

/**
 * Use the logger to report status.
 *
 * <p>
 * This should not be used in production. Someone should be emailed, or paged,
 * or something.
 *
 * @author Keith M. Hughes
 */
public class LoggingAlertStatusManager implements AlertStatusManager {

  /**
   * The designator for unknown items.
   */
  public static final String UNKNOWN = "UNKNOWN";

  /**
   * Local log where things should be written.
   */
  private Log log;

  /**
   * Construct a new manager.
   *
   * @param log
   *          the logger to use
   */
  public LoggingAlertStatusManager(Log log) {
    this.log = log;
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void announceLiveActivityStatus(LiveActivityRunner liveActivityRunner) {
    ActivityStatus status = liveActivityRunner.getCachedActivityStatus();

    String activityState = UNKNOWN;
    ActivityState state = status.getState();
    if (state != null) {
      activityState = state.toString();
    }

    log.error(String.format("ALERT: Activity %s has serious issues: %s\n",
        liveActivityRunner.getUuid(), activityState));
    String description = status.getDescription();
    if (description != null && !description.trim().isEmpty()) {
      log.error(description);
    }

    if (status != null) {
      String exceptionMessage = status.getExceptionAsString();
      if (exceptionMessage != null) {
        log.error(String.format("ALERT Exception involved: %s", exceptionMessage));
      }
    }
  }
}
