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

/**
 * a standard implementation of a time event expectation monitor.
 *
 * @author Keith M. Hughes
 */
class StandardTimeEventExpectationMonitor[T] extends IdempotentManagedResource {

  /**
   * The delay for handing a controller shut down event in milliseconds.
   */
  val CONTROLLER_SHUTDOWN_EVENT_HANDLING_DELAY = 500

  /**
   * Default number of milliseconds for space controller failure.
   */
  val SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT = 30000L

  /**
   * The default number of milliseconds the watcher thread delays between scans.
   */
  val WATCHER_DELAY_DEFAULT = 1000

  /**
   * Number of milliseconds after not receiving a heartbeat for a space
   * controller that we will raise the alarm.
   */
  val spaceControllerHeartbeatTime = SPACE_CONTROLLER_HEARTBEAT_TIME_DEFAULT;

  /**
   * Control for the alert manager.
   */
  private var alertWatcherControl: ManagedTask = _

  /**
   * Number of milliseconds the alert watcher waits before scanning for activity
   * state.
   */
  val alertWatcherDelay = WATCHER_DELAY_DEFAULT

  /**
   * A mapping of controller UUIDs to the controller.
   */
  private val spaceControllerWatchers: Map[String, TimeMonitorWatcher[T]] = HashMap()


  /**
   * The space environment to use.
   */
  private var spaceEnvironment: SmartSpacesEnvironment = _

  override def onStartup(): Unit = {
    alertWatcherControl = spaceEnvironment.getExecutorService().scheduleAtFixedRate(new Runnable() {
     override def run(): Unit = {
        scan()
      }
    }, alertWatcherDelay, alertWatcherDelay, TimeUnit.MILLISECONDS);
  }

  override def onShutdown(): Unit = {
      alertWatcherControl.cancel()
  }

  /**
   * Scan for alerts.
   */
  def scan(): Unit = {
    val currentTimestamp = spaceEnvironment.getTimeProvider().getCurrentTime()

    getSpaceControllerWatchers().foreach {
      _.check(currentTimestamp)
    }
  }

  /**
   * Handle a lost space controller heartbeat.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the amount of time the heartbeat has been lost
   */
  private def handleSpaceControllerHeartbeatLost(activeSpaceController: T,
      timeSinceLastHeartbeat: Long): Unit = {
    spaceEnvironment.getLog().warn(
        s"Lost heartbeat for space controller for ${ timeSinceLastHeartbeat} msec: hi");

    disconnectAndRaiseAlert(activeSpaceController, timeSinceLastHeartbeat);
  }

  /**
   * Disconnect from a space controller that has lost its connection and raise
   * an alert.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          the time since the last connection acknowledgement
   */
  private def disconnectAndRaiseAlert( activeSpaceController:T,
     timeSinceLastHeartbeat: Unit) {

    // TODO: Move alerting into an alerting system separate from a business
    // logic manager that decides that an alert is
    // needed

    // Shouldn't block the thread with alert which could take time depending on
    // what is being contacted, e.g. email
    // server.
    spaceEnvironment.getExecutorService().execute(new Runnable() {
      override def run(): Unit = {
        raiseHeartbeatLostAlert(activeSpaceController, timeSinceLastHeartbeat)
      }
    });
  }

  /**
   * Raise the heartbeat alert.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timeSinceLastHeartbeat
   *          how late the heartbeat is, in milliseconds
   */
  private def raiseHeartbeatLostAlert(activeSpaceController: T,
      timeSinceLastHeartbeat: Unit): Unit = {
    try {
      // Send event here
    } catch  {
      case e: Throwable =>
      spaceEnvironment.getLog().error(
          String.format("Lost heartbeat alert for space controller: %s",
              activeSpaceController.getDisplayName()), e);
    }
  }

  /**
   * Handle a controller heartbeat.
   *
   * @param activeSpaceController
   *          the space controller
   * @param timestamp
   *          timestamp of the controller coming in
   */
  def handleSpaceControllerHeartbeat(activeSpaceController: T,
      timestamp: Long): Unit = {
    val watcher =
        getSpaceControllerWatcher(activeSpaceController.getSpaceController().getUuid());

    if (watcher.isDefined) {
      watcher.heartbeat(timestamp)
    } else {
      spaceEnvironment.getLog().warn(
          s"Master alert manager got heartbeat for unknown space controller ${activeSpaceController.getSpaceController().getUuid()}}")
    }
  }

  /**
   * Add the watcher for a specific space controller.
   *
   * <p>
   * It will be given the timestamp of the current time.
   *
   * @param activeSpaceController
   *          the space controller
   */
  private def addSpaceControllerWatcher(activeSpaceController: T): Unit = {
    var timestamp = spaceEnvironment.getTimeProvider.getCurrentTime
    synchronized (spaceControllerWatchers) {
      var watcher =
          new TimeMonitorWatcher(activeSpaceController, timestamp)
      spaceControllerWatchers.put(activeSpaceController.getSpaceController().getUuid(), watcher);
    }
  }

  /**
   * Get the watcher for a specific space controller.
   *
   * @param uuid
   *          the UUID of the space controller
   *
   * @return the watcher
   */
  def getSpaceControllerWatcher(uuid: String ): Option[TimeMonitorWatcher] {
    synchronized (spaceControllerWatchers) {
      return spaceControllerWatchers.get(uuid)
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
 def removeSpaceControllerWatcher(activeSpaceController: T): Unit = {
    synchronized (spaceControllerWatchers) {
      spaceControllerWatchers.remove(activeSpaceController.getSpaceController().getUuid()
    }
  }

  /**
   * Get all space controller watchers currently registered.
   *
   * @return all watchers
   */
  private def getSpaceControllerWatchers(): List[TimeMonitorWatcher] = {
    synchronized (spaceControllerWatchers) {
      return spaceControllerWatchers.values()
    }
  }

  /**
   * Set the maximum amount of time willing to wait for a controller heartbeat
   * before complaining.
   *
   * @param spaceControllerHeartbeatTime
   *          the time to wait in milliseconds
   */
 def setSpaceControllerHeartbeatTime(spaceControllerHeartbeatTime: Long): Unit = {
    this.spaceControllerHeartbeatTime = spaceControllerHeartbeatTime
  }

  /**
   * Get the maximum amount of time willing to wait for a controller heartbeat
   * before complaining.
   *
   * @return the time to wait in milliseconds
   */
  def getSpaceControllerHeartbeatTime(): Long = {
    return spaceControllerHeartbeatTime
  }
}
  /**
   * The watcher for an individual space controller.
   *
   * @author Keith M. Hughes
   */
class TimeMonitorWatcher[T](val watchedItem: T, var timestamp: Long, var expectedTimeInterval: Long) {

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
      // activeSpaceController.setHeartbeatTime(heartbeatTimestamp)
      alerted = false
    }

    /**
     * Check the current timestamp.
     *
     * @param currentTimestamp
     *          the time stamp to check against
     */
    def check(currentTimestamp: Long): Boolean = {
      var timeSinceLastHeartbeat = currentTimestamp - timestamp
      if (timeSinceLastHeartbeat > expectedTimeInterval && !alerted) {
        alerted = true

        true
      } else {
        false
      }
    }
  }
