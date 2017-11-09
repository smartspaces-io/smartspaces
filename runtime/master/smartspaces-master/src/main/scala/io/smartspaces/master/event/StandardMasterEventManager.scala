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

package io.smartspaces.master.event

import io.smartspaces.activity.ActivityState
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.master.server.services.model.ActiveLiveActivity
import io.smartspaces.master.server.services.model.ActiveSpaceController
import io.smartspaces.spacecontroller.SpaceControllerState
import io.smartspaces.system.SmartSpacesEnvironment

import org.apache.commons.logging.Log

import java.util.List
import java.util.concurrent.CopyOnWriteArrayList
import io.smartspaces.resource.managed.IdempotentManagedResource
import scala.collection.JavaConverters._
import io.smartspaces.event.observable.EventPublisherSubject
import io.smartspaces.event.observable.ObservableCreator

/**
 * A helper for messages to {@link MasterEventListener} instances.
 *
 * @author Keith M. Hughes
 */
class StandardMasterEventManager extends MasterEventManager with IdempotentManagedResource {

  /**
   * Listeners registered with helper.
   */
  private val listeners: List[MasterEventListener] = new CopyOnWriteArrayList()

  /**
   * The creator for space controller offline observables.
   */
  private var spaceControllerOfflineAlertEventCreator: ObservableCreator[EventPublisherSubject[SpaceControllerConnectionLostAlertEvent]] = _

  /**
   * The subject for physical location occupancy events
   */
  private var spaceControllerOfflineAlertEventSubject: EventPublisherSubject[SpaceControllerConnectionLostAlertEvent] = _

  /**
   * The logger for this manager.
   */
  private var log: ExtendedLog = _

  /**
   * The space environment for this managed.
   */
  private var spaceEnvironment: SmartSpacesEnvironment = _

  override def onStartup(): Unit = {
    spaceControllerOfflineAlertEventCreator =
      new ObservableCreator[EventPublisherSubject[SpaceControllerConnectionLostAlertEvent]]() {
        override def newObservable(): EventPublisherSubject[SpaceControllerConnectionLostAlertEvent] = {
          EventPublisherSubject.create(log)
        }
      }

    spaceControllerOfflineAlertEventSubject =
      spaceEnvironment.getEventObservableRegistry.getObservable(
        SpaceControllerConnectionLostAlertEvent.EVENT_TYPE,
        spaceControllerOfflineAlertEventCreator)
  }

  override def addListener(listener: MasterEventListener): Unit = {
    listeners.add(listener)
  }

  override def removeListener(listener: MasterEventListener): Unit = {
    listeners.remove(listener)
  }

  override def removeAllListeners(): Unit = {
    listeners.clear()
  }

  override def signalSpaceControllerConnectAttempted(controller: ActiveSpaceController): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerConnectAttempted(controller)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller connection attempt master event listener: ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerConnectFailed(
    controller: ActiveSpaceController,
    waitedTime: Long): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerConnectFailed(controller, waitedTime)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller connection failure master event listener: ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerDisconnectAttempted(controller: ActiveSpaceController): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerDisconnectAttempted(controller)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller disconnection attempt master event listener: ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerHeartbeat(controller: ActiveSpaceController, timestamp: Long): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerHeartbeat(controller, timestamp)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller heartbeat master event listener: ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerHeartbeatLost(
    controller: ActiveSpaceController,
    timeSinceLastHeartbeat: Long): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerHeartbeatLost(controller, timeSinceLastHeartbeat)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller heartbeat lost master event listener: ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerStatusChange(
    controller: ActiveSpaceController,
    state: SpaceControllerState): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerStatusChange(controller, state)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller status change master event listener (${state}): ${controller.getDisplayName()}",
            e)
      }
    }
  }

  override def signalSpaceControllerShutdown(controller: ActiveSpaceController): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onSpaceControllerShutdown(controller)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing space controller shutdown master event listener: ${controller.getDisplayName()}", e)
      }
    }
  }

  override def signalLiveActivityDeploy(
    liveActivity: ActiveLiveActivity,
    result: LiveActivityDeploymentResponse, timestamp: Long): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onLiveActivityDeploy(liveActivity, result, timestamp)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing live activity deploy master event listener (${result}): ${liveActivity.getDisplayName()}",
            e)
      }
    }
  }

  override def signalLiveActivityDelete(
    liveActivity: ActiveLiveActivity,
    result: LiveActivityDeleteResponse): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onLiveActivityDelete(liveActivity, result)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing live activity delete master event listener (${result}): ${liveActivity.getDisplayName()}",
            e)
      }
    }
  }

  override def signalLiveActivityRuntimeStateChange(
    liveActivity: ActiveLiveActivity,
    oldState: ActivityState, newState: ActivityState): Unit = {
    listeners.asScala.foreach { (listener) =>
      try {
        listener.onLiveActivityStateChange(liveActivity, oldState, newState)
      } catch {
        case e: Throwable =>
          log.error(
            s"Exception while processing live activity deploy master event listener (${oldState} to ${newState}): ${liveActivity.getDisplayName()}",
            e)
      }
    }
  }
  
  override def broadcastSpaceControllerOfflineAlertEvent(event: SpaceControllerConnectionLostAlertEvent): Unit = {
    spaceControllerOfflineAlertEventSubject.onNext(event)
  }

  /**
   * Set the logger for this manager.
   *
   * @param log
   *          the logger
   */
  def setLog(log: ExtendedLog): Unit = {
    this.log = log
  }

  /**
   * Set the space environment for this manager.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  def setSpaceEnvironment(spaceEnvironment: SmartSpacesEnvironment): Unit = {
    this.spaceEnvironment = spaceEnvironment
  }
}
