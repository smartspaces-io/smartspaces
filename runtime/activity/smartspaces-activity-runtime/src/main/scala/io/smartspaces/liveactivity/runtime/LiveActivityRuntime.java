/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime;

import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.ActivityRuntime;
import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.util.resource.ManagedResource;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A runtime that can run live activities.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRuntime extends ActivityRuntime, ManagedResource {

  /**
   * Add a new runtime listener to the runtime.
   *
   * @param listener
   *          the new listener
   */
  void addRuntimeListener(LiveActivityRuntimeListener listener);

  /**
   * Remove a runtime listener from the runtime.
   *
   * <p>
   * This method does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeRuntimeListener(LiveActivityRuntimeListener listener);

  /**
   * Get the logger for the indicated activity.
   *
   * @param activity
   *          activity to log
   * @param configuration
   *          configuration properties
   * @param activityFilesystem
   *          file system for the activity
   *
   * @return logger for this activity and configuration
   */
  Log getActivityLog(InstalledLiveActivity activity, Configuration configuration,
      ActivityFilesystem activityFilesystem);

  /**
   * Release an activity log.
   *
   * @param activityLog
   *          a logger for an activity
   */
  void releaseActivityLog(Log activityLog);

  /**
   * Start up all activities in the controller that aren't currently started.
   */
  void startupAllActivities();

  /**
   * Shut down all activities in the controller.
   */
  void shutdownAllActivities();

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void startupLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void shutdownLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to activate.
   */
  void activateLiveActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to deactivate
   */
  void deactivateLiveActivity(String uuid);

  /**
   * Cause a status check of an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to get the status of
   */
  void statusLiveActivity(String uuid);

  /**
   * Configure the activity.
   *
   * @param uuid
   *          uuid of the activity
   * @param configuration
   *          the configuration request
   */
  void configureLiveActivity(String uuid, Map<String, String> configuration);

  /**
   * Get all live activities installed on this controller.
   *
   * @return all locally installed activities
   */
  List<InstalledLiveActivity> getAllInstalledLiveActivities();

  /**
   * Get an activity by UUID.
   *
   * @param uuid
   *          the UUID of the activity
   *
   * @return the activity with the given UUID, {@code null} if no such activity
   */
  LiveActivityRunner getLiveActivityRunnerByUuid(String uuid);

  /**
   * Prepare an instance of an activity to run.
   *
   * @param installedActivity
   *          information about the live activity whose instance is to be
   *          initialized
   * @param activityFilesystem
   *          the filesystem for the activity instance
   * @param instance
   *          the instance of the activity being started up
   * @param configuration
   *          the configuration for the instance
   * @param activityLog
   *          the log for the activity
   * @param executionContext
   *          execution context for this activity
   */
  void initializeActivityInstance(InstalledLiveActivity installedActivity,
      ActivityFilesystem activityFilesystem, Activity instance, Configuration configuration,
      Log activityLog, ActivityExecutionContext executionContext);

  /**
   * Clean the temp data folder for a given activity.
   *
   * @param uuid
   *          uuid of the activity
   */
  void cleanLiveActivityTmpData(String uuid);

  /**
   * Clean the permanent data folder for a given activity.
   *
   * @param uuid
   *          uuid of the activity
   */
  void cleanLiveActivityPermanentData(String uuid);

  /**
   * Set the publisher for live activity status information.
   *
   * @param liveActivityStatusPublisher
   *          the publisher
   */
  void setLiveActivityStatusPublisher(LiveActivityStatusPublisher liveActivityStatusPublisher);

  /**
   * Get the live activity runner factory instance used by the runtime.
   *
   * @return the live activity runner factory instance
   */
  LiveActivityRunnerFactory getLiveActivityRunnerFactory();

  /**
   * Get the activity storage manager.
   *
   * @return the activity storage manager
   */
  LiveActivityStorageManager getLiveActivityStorageManager();

  /**
   * Does the runtime have live activities running?
   *
   * @return {@code true} if any live activities are running
   */
  boolean hasLiveActivitiesRunning();

  /**
   * Is the specified live activity running?
   *
   * @param uuid
   *          the UUID of the live activity
   *
   * @return {@code true} if the live activities is running
   */
  boolean isLiveActivityRunning(String uuid);
}
