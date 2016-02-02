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

package io.smartspaces.master.server.services.internal.ros;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.master.server.services.internal.ros.MasterRosContext;
import io.smartspaces.master.server.services.internal.ros.StandardMasterRosContext;
import io.smartspaces.system.SmartSpacesEnvironment;
import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.RosMasterControllerFactory;
import org.ros.osgi.master.core.internal.BaseRosMasterController;

import com.google.common.collect.Lists;

/**
 * Test the StandardMasterRosContext.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterRosContextTest {

  private StandardMasterRosContext context;

  private TestRosMasterController rosMasterController;

  @Mock
  private RosMasterControllerFactory rosMasterControllerFactory;

  @Mock
  private RosEnvironment rosEnvironment;

  @Mock
  private NodeConfiguration nodeConfiguration;

  @Mock
  private SmartSpacesEnvironment spaceEnvironment;

  @Mock
  private Configuration systemConfiguration;

  @Mock
  private ConnectedNode masterNode;

  @Mock
  private Log log;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    rosMasterController = new TestRosMasterController();

    Mockito.when(rosMasterControllerFactory.newInternalController())
        .thenReturn(rosMasterController);

    Mockito.when(spaceEnvironment.getSystemConfiguration()).thenReturn(systemConfiguration);

    context = new StandardMasterRosContext(rosMasterControllerFactory);
    context.setLog(log);
    context.setRosEnvironment(rosEnvironment);
    context.setSpaceEnvironment(spaceEnvironment);

    Mockito.when(rosEnvironment.getPublicNodeConfigurationWithNodeName()).thenReturn(
        nodeConfiguration);

    // Fake ROSJava signaling a connection event.
    Mockito.when(
        rosEnvironment.newNode(nodeConfiguration,
            Lists.newArrayList(context.getMasterNodeListener()))).thenAnswer(
        new Answer<ConnectedNode>() {
          @Override
          public ConnectedNode answer(InvocationOnMock invocation) throws Throwable {
            context.getMasterNodeListener().onStart(masterNode);
            return masterNode;
          }
        });
  }

  /**
   * Test starting up the context.
   *
   * <p>
   * This test has a local ROS master start up at the same time.
   */
  @Test
  public void testStartupLocalRosMaster() {
    Mockito.when(
        systemConfiguration.getPropertyString(
            MasterRosContext.CONFIGURATION_NAME_ROS_MASTER_ENABLE,
            MasterRosContext.CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE)).thenReturn(
        MasterRosContext.CONFIGURATION_VALUE_MASTER_ENABLE_TRUE);

    context.startup();

    Mockito.verify(nodeConfiguration).setNodeName(
        MasterRosContext.ROS_NODENAME_smartspaces_MASTER);
    Mockito.verify(rosEnvironment).newNode(nodeConfiguration,
        Lists.newArrayList(context.getMasterNodeListener()));
    Assert.assertEquals(masterNode, context.getMasterNode());
    Assert.assertEquals(rosEnvironment, rosMasterController.getRosEnvironment());
  }

  /**
   * Test shutting down the context.
   *
   * <p>
   * This test using a local ROS master.
   */
  @Test
  public void testShutdownLocalRosMaster() {
    Mockito.when(
        systemConfiguration.getPropertyString(
            MasterRosContext.CONFIGURATION_NAME_ROS_MASTER_ENABLE,
            MasterRosContext.CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE)).thenReturn(
        MasterRosContext.CONFIGURATION_VALUE_MASTER_ENABLE_TRUE);

    context.startup();

    context.shutdown();
    Mockito.verify(masterNode).shutdown();

    // Wrong pieces in place for the node to call its listeners, so call
    // directly.
    context.getMasterNodeListener().onShutdownComplete(context.getMasterNode());
    Assert.assertTrue(rosMasterController.isShutdownCalled());
  }

  /**
   * Test starting up the context.
   *
   * <p>
   * This test has a remote ROS master.
   */
  @Test
  public void testStartupRemoteRosMaster() {
    Mockito.when(
        systemConfiguration.getPropertyString(
            MasterRosContext.CONFIGURATION_NAME_ROS_MASTER_ENABLE,
            MasterRosContext.CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE)).thenReturn(
        MasterRosContext.CONFIGURATION_VALUE_MASTER_ENABLE_FALSE);

    context.startup();

    Assert.assertNull(rosMasterController.getRosEnvironment());
    Mockito.verify(nodeConfiguration).setNodeName(
        MasterRosContext.ROS_NODENAME_smartspaces_MASTER);
    Mockito.verify(rosEnvironment).newNode(nodeConfiguration,
        Lists.newArrayList(context.getMasterNodeListener()));
    Assert.assertEquals(masterNode, context.getMasterNode());
  }

  /**
   * Test shutting down the context.
   *
   * <p>
   * This test using a remote ROS master.
   */
  @Test
  public void testShutdownRemoteRosMaster() {
    Mockito.when(
        systemConfiguration.getPropertyString(
            MasterRosContext.CONFIGURATION_NAME_ROS_MASTER_ENABLE,
            MasterRosContext.CONFIGURATION_DEFAULT_ROS_MASTER_ENABLE)).thenReturn(
        MasterRosContext.CONFIGURATION_VALUE_MASTER_ENABLE_FALSE);

    context.startup();

    context.shutdown();
    Mockito.verify(masterNode).shutdown();

    // Wrong pieces in place for the node to call its listeners, so call
    // directly.
    context.getMasterNodeListener().onShutdownComplete(context.getMasterNode());
    Assert.assertFalse(rosMasterController.isShutdownCalled());
  }

  /**
   * A ROS Master controller that merely signals startup and shutdown.
   *
   * @author Keith M. Hughes
   */
  private class TestRosMasterController extends BaseRosMasterController {

    /**
     * {@code true} if shutdown was called.
     */
    private boolean shutdownCalled = false;

    @Override
    public void startup() {
      signalRosMasterStartup();
    }

    @Override
    public void shutdown() {
      signalRosMasterShutdown();
      shutdownCalled = true;
    }

    /**
     * Was shutdown called?
     *
     * @return {@code true} if shutdown called
     */
    public boolean isShutdownCalled() {
      return shutdownCalled;
    }
  }
}
