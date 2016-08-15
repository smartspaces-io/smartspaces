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

package io.smartspaces.activity;

import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.Map;

import org.apache.commons.logging.Log;
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.configuration.Configuration
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.configuration.Configuration
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.configuration.Configuration
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.configuration.Configuration

/**
 * An activity for Smart Spaces.
 *
 * @author Keith M. Hughes
 */
trait Activity extends ActivityControl {

  /**
   * Get the Smart Spaces environment the activity is running under.
   *
   * @return space environment for this activity
   */
  def getSpaceEnvironment(): SmartSpacesEnvironment

  /**
   * Set the Smart Spaces environment the activity is running under.
   *
   * @param spaceEnvironment
   *          space environment for this activity
   */
  def setSpaceEnvironment(spaceEnvironment: SmartSpacesEnvironment): Unit

  /**
   * Set the activity configuration.
   *
   * @param configuration
   *          the activity configuration
   */
  def setConfiguration(configuration: Configuration): Unit

  /**
   * Get the current activity configuration.
   *
   * @return the current activity configuration
   */
  def getConfiguration(): Configuration

  /**
   * Get the activity runtime the activity is running under.
   *
   * @return the activity runtime
   */
  def getActivityRuntime(): ActivityRuntime

  /**
   * Set the activity runtime the activity is running under.
   *
   * @param activityRuntime
   *          the activity runtime
   */
  def setActivityRuntime(activityRuntime: ActivityRuntime): Unit

  /**
   * The configuration has been updated.
   *
   * @param update
   *          the full update, will be {@code null} when called during setup,
   *          though the initial activity configuration will be valid
   */
  def updateConfiguration(update: Map[String, String]): Unit

  /**
   * Is the activity activated?
   *
   * @return {@code true} if the activity is activated
   */
  def isActivated(): Boolean

  /**
   * Get the activity's file system.
   *
   * @return the activity's file system
   */
  def getActivityFilesystem(): ActivityFilesystem

  /**
   * Set the activity's file system.
   *
   * @param activityFilesystem
   *          the activity's file system
   */
  def setActivityFilesystem(activityFilesystem: ActivityFilesystem): Unit

  /**
   * Set the log the activity should use.
   *
   * @param log
   *          logger to use
   */
  def setLog(log: Log): Unit

  /**
   * Get the activity's logger.
   *
   * @return log
   */
  def getLog(): Log

  /**
   * Get the name of the activity.
   *
   * @return the name of the activity
   */
  def getName(): String

  /**
   * Set the name of this activity.
   *
   * @param name
   *          the name to set
   */
  def setName(name: String): Unit

  /**
   * Get the UUID of the activity.
   *
   * @return the UUID of the activity
   */
  def getUuid(): String

  /**
   * Set the UUID of this activity.
   *
   * @param uuid
   *          the uuid to set
   */
  def setUuid(uuid: String): Unit

  /**
   * Do a check on the activity state.
   */
  def checkActivityState(): Unit

  /**
   * What status is the activity in?
   *
   * @return the activity status
   */
  def getActivityStatus(): ActivityStatus

  /**
   * Set the activity status.
   *
   * @param activityStatus
   *          the new activity status
   */
  def setActivityStatus(activityStatus: ActivityStatus): Unit

  /**
   * The activity didn't start. Do any cleanup necessary and clear its status.
   */
  def handleStartupFailure(): Unit

  /**
   * Set the activity execution context for the activity.
   *
   * @param context
   *          execution context to use
   */
  def setExecutionContext(context: ActivityExecutionContext): Unit

  /**
   * Get the execution context.
   *
   * @return activity execution context
   */
  def getExecutionContext(): ActivityExecutionContext

  /**
   * Add a new activity listener to the activity.
   *
   * @param listener
   *          the new listener
   */
  def addActivityListener(listener: ActivityListener): Unit

  /**
   * Remove an activity listener from the activity.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener
   */
  def removeActivityListener(listener: ActivityListener): Unit
}
