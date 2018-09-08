/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.monitor.expectation.time

/**
 * The entity is online.
 *
 * @author Keith M. Hughes
 */
trait Online {

  /**
   * [[true]] if the entity is online.
   */
  def online(): Boolean
}

/**
 * An item that can be online and can be checked to see if it has transitioned to offline
 * at a particular time.
 * 
 * @author Keith M. Hughes
 */
trait OnlineMonitorable extends Online {
  
  /**
   * Check to see if the sensor is still considered online.
   *
   * <p>
   * This method will change the {@link #online} status and emit an event
   * if the sensor has become offline
   *
   * @param currentTime
   * 				the current time to check against
   *
   * @return [[true]] if has transitioned to offline
   */
  def checkIfOfflineTransition(currentTime: Long): Boolean
}

/**
 * An item that is being monitored by heartbeat and state updates.
 * 
 * @author Keith M. Hughes
 */
trait HeartbeatMonitorable extends OnlineMonitorable {
  
  /**
   * When this item was created.
   */
  def itemCreationTime: Long

  /**
   * A value in the state has been updated.
   *
   * @param updateTime
   * 		      the time of this update
   */
  def stateUpdated(updateTime: Long): Unit

  /**
   * Get the last update for the item.
   *
   * @return the last time
   */
  def lastUpdateTime(): Option[Long]

  /**
   * Update the heartbeat for the item.
   *
   * @param updateTime
   * 		      the time of this update
   */
  def updateHeartbeat(updateTime: Long): Unit

  /**
   * Get the last heartbeat update for the item.
   *
   * @return the last time
   */
  def lastHeartbeatUpdate(): Option[Long]
}
