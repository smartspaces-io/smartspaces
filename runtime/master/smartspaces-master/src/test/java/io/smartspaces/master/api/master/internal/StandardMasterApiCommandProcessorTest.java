/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.api.master.MasterApiActivityManager;
import io.smartspaces.master.api.master.MasterApiAutomationManager;
import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.api.master.MasterApiSpaceControllerManager;
import io.smartspaces.master.server.services.ExtensionManager;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.messaging.MessageSender;
import io.smartspaces.time.provider.TimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;

/**
 * Tests for the {@link StandardMasterApiCommandProcessor}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiCommandProcessorTest {

  @Mock
  private MasterApiActivityManager masterApiActivityManager;

  @Mock
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  @Mock
  private MasterApiAutomationManager masterApiAutomationManager;

  @Mock
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private TimeProvider timeProvider;

  @Mock
  private MessageSender<Map<String, Object>> allClientsMessageSender;

  @Mock
  private ExtendedLog log;

  private StandardMasterApiCommandProcessor commandProcessor;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(10000000l);

    commandProcessor = new StandardMasterApiCommandProcessor(masterApiActivityManager,
        masterApiSpaceControllerManager, masterApiAutomationManager, masterApiMasterSupportManager,
        extensionManager, timeProvider, allClientsMessageSender, log);
  }

  /**
   * Test the manager getting an activity state change from the master event
   * bus.
   */
  @Test
  public void testActivityStateChange() {
    String uuid = "foo";

    LiveActivity liveActivity = Mockito.mock(LiveActivity.class);
    Mockito.when(liveActivity.getUuid()).thenReturn(uuid);
    ActiveLiveActivity activeLiveActivity = new ActiveLiveActivity(null, liveActivity, null, null);

    commandProcessor.sendLiveActivityStateChangeMessage(activeLiveActivity,
        ActivityState.STARTUP_ATTEMPT, ActivityState.RUNNING);

    @SuppressWarnings("unchecked")
    Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>) (Class) Map.class;
    ArgumentCaptor<Map<String, Object>> argumentCaptor = ArgumentCaptor.forClass(mapClass);

    Mockito.verify(allClientsMessageSender).sendMessage(argumentCaptor.capture());
  }
}
