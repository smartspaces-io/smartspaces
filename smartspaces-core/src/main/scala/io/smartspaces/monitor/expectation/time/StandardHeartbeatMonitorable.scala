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

  override def checkIfOfflineTransition(currentTime: Long): Boolean = {
    // Only check if the model thinks it is online and there was an update time,
    // otherwise we want the initial
    if (_online) {
      if (stateUpdateTimeLimit.isDefined) {
        // The only way we would ever be considered online is if there was a lastUpdate,
        // so the .get will work.
        _online = !isTimeout(currentTime, _lastUpdateTime.get, stateUpdateTimeLimit.get)
      } else if (heartbeatUpdateTimeLimit.isDefined) {
        // If this sensor requires a heartbeat, the heartbeat time can be checked.

        val updateToUse = if (_lastUpdateTime.isDefined) {
          if (_lastHeartbeatUpdate.isDefined) {
            Math.max(_lastUpdateTime.get, _lastHeartbeatUpdate.get)
          } else {
            _lastUpdateTime.get
          }
        } else {
          _lastHeartbeatUpdate.get
        }
        
        _online = !isTimeout(currentTime, updateToUse, heartbeatUpdateTimeLimit.get)
      }

      // We knew online was true, so if now offline, then transitioned.
      if (!_online) {
        signalOffline(currentTime)
      }

      !_online
    } else {
      // Now, we are considered offline. If we have never been updated then we can check at the
      // time of birth of the model. otherwise no need to check.
      if (!offlineSignaled) {
        if (stateUpdateTimeLimit.isDefined) {
          if (isTimeout(currentTime, _lastUpdateTime.getOrElse(itemCreationTime), stateUpdateTimeLimit.get)) {
            signalOffline(currentTime)

            true
          } else {
            false
          }
        } else if (heartbeatUpdateTimeLimit.isDefined) {
          // If this sensor requires a heartbeat, the heartbeat time can be checked.
          if (isTimeout(currentTime, _lastHeartbeatUpdate.getOrElse(itemCreationTime), heartbeatUpdateTimeLimit.get)) {
            signalOffline(currentTime)

            true
          } else {
            false
          }
        } else {
          false
        }
      } else {
        false
      }
    }
  }

  /**
   * Signal that the sensor has gone offline.
   *
   * @param currentTime
   * 		the time when the sensor was detected offline
   */
  private def signalOffline(currentTime: Long): Unit = {
    offlineSignaled = true
  }

  def heartbeatUpdateTimeLimit: Option[Long] = None

  def stateUpdateTimeLimit: Option[Long] = None

  /**
   * REMOVE AFTER REFACTOR COMPLETE
   */
  def setOnline(o: Boolean): Unit = this._online = o
}