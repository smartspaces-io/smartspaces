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

package io.smartspaces.liveactivity.runtime.logging;

import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.logging.ExtendedLog;

/**
 * A factory for loggers for activities.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityLogFactory {

  /**
   * Get the logger for a local activity.
   *
   * @param installedActivity
   *          the activity a logger is needed for
   * @param level
   *          initial level for the logger
   * @param activityFilesystem
   *          file system for the activity
   *
   * @return a fully configured logger at the requested level
   */
  ExtendedLog createLogger(InstalledLiveActivity installedActivity, String level,
      ActivityFilesystem activityFilesystem);

  /**
   * Release an activity log.
   *
   * @param activityLog
   *          the activity log to release
   */
  void releaseLog(ExtendedLog activityLog);
}
