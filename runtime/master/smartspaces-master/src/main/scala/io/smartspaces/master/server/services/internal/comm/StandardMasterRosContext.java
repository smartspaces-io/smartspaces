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

package io.smartspaces.master.server.services.internal.comm;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeListener;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.RosMasterController;
import org.ros.osgi.master.core.RosMasterControllerFactory;
import org.ros.osgi.master.core.RosMasterControllerListener;
import org.ros.osgi.master.core.internal.StandardRosMasterControllerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * A ROS context for the Smart Spaces Master.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterRosContext implements MasterRosContext {

  /**
   * The timeout default for the registration of the Smart Spaces Master ROS
   * node with the ROS master.
   */
  public static final int ROS_MASTER_REGISTRATION_TIMEOUT_DEFAULT = 10000;

  /**
   * The ROS environment the client is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The ROS Master controller to use.
   */
  private RosMasterController rosMasterController;

  /**
   * Node for this client.
   */
  private ConnectedNode masterNode;

  /**
   * Logger for this context.
   */
  private Log log;

  /**
   * The startup latch used for startup of the ROS node for the Smart Spaces
   * master.
   */
  private CountDownLatch startupLatch;

  /**
   * The timeout for waiting for a OS master registration for the Smart Spaces
   * Master ROS node.
   */
  private int rosMasterRegistrationTimeout = ROS_MASTER_REGISTRATION_TIMEOUT_DEFAULT;

  /**
   * The space environment to use.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The factory for creating ROS Master Controller instances.
   */
  private RosMasterControllerFactory rosMasterControllerFactory;

  /**
   * The master node listener.
   */
  private NodeListener masterNodeListener = new NodeListener() {
    @Override
    public void onStart(ConnectedNode connectedNode) {
      handleMasterRosNodeStartup(connectedNode);
    }

    @Override
    public void onShutdownComplete(Node node) {
      handleMasterRosNodeCompleteShutdown();
    }

    @Override
    public void onShutdown(Node node) {
      // Nothing to do
    }

    @Override
    public void onError(Node node, Throwable throwable) {
      handleMasterRosNodeError(node, throwable);
    }
  };

  /**
   * Construct a new ROS context.
   */
  public StandardMasterRosContext() {
    this(new StandardRosMasterControllerFactory());
  }

  /**
   * Construct a new ROS context.
   *
   * @param rosMasterControllerFactory
   *          the factory for creating ROS Master Controller instances
   */
  @VisibleForTesting
  StandardMasterRosContext(RosMasterControllerFactory rosMasterControllerFactory) {
    this.rosMasterControllerFactory = rosMasterControllerFactory;
  }

  @Override
  public void startup() {
    log.info("Starting up the Smart Spaces Master ROS context");

    startupLatch = new CountDownLatch(1);

    if (spaceEnvironment.getSystemConfiguration().getPropertyBoolean(
        CONFIGURATION_NAME_ROS_MASTER_ENABLE, CONFIGURATION_VALUE_DEFAULT_ROS_MASTER_ENABLE)) {
      startupRosMasterController();
    }

    log.info("The Smart Spaces Master ROS context is started up");
  }

  @Override
  public void shutdown() {
    log.info("Shutting down the Smart Spaces Master ROS context");
    if (masterNode != null) {
      masterNode.shutdown();
    }

    if (rosMasterController != null) {
      rosMasterController.shutdown();
    }
  }

  /**
   * Get the ROS node for the Smart Spaces Master.
   *
   * @return the ROS node for the Smart Spaces Master
   */
  @Override
  public ConnectedNode getMasterNode() {
    if (masterNode != null) {
      return masterNode;
    } else {
      throw SimpleSmartSpacesException
          .newFormattedException("The Smart Spaces Master is not connected to a ROS Master");
    }
  }

  @Override
  public RosEnvironment getRosEnvironment() {
    return rosEnvironment;
  }

  /**
   * Start the ROS master.
   */
  private void startupRosMasterController() {
    log.info("Starting up the Smart Spaces internal ROS Master");

    rosMasterController = rosMasterControllerFactory.newInternalController();
    rosMasterController.setRosEnvironment(rosEnvironment);

    rosMasterController.addListener(new RosMasterControllerListener() {
      @Override
      public void onRosMasterStartup() {
        log.info("Smart Spaces internal ROS Master started at "
            + rosMasterController.getRosEnvironment().getMasterUri());
        // connectToRosMaster();
      }

      @Override
      public void onRosMasterShutdown() {
        // Don't care
      }
    });

    rosMasterController.startup();
  }

  /**
   * Handle the startup of the ROS node for the Smart Spaces Master.
   *
   * @param masterNode
   *          the Smart Spaces Master's ROS node
   */
  private void handleMasterRosNodeStartup(ConnectedNode masterNode) {
    this.masterNode = masterNode;
    startupLatch.countDown();
  }

  /**
   * Handle any operations after the complete shutdown of the ROS node for the
   * Smart Spaces Master.
   */
  private void handleMasterRosNodeCompleteShutdown() {
    log.info(String.format("Got ROS node complete shutdown for Smart Spaces master node %s",
        masterNode.getName()));

    if (rosMasterController != null) {
      rosMasterController.shutdown();
    }
  }

  /**
   * Handle an error in the Smart Spaces Master's ROS node.
   *
   * @param node
   *          the master's ROS node
   * @param throwable
   *          the error
   */
  private void handleMasterRosNodeError(Node node, Throwable throwable) {
    log.error(String.format("Got ROS node error for Smart Spaces master node %s", node.getName()),
        throwable);
  }

  /**
   * Set the ROS environment for the context.
   *
   * @param rosEnvironment
   *          the ROS Environment
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Set the log for the context.
   *
   * @param log
   *          the log
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Get the Smart Spaces Master's ROS node listener.
   *
   * @return the Smart Spaces Master's ROS node listener
   */
  @VisibleForTesting
  NodeListener getMasterNodeListener() {
    return masterNodeListener;
  }
}
