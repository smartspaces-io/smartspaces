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
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

/**
 * A {@link LiveActivityLogFactory} which uses the
 * {@link SmartSpacesEnvironment} to get a logger.
 *
 * @author Keith M. Hughes
 */
public class SmartSpacesEnvironmentLiveActivityLogFactory implements LiveActivityLogFactory {

  /**
   * Prefix to be affixed to the logger name.
   */
  private static final String ACTIVITY_LOG_PREFIX = "activity";

  /**
   * The name of the file for the per-activity log.
   */
  private static final String ACTIVITY_LOG_FILENAME = "activity.log";

  /**
   * The Smart Spaces environment this is being run in.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The file support to be used by the factory.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a new activity log factory for the given space environment.
   *
   * @param spaceEnvironment
   *          environment to define logging context
   */
  public SmartSpacesEnvironmentLiveActivityLogFactory(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public ExtendedLog createLogger(InstalledLiveActivity installedActivity, String level,
      ActivityFilesystem activityFilesystem) {
    return spaceEnvironment.getLog(ACTIVITY_LOG_PREFIX + "." + installedActivity.getUuid(), level,
        fileSupport.newFile(activityFilesystem.getLogDirectory(), ACTIVITY_LOG_FILENAME)
            .getAbsolutePath());
  }

  @Override
  public void releaseLog(ExtendedLog activityLog) {
    spaceEnvironment.releaseLog(activityLog);
  }
}
