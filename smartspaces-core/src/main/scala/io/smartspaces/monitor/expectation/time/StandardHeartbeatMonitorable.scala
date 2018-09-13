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
   * The time of the last state update.
   */
  protected var _timestampLastStateUpdate: Option[Long] = None

  /**
   * The time of the last heartbeat update.
   */
  protected var _timestampLastHeartbeatUpdate: Option[Long] = None

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
  private def updateHappened(timestamp: Long): Unit = {
    if (!_online && offlineSignaled) {
      emitOnlineEvent(timestamp)
    }
    
    // The online status is definitely true if an update is coming in.
    offlineSignaled = false
    _online = true
  }

  override def stateUpdated(timestamp: Long): Unit = {
    _timestampLastStateUpdate = Option(timestamp)

    updateHappened(timestamp)
  }

  override def timestampLastStateUpdate: Option[Long] = {
    _timestampLastStateUpdate
  }

  override def updateHeartbeat(timestamp: Long): Unit = {
    _timestampLastHeartbeatUpdate = Option(timestamp)

    updateHappened(timestamp)
  }

  override def timestampLastHeartbeatUpdate: Option[Long] = {
    _timestampLastHeartbeatUpdate
  }

  override def timestampLastUpdate: Option[Long] = {
    if (_timestampLastStateUpdate.isDefined) {
      if (_timestampLastHeartbeatUpdate.isDefined) {
        Some(Math.max(_timestampLastStateUpdate.get, _timestampLastHeartbeatUpdate.get))
      } else {
        _timestampLastStateUpdate
      }
    } else {
      _timestampLastHeartbeatUpdate
    }
  }
  
  override def online: Boolean = _online

  override def checkIfOfflineTransition(currentTime: Long): Boolean = {
    // Only check if the model thinks it is online and there was an update time,
    // otherwise we want the initial
    if (_online) {
      if (stateUpdateTimeLimit.isDefined) {
        // The only way we would ever be considered online is if there was a lastUpdate,
        // so the .get will work.
        _online = !isTimeout(currentTime, _timestampLastStateUpdate.get, stateUpdateTimeLimit.get)
      } else if (heartbeatUpdateTimeLimit.isDefined) {
        // If this sensor requires a heartbeat, the heartbeat time can be checked.

        val updateToUse = if (_timestampLastStateUpdate.isDefined) {
          if (_timestampLastHeartbeatUpdate.isDefined) {
            Math.max(_timestampLastStateUpdate.get, _timestampLastHeartbeatUpdate.get)
          } else {
            _timestampLastStateUpdate.get
          }
        } else {
          _timestampLastHeartbeatUpdate.get
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
          if (isTimeout(currentTime, _timestampLastStateUpdate.getOrElse(timestampItemCreation), stateUpdateTimeLimit.get)) {
            signalOffline(currentTime)

            true
          } else {
            false
          }
        } else if (heartbeatUpdateTimeLimit.isDefined) {
          // If this sensor requires a heartbeat, the heartbeat time can be checked.
          if (isTimeout(currentTime, _timestampLastHeartbeatUpdate.getOrElse(timestampItemCreation), heartbeatUpdateTimeLimit.get)) {
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
  private def signalOffline(timestamp: Long): Unit = {
    offlineSignaled = true
    
    emitOfflineEvent(timestamp)
  }

  /**
   * Get the time limit for a heartbeat update.
   */
  def heartbeatUpdateTimeLimit: Option[Long] = None

  /**
   * Get the time limit for a state update.
   */
  def stateUpdateTimeLimit: Option[Long] = None

  /**
   * Set whether online or not.
   *
   * <p>
   * This is for testing.
   */
  def setOnline(o: Boolean): Unit = {
    this._online = o
  }
  
  
  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  /* private[time] */ def setLastHeartbeatUpdateTime(time: Long): Unit = {
    _timestampLastHeartbeatUpdate = Some(time)
  }

  /**
   * Set the last update time.
   *
   * <p>
   * This is for testing.
   */
  /* protected[time] */ def setLastUpdateTime(time: Long): Unit = {
    _timestampLastStateUpdate = Some(time)
  }

  
  /**
   * Set if offline signaled.
   *
   * <p>
   * This is for testing.
   */
  def setOfflineSignaled(offlineSignaled: Boolean): Unit = {
    this.offlineSignaled = offlineSignaled
  }
}