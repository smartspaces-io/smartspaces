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

package io.smartspaces.activity.behavior.ros

import org.ros.node.ConnectedNode
import org.ros.osgi.common.RosEnvironment
import io.smartspaces.activity.ActivityBehavior

/**
 * An {@link Activity} which uses ROS.
 *
 * @author Keith M. Hughes
 */
trait RosActivityBehavior extends ActivityBehavior {

  /**
   * Get the current ROS Environment being used by this activity.
   *
   * @return the ROS environment, can be {@code null}
   */
  def getRosEnvironment(): RosEnvironment

  /**
   * Get the main ROS node for this activity.
   *
   * @return the main ROS node
   */
  def getMainNode(): ConnectedNode
}
