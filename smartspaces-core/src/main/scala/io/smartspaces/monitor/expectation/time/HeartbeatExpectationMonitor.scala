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

import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.tasks.ManagedTask

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.time.provider.TimeProvider

/**
 * Time event expectation monitors watch for heartbeats for monitored items
 * and generate an event if the heartbeat does not happen within an expected time.
 *
 * @author Keith M. Hughes
 */
trait HeartbeatExpectationMonitor[T] {

  /**
   * The expectation time for heartbeats.
   */
  var monitoredHeartbeatExpectationTimeDefault: Long

  /**
   * Add the watcher for a monitored item.
   *
   * <p>
   * It will be given the timestamp of the current time.
   *
   * @param monitored
   *          the item being monitored
   * @param monitoredId
   *          the ID of the item being monitored
   * @param monitoredHeartbeatExpectationTime
   *          the time after which the heartbeat will be considered lost
   */
  def addMonitoredWatcher(monitoredId: String, monitored: T, monitoredHeartbeatExpectationTime: Long): Unit

  /**
   * Add the watcher for a monitored item.
   *
   * <p>
   * It will be given the timestamp of the current time.
   *
   * <p>
   * The default expectation time will be used.
   *
   * @param monitored
   *          the item being monitored
   * @param monitoredId
   *          the ID of the item being monitored
   */
  def addMonitoredWatcher(monitoredId: String, monitored: T): Unit

  /**
   * Get the watcher for a specific monitored item.
   *
   * @param monitoredId
   *          the ID of the monitored item
   *
   * @return the watcher
   */
  def getMonitoredWatcher(monitoredId: String): Option[HeartbeatMonitorWatcher[T]]

  /**
   * Update the timestamp of a watcher.
   *
   * @param monitoredId
   *          the ID of the monitored item
   * @param heartbeatTimestamp
   *          the timestamp for the heartbeat event
   */
  def updateHeartbeat(monitoredId: String, heartbeatTimestamp: Long): Unit

  /**
   * Scan for alerts.
   */
  def scan(): Unit
}

/**
 * A listener for events on the heartbeat expectation monitor.
 *
 * @author Keith M. Hughes
 */
trait HeartbeatExpectationMonitorListener[T] {

  /**
   * A heartbeat was lost.
   *
   * @param watcher
   *        the watcher for the item that lost its heartbeat
   * @param timeLost
   *        timestamp when the heartbeat loss was noticed
   * @param timeSinceLastHeartbeat
   *          the amount of time the heartbeat has been lost
   *
   */
  def onHeartbeatLost(watcher: HeartbeatMonitorWatcher[T], timeLost: Long, timeSinceLastHeartbeat: Long): Unit

  /**
   * A heartbeat was regained.
   *
   * @param watcher
   *        the watcher for the item that lost its heartbeat
   * @param timeRegained
   *        timestamp when the heartbeat was regained
   *
   */
  def onHeartbeatRegained(watcher: HeartbeatMonitorWatcher[T], timeRegained: Long): Unit
}

/**
 * a standard implementation of a time event expectation monitor.
 *
 * @author Keith M. Hughes
 */
class StandardHeartbeatExpectationMonitor[T](
  var monitoredHeartbeatExpectationTimeDefault: Long,
  val heartbeatExpectationMonitorListener: HeartbeatExpectationMonitorListener[T],
  timeProvider: TimeProvider,
  log: ExtendedLog) extends HeartbeatExpectationMonitor[T] {

  /**
   * A mapping of monitored item IDs to the watch for that item.
   */
  private val monitoredWatchers: Map[String, HeartbeatMonitorWatcher[T]] = HashMap()

  override def addMonitoredWatcher(monitoredId: String, monitored: T): Unit = {
    addMonitoredWatcher(monitoredId, monitored, monitoredHeartbeatExpectationTimeDefault)
  }

  override def addMonitoredWatcher(monitoredId: String, monitored: T, monitoredHeartbeatExpectationTime: Long): Unit = {
    var timestamp = timeProvider.getCurrentTime
    monitoredWatchers.synchronized {
      monitoredWatchers.put(
        monitoredId,
        new HeartbeatMonitorWatcher(monitoredId, monitored, timestamp, monitoredHeartbeatExpectationTime))
    }
  }

  override def getMonitoredWatcher(monitoredId: String): Option[HeartbeatMonitorWatcher[T]] = {
    monitoredWatchers.synchronized {
      return monitoredWatchers.get(monitoredId)
    }
  }

  /**
   * Remove the watcher for a specific ID.
   *
   * <p>
   * Does nothing if no watcher for that ID.
   *
   * @param monitoredId
   *          the ID of the monitored item to remove
   */
  def removeWatcher(monitoredId: String): Unit = {
    monitoredWatchers.synchronized {
      monitoredWatchers.remove(monitoredId)
    }
  }

  override def updateHeartbeat(monitoredId: String, heartbeatTimestamp: Long): Unit = {
    val watcher = getMonitoredWatcher(monitoredId)
    if (watcher.isDefined) {
      if (watcher.get.updateHeartbeat(heartbeatTimestamp)) {
        handleWatcherHeartbeatRegained(watcher.get, heartbeatTimestamp)
      }
    } else {
      // TODO(keith): Decide what to do if not found.
    }
  }

  override def scan(): Unit = {
    val currentTimestamp = timeProvider.getCurrentTime()

    getWatchers().foreach { (watcher) =>
      if (watcher.checkIfOfflineTransition(currentTimestamp)) {
        handleWatcherHeartbeatLost(watcher, watcher.timeSinceLastHeartbeat(currentTimestamp), currentTimestamp)
      }
    }
  }

  /**
   * Handle a lost heartbeat.
   *
   * @param watcher
   *          the watcher that has lots its heartbeat
   * @param timeSinceLastHeartbeat
   *          the amount of time the heartbeat has been lost
   * @param currentTimestamp
   *          the current time
   */
  private def handleWatcherHeartbeatLost(
    watcher: HeartbeatMonitorWatcher[T],
    timeSinceLastHeartbeat: Long,
    currentTimestamp: Long): Unit = {

    try {
      heartbeatExpectationMonitorListener.onHeartbeatLost(watcher, currentTimestamp, timeSinceLastHeartbeat)
    } catch {
      case e: Throwable =>
        log.error(s"Heartbeat monitor listener failed for lost event with watcher ${watcher.monitoredId}", e)
    }
  }

  /**
   * Handle a regained heartbeat.
   *
   * @param watcher
   *          the watcher that has lots its heartbeat
   * @param heartbeatTimestamp
   *          the time of the heartbeat that triggered this message
   */
  private def handleWatcherHeartbeatRegained(
    watcher: HeartbeatMonitorWatcher[T],
    heartbeatTimestamp: Long): Unit = {

    try {
      heartbeatExpectationMonitorListener.onHeartbeatRegained(watcher, heartbeatTimestamp)
    } catch {
      case e: Throwable =>
        log.error(s"Heartbeat monitor listener failed for regained event with watcher ${watcher.monitoredId}", e)
    }
  }

  /**
   * Get all watchers currently registered.
   *
   * @return all watchers
   */
  private def getWatchers(): Iterable[HeartbeatMonitorWatcher[T]] = {
    monitoredWatchers.synchronized {
      return monitoredWatchers.values
    }
  }
}

/**
 * An object that is having its heartbeat monitored.
 * 
 * @author Keith M. Hughes
 */
trait HeartbeatMonitored {
  
  /**
   * Update the heartbeat.
   *
   * @param heartbeatTimestamp
   *          the new heartbeat time
   */
  def updateHeartbeat(heartbeatTimestamp: Long): Unit
  
  /**
   * Calculate the time since the last heartbeat.
   *
   * @param currentTimestamp
   *        the current timestamp
   */
  def timeSinceLastHeartbeat(currentTimestamp: Long): Long
}

/**
 * The watcher for an individual monitored item.
 *
 * @author Keith M. Hughes
 */
class HeartbeatMonitorWatcher[T](
  val monitoredId: String,
  val monitored: T,
  var timestamp: Long,
  var expectedTimeInterval: Long) {

  /**
   * {@code true} if the item is considered online
   */
  private var lost = new AtomicBoolean(false)

  /**
   * Update the heartbeat.
   *
   * @param heartbeatTimestamp
   *          the new heartbeat time
   *
   * @return {@code true} if the heartbeat had been previously lost
   */
  def updateHeartbeat(heartbeatTimestamp: Long): Boolean = {
    timestamp = heartbeatTimestamp
    lost.getAndSet(false)
  }

  /**
   * Check if the object is transitioning to an offline status.
   *
   * @param currentTimestamp
   *          the time stamp to check against
   *
   * @return [[true]] if noticing it is lost
   */
  def checkIfOfflineTransition(currentTimestamp: Long): Boolean = {
    if (timeSinceLastHeartbeat(currentTimestamp) > expectedTimeInterval) {
      !lost.getAndSet(true)
    } else {
      false
    }
  }

  /**
   * Calculate the time since the last heartbeat.
   *
   * @param currentTimestamp
   *        the current timestamp
   */
  def timeSinceLastHeartbeat(currentTimestamp: Long): Long = {
    currentTimestamp - timestamp
  }
}

