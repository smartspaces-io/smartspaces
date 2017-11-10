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

package io.smartspaces.master.event

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.reactivex.Observable
import io.reactivex.Observer
import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.event.observable.StandardEventObservableRegistry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.spacecontroller.SpaceControllerState
import io.smartspaces.master.server.services.model.ActiveLiveActivity
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse
import io.smartspaces.activity.ActivityState
import io.smartspaces.master.server.services.model.ActiveSpaceController
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse.ActivityDeployStatus

/**
 * Tests for the standard master event manager.
 *
 * @author Keith M. Hughes
 */
class StandardMasterEventManagerTest extends JUnitSuite {

  var eventManager: StandardMasterEventManager = _

  var eventRegistry: EventObservableRegistry = _

  @Mock var log: ExtendedLog = _

  @Mock var spaceEnvironment: SmartSpacesEnvironment = _

  @Mock var eventListener: MasterEventListener = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    eventRegistry = new StandardEventObservableRegistry(log)
    Mockito.when(spaceEnvironment.getEventObservableRegistry).thenReturn(eventRegistry)

    eventManager = new StandardMasterEventManager()
    eventManager.setLog(log)
    eventManager.setSpaceEnvironment(spaceEnvironment)

    eventManager.startup()

    eventManager.addListener(eventListener)
  }

  /**
   * See if the connection lost event happens.
   */
  @Test def testSpaceControllerConnectionLostAlert(): Unit = {
    val observable: Observable[SpaceControllerConnectionLostAlertEvent] =
      eventRegistry.getObservable(SpaceControllerConnectionLostAlertEvent.EVENT_TYPE)

    val observer: Observer[SpaceControllerConnectionLostAlertEvent] =
      Mockito.mock(classOf[Observer[SpaceControllerConnectionLostAlertEvent]])

    observable.subscribe(observer)

    val event = new SpaceControllerConnectionLostAlertEvent(1000l, "foo", "bar", "bletch", "spam")
    eventManager.broadcastSpaceControllerConnectionLostAlertEvent(event)

    Mockito.verify(observer).onNext(event)
  }

  /**
   * See if the connection failure event happens.
   */
  @Test def testSpaceControllerConnectionFailureAlert(): Unit = {
    val observable: Observable[SpaceControllerConnectionFailureAlertEvent] =
      eventRegistry.getObservable(SpaceControllerConnectionFailureAlertEvent.EVENT_TYPE)

    val observer: Observer[SpaceControllerConnectionFailureAlertEvent] =
      Mockito.mock(classOf[Observer[SpaceControllerConnectionFailureAlertEvent]])

    observable.subscribe(observer)

    val event = new SpaceControllerConnectionFailureAlertEvent(1000l, "foo", "bar", "bletch", "spam")
    eventManager.broadcastSpaceControllerConnectionFailureAlertEvent(event)

    Mockito.verify(observer).onNext(event)
  }

  @Test def testSignalSpaceControllerConnectAttempted(): Unit = {
    val controller = new ActiveSpaceController(null, null)

    eventManager.signalSpaceControllerConnectAttempted(controller)

    Mockito.verify(eventListener).onSpaceControllerConnectAttempted(controller)
  }

  @Test def testSignalSpaceControllerConnectFailed(): Unit = {
    val controller = new ActiveSpaceController(null, null)
    val waitedTime = 1000l

    eventManager.signalSpaceControllerConnectFailed(controller, waitedTime)

    Mockito.verify(eventListener).onSpaceControllerConnectFailed(controller, waitedTime)
  }

  @Test def testSignalSpaceControllerDisconnectAttempted(): Unit = {
    val controller = new ActiveSpaceController(null, null)

    eventManager.signalSpaceControllerDisconnectAttempted(controller)

    Mockito.verify(eventListener).onSpaceControllerDisconnectAttempted(controller)
  }

  @Test def testSignalSpaceControllerHeartbeat(): Unit = {
    val controller = new ActiveSpaceController(null, null)
    val timestamp = 1000l

    eventManager.signalSpaceControllerHeartbeat(controller, timestamp)

    Mockito.verify(eventListener).onSpaceControllerHeartbeat(controller, timestamp)
  }

  @Test def testSignalSpaceControllerHeartbeatLost(): Unit = {
    val controller = new ActiveSpaceController(null, null)
    val timeSinceLastHeartbeat = 10000l

    eventManager.signalSpaceControllerHeartbeatLost(controller, timeSinceLastHeartbeat)

    Mockito.verify(eventListener).onSpaceControllerHeartbeatLost(controller, timeSinceLastHeartbeat)
  }

  @Test def testSignalSpaceControllerStatusChange(): Unit = {
    val controller = new ActiveSpaceController(null, null)
    val state = SpaceControllerState.RUNNING

    eventManager.signalSpaceControllerStatusChange(controller, state)
  }

  @Test def testSignalSpaceControllerShutdown(): Unit = {
    val controller = new ActiveSpaceController(null, null)

    eventManager.signalSpaceControllerShutdown(controller)

    Mockito.verify(eventListener).onSpaceControllerShutdown(controller)
  }

  /**
   * Test live activity deployment signalling.
   */
  @Test def signalLiveActivityDeploy(): Unit = {
    val liveActivity = new ActiveLiveActivity(null, null, null, null)
    val result = new LiveActivityDeploymentResponse("bar", "bletch",
      ActivityDeployStatus.SUCCESS, "foo", 100l)
    val timestamp = 10l

    eventManager.signalLiveActivityDeploy(liveActivity, result, timestamp)

    Mockito.verify(eventListener).onLiveActivityDeploy(liveActivity, result, timestamp)
  }

  /**
   * Test live activity delete signalling.
   */
  @Test def testSignalLiveActivityDelete(): Unit = {

    val liveActivity = new ActiveLiveActivity(null, null, null, null)
    val result = new LiveActivityDeleteResponse(null, null, 1000l, null)

    eventManager.signalLiveActivityDelete(liveActivity, result)

    Mockito.verify(eventListener).onLiveActivityDelete(liveActivity, result)
  }

  @Test def testSignalLiveActivityRuntimeStateChange(): Unit = {
    val liveActivity = new ActiveLiveActivity(null, null, null, null)
    val oldState = ActivityState.ACTIVE
    val newState = ActivityState.CRASHED

    eventManager.signalLiveActivityRuntimeStateChange(liveActivity, oldState, newState)

    Mockito.verify(eventListener).onLiveActivityStateChange(liveActivity, oldState, newState)
  }

}