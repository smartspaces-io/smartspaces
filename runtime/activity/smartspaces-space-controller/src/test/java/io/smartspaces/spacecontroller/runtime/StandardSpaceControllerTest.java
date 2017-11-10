/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.spacecontroller.runtime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResult;
import io.smartspaces.liveactivity.runtime.LiveActivityRuntime;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.spacecontroller.SpaceController;
import io.smartspaces.spacecontroller.runtime.configuration.SpaceControllerConfigurationManager;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesFilesystem;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;
import io.smartspaces.tasks.ImmediateRunSequentialTaskQueue;
import io.smartspaces.time.provider.TimeProvider;
import io.smartspaces.util.io.FileSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Unit tests for the {@link StandardSpaceController}.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceControllerTest {
  private StandardSpaceController controller;

  private ScheduledExecutorService executorService;

  private TimeProvider timeProvider;
  private SpaceControllerCommunicator controllerCommunicator;
  private SmartSpacesSystemControl spaceSystemControl;
  private SpaceControllerInfoPersister controllerInfoPersister;
  private SmartSpacesEnvironment spaceEnvironment;
  private ServiceRegistry serviceRegistry;
  private SpaceControllerDataBundleManager dataBundleManager;
  private SmartSpacesFilesystem systemFilesystem;
  private SpaceControllerConfigurationManager spaceControllerConfigurationManager;
  private LiveActivityRuntime liveActivityRuntime;
  private SpaceControllerActivityInstallationManager spaceControllerActivityInstallManager;

  private Configuration systemConfiguration;

  private FileSupport fileSupport;

  private ExtendedLog log;

  @Before
  public void setup() {
    log = mock(ExtendedLog.class);

    executorService = new DefaultScheduledExecutorService();

    controllerCommunicator = mock(SpaceControllerCommunicator.class);
    spaceSystemControl = mock(SmartSpacesSystemControl.class);
    controllerInfoPersister = mock(SpaceControllerInfoPersister.class);
    systemConfiguration = SimpleConfiguration.newConfiguration();
    dataBundleManager = mock(StandardSpaceControllerDataBundleManager.class);
    spaceControllerConfigurationManager = mock(SpaceControllerConfigurationManager.class);
    spaceControllerActivityInstallManager = mock(SpaceControllerActivityInstallationManager.class);

    systemFilesystem = mock(SmartSpacesFilesystem.class);

    timeProvider = mock(TimeProvider.class);

    liveActivityRuntime = mock(LiveActivityRuntime.class);

    spaceEnvironment = mock(SmartSpacesEnvironment.class);
    when(spaceEnvironment.getLog()).thenReturn(log);
    when(spaceEnvironment.getSystemConfiguration()).thenReturn(systemConfiguration);
    when(spaceEnvironment.getExecutorService()).thenReturn(executorService);
    when(spaceEnvironment.getFilesystem()).thenReturn(systemFilesystem);
    when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    serviceRegistry = mock(ServiceRegistry.class);
    when(spaceEnvironment.getServiceRegistry()).thenReturn(serviceRegistry);

    systemConfiguration.setProperty(SpaceController.CONFIGURATION_NAME_CONTROLLER_UUID, "abc123");
    systemConfiguration.setProperty(SpaceController.CONFIGURATION_NAME_CONTROLLER_NAME, "testcontroller");
    systemConfiguration.setProperty(SpaceController.CONFIGURATION_NAME_CONTROLLER_DESCRIPTION, "yipee");
    systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_HOSTID, "gloop");
    systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_NAME, "zorp");
   systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_CONTAINER_FILE_CONTROLLABLE,
        "false");

    controller =
        new StandardSpaceController(spaceControllerActivityInstallManager, null,
            controllerCommunicator, controllerInfoPersister, spaceSystemControl, dataBundleManager,
            spaceControllerConfigurationManager, liveActivityRuntime,
            new ImmediateRunSequentialTaskQueue(), spaceEnvironment);

    fileSupport = mock(FileSupport.class);
    controller.setFileSupport(fileSupport);

    controller.startup();
  }

  @After
  public void cleanup() {
    controller.shutdown();
    executorService.shutdown();
  }

  @Test
  public void testSetup() {
    verify(liveActivityRuntime).setLiveActivityStatusPublisher(controller);
  }

  /**
   * Test cleaning the permanent data directory.
   */
  @Test
  public void testCleanPermanentDataDir() {
    File systemDatadir = new File("permanent");

    when(systemFilesystem.getDataDirectory()).thenReturn(systemDatadir);

    controller.cleanControllerPermanentData();

    verify(fileSupport, times(1)).deleteDirectoryContents(systemDatadir);
  }

  /**
   * Test cleaning the permanent data directory.
   */
  @Test
  public void testCleanTempDataDir() {
    File systemDatadir = new File("temp");

    when(systemFilesystem.getTempDirectory()).thenReturn(systemDatadir);

    controller.cleanControllerTempData();

    verify(fileSupport, times(1)).deleteDirectoryContents(systemDatadir);
  }

  /**
   * Test cleaning a live activity's temp dir.
   */
  @Test
  public void testCleanLiveActivityTempDataDir() {
    String uuid = "foo";

    controller.cleanLiveActivityTmpData(uuid);

    verify(liveActivityRuntime, times(1)).cleanLiveActivityTmpData(uuid);
  }

  /**
   * Test cleaning a live activity's permanent dir.
   */
  @Test
  public void testCleanLiveActivityPermanentDataDir() {
    String uuid = "foo";

    controller.cleanLiveActivityPermanentData(uuid);

    verify(liveActivityRuntime, times(1)).cleanLiveActivityPermanentData(uuid);
  }

  /**
   * Test starting an activity.
   */
  @Test
  public void testLiveActivityStartup() {
    String uuid = "foo";

    controller.startupLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).startupLiveActivity(uuid);
  }

  /**
   * Test shutting down an activity.
   */
  @Test
  public void testLiveActivityShutdown() {
    String uuid = "foo";

    controller.shutdownLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).shutdownLiveActivity(uuid);
  }

  /**
   * Test activating an activity.
   */
  @Test
  public void testLiveActivityActivate() {
    String uuid = "foo";

    controller.activateLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).activateLiveActivity(uuid);
  }

  /**
   * Test deactivating an activity.
   */
  @Test
  public void testLiveActivityDeactivate() {
    String uuid = "foo";

    controller.deactivateLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).deactivateLiveActivity(uuid);
  }

  /**
   * Test getting the status of an activity.
   */
  @Test
  public void testLiveActivityStatus() {
    String uuid = "foo";

    controller.statusLiveActivity(uuid);

    verify(liveActivityRuntime, times(1)).statusLiveActivity(uuid);
  }

  /**
   * Test configuring an activity.
   */
  @Test
  public void testLiveActivityconfigure() {
    Map<String, String> configuration = new HashMap<>();
    configuration.put("a", "b");

    String uuid = "foo";

    controller.configureLiveActivity(uuid, configuration);

    verify(liveActivityRuntime, times(1)).configureLiveActivity(uuid, configuration);
  }

  /**
   * Test deleting a live activity when it is possible to delete.
   */
  @Test
  public void testLiveActivityDelete() {
    String uuid = "1.2.3.4";

    when(liveActivityRuntime.isLiveActivityRunning(uuid)).thenReturn(false);

    LiveActivityDeleteRequest request = new LiveActivityDeleteRequest(uuid, "foo", "1.2.3", false);

    controller.deleteLiveActivity(request);

    verify(spaceControllerActivityInstallManager).handleDeleteRequest(request);
  }

  /**
   * Test deleting a live activity when it is running.
   */
  @Test
  public void testLiveActivityDeleteWhenRunning() {
    String uuid = "1.2.3.4";

    when(liveActivityRuntime.isLiveActivityRunning(uuid)).thenReturn(true);

    LiveActivityDeleteRequest request = new LiveActivityDeleteRequest(uuid, "foo", "1.2.3", false);

    LiveActivityDeleteResult response = controller.deleteLiveActivity(request);

    assertEquals(uuid, response.getUuid());
    assertEquals(LiveActivityDeleteResult.LiveActivityDeleteStatus.FAILURE, response.getStatus());

    verify(spaceControllerActivityInstallManager, times(0)).handleDeleteRequest(request);
  }
}
