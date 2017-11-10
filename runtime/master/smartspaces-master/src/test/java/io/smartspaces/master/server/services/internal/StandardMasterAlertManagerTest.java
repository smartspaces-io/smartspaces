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

package io.smartspaces.master.server.services.internal;

import static org.mockito.Mockito.when;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse.ActivityDeployStatus;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.event.MasterEventManager;
import io.smartspaces.master.event.SpaceControllerConnectionFailureAlertEvent;
import io.smartspaces.master.event.SpaceControllerConnectionLostAlertEvent;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.spacecontroller.SpaceControllerState;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.time.provider.TimeProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link StandardMasterAlertManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterAlertManagerTest {

  private StandardMasterAlertManager alertManager;

  @Mock
  private SmartSpacesEnvironment spaceEnvironment;

  @Mock
  private TimeProvider timeProvider;

  @Mock
  private MasterEventManager masterEventManager;
  
  @Mock
  private ScheduledExecutorService executorService;

  @Mock
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  @Mock
  private ExtendedLog log;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);

    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);
    when(spaceEnvironment.getExecutorService()).thenReturn(executorService);

    log = Mockito.mock(ExtendedLog.class);
    when(spaceEnvironment.getLog()).thenReturn(log);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    activeSpaceControllerManager = Mockito.mock(ActiveSpaceControllerManager.class);

    alertManager = new StandardMasterAlertManager();
    alertManager.setSpaceEnvironment(spaceEnvironment);
    alertManager.setMasterEventManager(masterEventManager);
    alertManager.setActiveSpaceControllerManager(activeSpaceControllerManager);
  }

  /**
   * Don't trigger after two scans.
   */
  @Test
  public void testAlertManagerScanNoTrigger() {
    String uuid = "foo";

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    long initialTimestamp = 1000;
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();
    alertManager.getMasterEventListener().onSpaceControllerHeartbeat(active,
        initialTimestamp + maxHeartbeatTime - 1);
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp + maxHeartbeatTime);
    alertManager.scan();

    Mockito.verify(masterEventManager, Mockito.never()).signalSpaceControllerHeartbeatLost(active,
        maxHeartbeatTime);
  }

  /**
   * Trigger after two scans.
   */
  @Test
  public void testAlertManagerScanTrigger() {
    String uuid = "this.is.my.uuid";
    long initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp + maxHeartbeatTime + 1);
    alertManager.scan();

    Mockito.verify(masterEventManager, Mockito.times(1)).signalSpaceControllerHeartbeatLost(active,
        maxHeartbeatTime + 1);
  }

  /**
   * Don't trigger after two scans because of disconnect.
   */
  @Test
  public void testAlertManagerScanNoTriggerFromDisconnect() {
    String uuid = "this.is.my.uuid";
    long initialTimestamp = 1000;
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    alertManager.getMasterEventListener().onSpaceControllerDisconnectAttempted(active);
    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(initialTimestamp + maxHeartbeatTime + 1);
    alertManager.scan();

    Mockito.verify(masterEventManager, Mockito.never())
        .signalSpaceControllerHeartbeatLost(Mockito.eq(active), Mockito.anyLong());
  }

  /**
   * Test that an alert is raised when an event comes in about a lost heartbeat
   * and that the controller is disconnected.
   */
  @Test
  public void testConnectionLostTriggerFromMasterEvent() {
    String uuid = "this.is.my.uuid";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("foo");
    controller.setName("howdy");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    long timeSinceLastHeartbeat = 1000;
    alertManager.getMasterEventListener().onSpaceControllerHeartbeatLost(active,
        timeSinceLastHeartbeat);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1))
        .disconnectSpaceController(controller, true);

    ArgumentCaptor<SpaceControllerConnectionLostAlertEvent> argument =
        ArgumentCaptor.forClass(SpaceControllerConnectionLostAlertEvent.class);
    Mockito.verify(masterEventManager)
        .broadcastSpaceControllerConnectionLostAlertEvent(argument.capture());
    SpaceControllerConnectionLostAlertEvent event = argument.getValue();

    Assert.assertEquals(controller.getUuid(), event.controllerUuid());
    Assert.assertEquals(controller.getHostId(), event.controllerHostId());
    Assert.assertEquals(timeSinceLastHeartbeat, event.timeSinceLastHeartbeat());
  }

  /**
   * Test that an alert is raised when an event comes in about a connection
   * failure and that the controller is disconnected.
   */
  @Test
  public void testConnectionFailureFromMasterEvent() {
    String uuid = "this.is.my.uuid";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("foo");
    controller.setName("howdy");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    alertManager.addSpaceControllerWatcher(active);
    Assert.assertEquals(active,  alertManager.getSpaceControllerWatcher(uuid).getMonitored());

    long waitedTime = 1000;
    alertManager.getMasterEventListener().onSpaceControllerConnectFailed(active, waitedTime);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1))
        .disconnectSpaceController(controller, true);

    Assert.assertNull(alertManager.getSpaceControllerWatcher(uuid));

    ArgumentCaptor<SpaceControllerConnectionFailureAlertEvent> argument =
        ArgumentCaptor.forClass(SpaceControllerConnectionFailureAlertEvent.class);
    Mockito.verify(masterEventManager)
        .broadcastSpaceControllerConnectionFailureAlertEvent(argument.capture());
    SpaceControllerConnectionFailureAlertEvent event = argument.getValue();

    Assert.assertEquals(controller.getUuid(), event.controllerUuid());
    Assert.assertEquals(controller.getHostId(), event.controllerHostId());
    Assert.assertEquals(waitedTime, event.timeSinceLastHeartbeat());
  }

  @Test
  public void testEventSpaceControllerConnectAttempted() {
    String uuid = "this.is.my.uuid";
    long initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    
    Assert.assertEquals(active,  alertManager.getSpaceControllerWatcher(uuid).getMonitored());
  }

  @Test
  public void testEventSpaceControllerDisconnectAttempted() {
    String uuid = "this.is.my.uuid";
    long initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    alertManager.addSpaceControllerWatcher(active);
    Assert.assertEquals(active,  alertManager.getSpaceControllerWatcher(uuid).getMonitored());

    alertManager.getMasterEventListener().onSpaceControllerDisconnectAttempted(active);

    Assert.assertNull(alertManager.getSpaceControllerWatcher(uuid));
  }

  @Test
  public void testEventSpaceControllerShutdown() {
    String uuid = "this.is.my.uuid";
    long initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    alertManager.getMasterEventListener().onSpaceControllerShutdown(active);

    ArgumentCaptor<Runnable> argument = ArgumentCaptor.forClass(Runnable.class);
    Mockito.verify(executorService).schedule(argument.capture(), 
        Matchers.eq(StandardMasterAlertManager.CONTROLLER_SHUTDOWN_EVENT_HANDLING_DELAY), 
        Matchers.eq(TimeUnit.MILLISECONDS));
    argument.getValue().run();
    
    Mockito.verify(activeSpaceControllerManager, Mockito.times(1))
        .disconnectSpaceController(controller, false);
  }
}
