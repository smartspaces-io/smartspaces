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
 * A standard mixin for giving an item heartbeat functionality.
 * 
 * @author Keith M. Hughes
 */
trait StandardHeartbeatMonitorable extends HeartbeatMonitorable {

  /**
   * Is the item online?
   *
   * <p>
   * Assume that it is offline until told otherwise.
   */
  protected var _online: Boolean = false

  /**
   * The time of the last update.
   */
  protected var _lastUpdateTime: Option[Long] = None

  /**
   * The time of the last heartbeat update.
   */
  protected var _lastHeartbeatUpdate: Option[Long] = None

  /**
   * {@code true} if there has been a signaling of going offline.
   */
  protected var offlineSignaled: Boolean = false

  /**
   * Calculate a timeout online status based on time calculations.
   *
   * @param currentTime
   *        the current time
   * @param referenceTime
   *        the time to be compared to, such as last update time or model creation time
   * @param timeLimit
   *        the maximum amount of time before it is decided to be offline
   *
   * @return {@code true} if there has been a timeout
   */
  protected def isTimeout(currentTime: Long, referenceTime: Long, timeLimit: Long): Boolean = {
    currentTime - referenceTime > timeLimit
  }

  /**
   * An update happened.
   */
  private def updateHappened(): Unit = {
    // The online status is definitely true if an update is coming in.
    offlineSignaled = false
    _online = true
  }

  override def stateUpdated(timestamp: Long): Unit = {
    _lastUpdateTime = Option(timestamp)

    updateHappened
  }

  override def lastUpdateTime(): Option[Long] = {
    _lastUpdateTime
  }

  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  /* protected[time] */ def setLastUpdateTime(time: Long): Unit = {
    _lastUpdateTime = Option(time)
  }
  
  override def updateHeartbeat(timestamp: Long): Unit = {
    _lastHeartbeatUpdate = Option(timestamp)

    updateHappened
  }

  override def lastHeartbeatUpdate(): Option[Long] = {
    _lastHeartbeatUpdate
  }

  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  /* private[time] */ def setLastHeartbeatUpdateTime(time: Long): Unit = {
    _lastHeartbeatUpdate = Option(time)
  }

  override def online(): Boolean = _online
  
  
  /**
   * REMOVE AFTER REFACTOR COMPLETE
   */
  def online_=(o: Boolean) = this._online = o
}