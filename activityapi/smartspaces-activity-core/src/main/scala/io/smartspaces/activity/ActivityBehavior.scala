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

package io.smartspaces.activity

import java.util.Map

/**
 * Methods that an activity behavior should support.
 * 
 * @author Keith M. Hughes
 */
trait ActivityBehavior extends SupportedActivity {
  
  /**
   * Perform any common tasks for activity configuration.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   *
   * @param update
   *          the update map
   */
  def commonActivityConfigurationUpdate(update: Map[String, String]): Unit

  /**
   * Setup any needed activity components and other startup.
   *
   * <p>
   * This method is not normally used by activity developers, they should
   * install components in {@link #addActivityComponent(ActivityComponent)}.
   * This allows a support base class to add in things unknown to the casual
   * user.
   */
  def commonActivitySetup(): Unit

  /**
   * Any common startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityStartup(): Unit

  /**
   * Any common post startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityPostStartup(): Unit

  /**
   * Any common activate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityActivate(): Unit

  /**
   * Any common deactivate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityDeactivate(): Unit

  /**
   * Any common pre-shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityPreShutdown(): Unit

  /**
   * Any common shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  def commonActivityShutdown(): Unit

  /**
   * Cleanup any activity in support implementations.
   *
   * <p>
   * This method is not normally used by activity developers, they should clean
   * up their activity in {@link #onActivityCleanup()}. This allows a support
   * base class to add in things unknown to the casual user.
   */
  def commonActivityCleanup(): Unit
}
