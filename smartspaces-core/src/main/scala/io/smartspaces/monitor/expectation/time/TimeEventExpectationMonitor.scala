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

package io.smartspaces.master.server.services.internal;

import io.smartspaces.system.SmartSpacesEnvironment

import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.tasks.ManagedTask
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import java.util.concurrent.TimeUnit

/**
 * Time event expectation monitors watch for heartbeats for monitored items
 * and generate an event if the heartbeat does not happen within an expected time.
 *
 * @author Keith M. Hughes
 */
trait TimeEventExpectationMonitor[T] {
  var monitoredHeartbeatExpectationTime: Long

  /**
   * Add the watcher for a specific space controller.
   *
   * <p>
   * It will be given the timestamp of the current time.
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
  def getMonitoredWatcher(monitoredId: String): Option[TimeMonitorWatcher[T]]

  /**
   * Update the timestamp of a watcher.
   *
   * @param monitoredId
   *          the ID of the monitored item
   * @param heartbeatTimestamp
   *          the timestamp for the heartbeat message
   */
  def updateWatcherTimestamp(monitoredId: String, heartbeatTimestamp: Long): Unit

  /**
   * Scan for alerts.
   */
  def scan(): Unit
}

/**
 * a standard implementation of a time event expectation monitor.
 *
 * @author Keith M. Hughes
 */
class StandardTimeEventExpectationMonitor[T](
  val spaceEnvironment: SmartSpacesEnvironment,
  var monitoredHeartbeatExpectationTime: Long) extends TimeEventExpectationMonitor[T] with IdempotentManagedResource {

  /**
   * The default number of milliseconds the watcher thread delays between scans.
   */
  val WATCHER_DELAY_DEFAULT = 1000

  /**
   * Control for the alert manager.
   */
  private var alertWatcherControl: ManagedTask = _

  /**
   * Number of milliseconds the alert watcher waits before scanning for activity
   * state.
   */
  var alertWatcherDelay = WATCHER_DELAY_DEFAULT

  /**
   * A mapping of monitored item IDs to the watch for that item.
   */
  private val monitoredWatchers: Map[String, TimeMonitorWatcher[T]] = HashMap()

  override def onStartup(): Unit = {
    alertWatcherControl = spaceEnvironment.getContainerManagedScope.scheduleAtFixedRate(new Runnable() {
      override def run(): Unit = {
        scan()
      }
    }, alertWatcherDelay, alertWatcherDelay, TimeUnit.MILLISECONDS)
  }

  override def onShutdown(): Unit = {
    alertWatcherControl.cancel()
  }

  override def addMonitoredWatcher(monitoredId: String, monitored: T): Unit = {
    var timestamp = spaceEnvironment.getTimeProvider.getCurrentTime
    monitoredWatchers.synchronized {
      monitoredWatchers.put(
        monitoredId,
        new TimeMonitorWatcher(monitoredId, monitored, timestamp, monitoredHeartbeatExpectationTime))
    }
  }

  override def getMonitoredWatcher(monitoredId: String): Option[TimeMonitorWatcher[T]] = {
    monitoredWatchers.synchronized {
      return monitoredWatchers.get(monitoredId)
    }
  }

  /**
   * Remove the watcher for a specific space controller.
   *
   * <p>
   * Does nothing if no watcher for the space controller.
   *
   * @param activeSpaceController
   *          the space controller
   */
  def removeWatcher(monitoredId: String): Unit = {
    monitoredWatchers.synchronized {
      monitoredWatchers.remove(monitoredId)
    }
  }
  override def updateWatcherTimestamp(monitoredId: String, heartbeatTimestamp: Long): Unit = {
    val watcher = getMonitoredWatcher(monitoredId)
    if (watcher.isDefined) {
      watcher.get.heartbeat(heartbeatTimestamp)
    } else {
      // TODO(keith): Decide what to do if not found.
    }
  }

  override def scan(): Unit = {
    val currentTimestamp = spaceEnvironment.getTimeProvider().getCurrentTime()

    getWatchers().foreach { (watcher) =>
      if (watcher.check(currentTimestamp)) {
        handleWatcherHeartbeatLost(watcher, watcher.timeSinceLastHeartbeat(currentTimestamp))
      }
    }
  }

  /**
   * Handle a lost heartbeat.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the amount of time the heartbeat has been lost
   */
  private def handleWatcherHeartbeatLost(
    watcher: TimeMonitorWatcher[T],
    timeSinceLastHeartbeat: Long): Unit = {
    spaceEnvironment.getLog().warn(
      s"Lost heartbeat for monitored item for ${timeSinceLastHeartbeat} msec");

  }

  /**
   * Get all space controller watchers currently registered.
   *
   * @return all watchers
   */
  private def getWatchers(): Iterable[TimeMonitorWatcher[T]] = {
    monitoredWatchers.synchronized {
      return monitoredWatchers.values
    }
  }
}

/**
 * The watcher for an individual space controller.
 *
 * @author Keith M. Hughes
 */
class TimeMonitorWatcher[T](
  val monitoredId: String,
  val watchedItem: T,
  var timestamp: Long,
  var expectedTimeInterval: Long) {

  /**
   * {@code true} if an alert has been sent.
   */
  private var alerted = false

  /**
   * New heartbeat coming in. Catch it.
   *
   * @param heartbeatTimestamp
   *          the new heartbeat
   */
  def heartbeat(heartbeatTimestamp: Long): Unit = {
    timestamp = heartbeatTimestamp
    alerted = false
  }

  /**
   * Check the current timestamp.
   *
   * @param currentTimestamp
   *          the time stamp to check against
   *
   * @return [[true]] if an alert should be raised
   */
  def check(currentTimestamp: Long): Boolean = {
    if (timeSinceLastHeartbeat(currentTimestamp) > expectedTimeInterval && !alerted) {
      alerted = true

      true
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

