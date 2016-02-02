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
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.master.event.MasterEventManager;
import io.smartspaces.master.server.services.ActiveSpaceController;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.MasterAlertManager;
import io.smartspaces.master.server.services.internal.StandardMasterAlertManager;
import io.smartspaces.service.alert.AlertService;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.time.SettableTimeProvider;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Unit tests for {@link StandardMasterAlertManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterAlertManagerTest {

  private StandardMasterAlertManager alertManager;

  private SmartSpacesEnvironment spaceEnvironment;

  private SettableTimeProvider timeProvider;

  private AlertService alertService;

  private MasterEventManager masterEventManager;

  private ActiveSpaceControllerManager activeSpaceControllerManager;

  private ScheduledExecutorService executorService;

  private Log log;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);
    timeProvider = new SettableTimeProvider();
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    executorService = Mockito.mock(ScheduledExecutorService.class);
    when(spaceEnvironment.getExecutorService()).thenReturn(executorService);

    log = Mockito.mock(Log.class);
    when(spaceEnvironment.getLog()).thenReturn(log);

    masterEventManager = Mockito.mock(MasterEventManager.class);

    activeSpaceControllerManager = Mockito.mock(ActiveSpaceControllerManager.class);

    alertService = Mockito.mock(AlertService.class);
    alertManager = new StandardMasterAlertManager();
    alertManager.setSpaceEnvironment(spaceEnvironment);
    alertManager.setAlertService(alertService);
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

    int initialTimestamp = 1000;
    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();
    alertManager.getMasterEventListener().onSpaceControllerHeartbeat(active,
        initialTimestamp + maxHeartbeatTime - 1);
    timeProvider.setCurrentTime(initialTimestamp + maxHeartbeatTime);
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
    int initialTimestamp = 1000;

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("hostess");
    controller.setName("NumeroUno");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();

    timeProvider.setCurrentTime(initialTimestamp + maxHeartbeatTime + 1);
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
    int initialTimestamp = 1000;
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    timeProvider.setCurrentTime(initialTimestamp);
    alertManager.getMasterEventListener().onSpaceControllerConnectAttempted(active);
    alertManager.scan();

    alertManager.getMasterEventListener().onSpaceControllerDisconnectAttempted(active);
    long maxHeartbeatTime = alertManager.getSpaceControllerHeartbeatTime();

    timeProvider.setCurrentTime(initialTimestamp + maxHeartbeatTime + 1);
    alertManager.scan();

    Mockito.verify(masterEventManager, Mockito.never()).signalSpaceControllerHeartbeatLost(
        Mockito.eq(active), Mockito.anyLong());
  }

  /**
   * Test that an alert is raised when an event comes in about a lost heartbeat
   * and that the controller is disconnected.
   */
  @Test
  public void testAlertManagerTriggerFromMasterEvent() {
    String uuid = "this.is.my.uuid";
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setHostId("foo");
    controller.setName("howdy");

    ActiveSpaceController active = new ActiveSpaceController(controller, timeProvider);

    alertManager.getMasterEventListener().onSpaceControllerHeartbeatLost(active, 1000);

    Mockito.verify(activeSpaceControllerManager, Mockito.times(1)).disconnectSpaceController(
        controller, true);

    ArgumentCaptor<Runnable> argument = ArgumentCaptor.forClass(Runnable.class);
    Mockito.verify(executorService).execute(argument.capture());
    argument.getValue().run();

    Mockito.verify(alertService, Mockito.times(1)).raiseAlert(
        Mockito.eq(MasterAlertManager.ALERT_TYPE_CONTROLLER_TIMEOUT), Mockito.eq(uuid),
        Mockito.anyString());
  }
}
