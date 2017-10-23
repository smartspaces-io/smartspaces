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
import io.smartspaces.container.control.message.StandardMasterSpaceControllerCodec;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeRequest;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeRequestOperation;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeStatus;
import io.smartspaces.container.control.message.common.ConfigurationParameterRequest;
import io.smartspaces.container.control.message.common.ConfigurationParameterRequest.ConfigurationParameterRequestOperation;
import io.smartspaces.container.control.message.common.ConfigurationRequest;
import io.smartspaces.container.control.message.container.ControllerFullStatus;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.SpaceControllerConfiguration;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.server.services.RemoteSpaceControllerClient;
import io.smartspaces.master.server.services.RemoteSpaceControllerClientListener;
import io.smartspaces.master.server.services.internal.DataBundleState;
import io.smartspaces.master.server.services.internal.MasterDataBundleManager;
import io.smartspaces.master.server.services.internal.RemoteSpaceControllerClientListenerCollection;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.master.spacecontroller.client.RemoteActivityDeploymentManager;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;
import io.smartspaces.spacecontroller.SpaceControllerState;
import io.smartspaces.spacecontroller.SpaceControllerStatus;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RemoteSpaceControllerClient} which uses TCP.
 *
 * @author Keith M. Hughes
 */
public class SimpleTcpRemoteSpaceControllerClient implements RemoteSpaceControllerClient {

  /**
   * Default number of milliseconds to wait for a controller connection.
   */
  public static final long CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT = 5000;

  /**
   * Number of milliseconds to wait for a controller connect.
   */
  private final long controllerConnectionTimeWait = CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT;

  /**
   * Map of controller communicators keyed by the name of the remote ROS node.
   */
  private final Map<String, SpaceControllerCommunicator> controllerCommunicators = new HashMap<>();

  /**
   * The message codec for controller/master communications.
   */
  private StandardMasterSpaceControllerCodec messageCodec =
      new StandardMasterSpaceControllerCodec();

  /**
   * The TCP client service for connections to space controllers.
   */
  private TcpClientNetworkCommunicationEndpointService tcpClientService;

  /**
   * The space environment to run under.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Helps with listeners for activity events.
   */
  private RemoteSpaceControllerClientListenerCollection remoteControllerClientListeners;

  /**
   * Data bundle manager for this controller.
   */
  private MasterDataBundleManager masterDataBundleManager;

  /**
   * The remote activity installation manager.
   */
  private RemoteActivityDeploymentManager remoteActivityDeploymentManager;

  /**
   * Logger for the controller.
   */
  private ExtendedLog log;

  @Override
  public void startup() {
    log.info("Starting up remote space controller client");

    tcpClientService = spaceEnvironment.getServiceRegistry()
        .getRequiredService(TcpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    remoteControllerClientListeners = new RemoteSpaceControllerClientListenerCollection(log);

    masterDataBundleManager.startup();
  }

  @Override
  public void shutdown() {
    for (SpaceControllerCommunicator communicator : controllerCommunicators.values()) {
      communicator.shutdown();
    }
    controllerCommunicators.clear();

    remoteControllerClientListeners.clear();

    masterDataBundleManager.shutdown();
  }

  @Override
  public void connectToSpaceController(ActiveSpaceController controller) {
    getCommunicator(controller, true);
  }

  @Override
  public void disconnectFromSpaceController(ActiveSpaceController controller) {
    shutdownCommunicator(controller);
  }

  @Override
  public void requestSpaceControllerShutdown(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER);

    // Heartbeat is shut down once controller acknowledges shutdown.
  }

  @Override
  public void requestSpaceControllerHardRestart(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESTART_HARD_CONTROLLER);

    // Heartbeat is shut down once controller acknowledges shutdown.
  }

  @Override
  public void requestSpaceControllerSoftRestart(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESTART_SOFT_CONTROLLER);

    // Heartbeat is shut down once controller acknowledges shutdown.
  }

  @Override
  public void requestSpaceControllerStatus(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_STATUS);
  }

  @Override
  public void shutdownSpacecontrollerAllActivities(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES);
  }

  @Override
  public void configureSpaceController(ActiveSpaceController controller) {
    ConfigurationRequest request = new ConfigurationRequest();
    SpaceControllerConfiguration configuration = controller.spaceController().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter = new ConfigurationParameterRequest();
        newParameter.setOperation(ConfigurationParameterRequestOperation.ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        request.addParameter(newParameter);
      }
    }

    sendSpaceControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CONFIGURE, request);
  }

  @Override
  public void deployLiveActivity(ActiveLiveActivity liveActivity,
      LiveActivityDeploymentRequest request) {
    sendSpaceControllerRequest(liveActivity.getActiveController(),
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY, request);
  }

  @Override
  public void deleteLiveActivity(ActiveLiveActivity liveActivity,
      LiveActivityDeleteRequest request) {
    sendSpaceControllerRequest(liveActivity.getActiveController(),
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY, request);
  }

  @Override
  public void querySpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentQueryRequest query) {
    sendSpaceControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESOURCE_QUERY, query);
  }

  @Override
  public void commitSpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentCommitRequest request) {
    sendSpaceControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESOURCE_COMMIT, request);
  }

  @Override
  public void cleanSpaceControllerTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_TMP);
  }

  @Override
  public void cleanSpaceControllerPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT);
  }

  @Override
  public void cleanSpaceControllerActivitiesTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES);
  }

  @Override
  public void cleanSpaceControllerActivitiesPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES);
  }

  @Override
  public void captureSpaceControllerDataBundle(ActiveSpaceController controller) {
    masterDataBundleManager.captureControllerDataBundle(controller);
  }

  @Override
  public void restoreSpaceControllerDataBundle(ActiveSpaceController controller) {
    masterDataBundleManager.restoreControllerDataBundle(controller);
  }

  @Override
  public void fullConfigureLiveActivity(ActiveLiveActivity activity) {
    ConfigurationRequest request = new ConfigurationRequest();
    ActivityConfiguration configuration = activity.getLiveActivity().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter = new ConfigurationParameterRequest();
        newParameter.setOperation(ConfigurationParameterRequestOperation.ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        request.addParameter(newParameter);
      }
    }

    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CONFIGURE, request);
  }

  @Override
  public void startupLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_STARTUP, null);
  }

  @Override
  public void activateLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_ACTIVATE, null);
  }

  @Override
  public void deactivateLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_DEACTIVATE, null);
  }

  @Override
  public void shutdownLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_SHUTDOWN, null);
  }

  @Override
  public void statusLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_STATUS, null);
  }

  @Override
  public void cleanLiveActivityPermanentData(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CLEAN_DATA_PERMANENT, null);
  }

  @Override
  public void cleanLiveActivityTempData(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CLEAN_DATA_TMP, null);
  }

  @Override
  public RemoteSpaceControllerClientListenerCollection registerRemoteActivityDeploymentManager(
      RemoteActivityDeploymentManager remoteActivityDeploymentManager) {
    this.remoteActivityDeploymentManager = remoteActivityDeploymentManager;

    return remoteControllerClientListeners;
  }

  /**
   * Send a controller request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param controller
   *          the controller the request is being sent to
   * @param operation
   *          the operation requested
   */
  private void sendControllerRequest(ActiveSpaceController controller, String operation) {
    sendSpaceControllerRequest(controller, operation, null);
  }

  /**
   * Send a controller request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param controller
   *          the controller the request is being sent to
   * @param operation
   *          the operation requested
   * @param payload
   *          any data to be sent with the request (can be {@code null})
   */
  void sendSpaceControllerRequest(ActiveSpaceController controller, String operation,
      Object payload) {
    String request = messageCodec
        .encodeFinalMessage(messageCodec.encodeBaseControllerRequestMessage(operation, payload));

    SpaceControllerCommunicator communicator = getCommunicator(controller, true);

    communicator.sendControllerRequest(request);
  }

  /**
   * Send an activity runtime request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param activity
   *          the activity the request is being sent to
   * @param operation
   *          the operation requested
   * @param payload
   *          the data to send
   */
  private void sendLiveActivityRuntimeRequest(ActiveLiveActivity activity, String operation,
      ConfigurationRequest configurationRequest) {
    LiveActivityRuntimeRequest request = new LiveActivityRuntimeRequest(
        activity.getLiveActivity().getUuid(), operation, false, configurationRequest);

    sendSpaceControllerRequest(activity.getActiveController(),
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST,
        request);
  }

  /**
   * Handle controller status updates.
   *
   * @param spaceController
   *          the space controller that sent the status update
   * @param status
   *          The status update.
   */
  private void handleRemoteControllerStatusUpdate(ActiveSpaceController spaceController, String statusMessage) {
    Map<String, Object> statusObject = messageCodec.parseMessage(statusMessage);

    String statusType = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE);
    switch (statusType) {
      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_HEARTBEAT:
        handleControllerHeartbeat(spaceController, statusObject);

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTROLLER_FULL_STATUS:
        // A full status request will also be treated as a heartbeat event
        // since the controller will only respond if it is alive.
        handleControllerHeartbeat(spaceController, statusObject);

        ControllerFullStatus fullStatus = messageCodec.decodeControllerFullStatus(statusObject);

        List<LiveActivityRuntimeStatus> liveActivityStatuses = fullStatus.getLiveActivityStatuses();
        if (log.isInfoEnabled()) {
          String controllerUuid = (String) statusObject
              .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID);
          log.info(String.format("Received space controller full status %s, %d activities",
              controllerUuid, liveActivityStatuses.size()));
        }
        for (LiveActivityRuntimeStatus liveActivityStatus : liveActivityStatuses) {
          if (log.isInfoEnabled()) {
            log.info(String.format("\tActivity %s, %s\n", liveActivityStatus.getUuid(),
                liveActivityStatus.getStatus()));
          }
          handleRemoteLiveActivityStatusUpdate(liveActivityStatus);
        }

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_LIVE_ACTIVITY_RUNTIME_STATUS:
        LiveActivityRuntimeStatus liveActivityStatus =
            messageCodec.decodeLiveActivityRuntimeStatus(statusObject);
        if (log.isDebugEnabled()) {
          log.debug(String.format("Activity status %s, %s\n", liveActivityStatus.getUuid(),
              liveActivityStatus.getStatus()));
        }
        handleRemoteLiveActivityStatusUpdate(liveActivityStatus);

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_INSTALL:

        LiveActivityDeploymentResponse deployResponse =
            messageCodec.decodeLiveActivityDeploymentResponse(statusObject);
        remoteActivityDeploymentManager.handleLiveDeployResult(deployResponse);

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_DELETE:
        LiveActivityDeleteResponse liveActivityDeleteResponse =
            messageCodec.decodeLiveActivityDeleteResponse(statusObject);
        remoteControllerClientListeners.signalActivityDelete(liveActivityDeleteResponse.getUuid(),
            liveActivityDeleteResponse);

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_DATA_CAPTURE:
        handleControllerStatusDataCapture(spaceController, statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_DATA_RESTORE:

        handleControllerStatusDataRestore(spaceController, statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_QUERY:
        handleContainerResourceQueryResponse(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_COMMIT:
        handleContainerResourceCommitResponse(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_SHUTDOWN:
        handleSpaceControllerShutdown(spaceController, statusObject);
        break;

      default:
        log.warn(String.format("Unknown status type %s, for controller %s", statusType, statusObject
            .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID)));
    }
  }

  /**
   * Handle a data restore from a controller status message.
   * 
   * @param spaceController
   *          the space controller
   * @param statusObject
   *          the controller status object
   */
  private void handleControllerStatusDataRestore(ActiveSpaceController spaceController, Map<String, Object> statusObject) {
    String statusCode = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_CODE);
    log.info("Received data restore response " + statusCode);
    DataBundleState restoreState = SpaceControllerStatus.isSuccessDescription(statusCode)
        ? DataBundleState.RESTORE_RECEIVED : DataBundleState.RESTORE_ERROR;
    remoteControllerClientListeners.signalDataBundleState(spaceController, restoreState);
  }

  /**
   * Handle a data capture from a controller status message.
   * 
   * @param spaceController
   *          the space controller
   * @param statusObject
   *          the controller status object
   */
  private void handleControllerStatusDataCapture(ActiveSpaceController spaceController, Map<String, Object> statusObject) {
    String statusCode = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_CODE);
    log.info("Received data capture response " + statusCode);
    DataBundleState captureState = SpaceControllerStatus.isSuccessDescription(statusCode)
        ? DataBundleState.CAPTURE_RECEIVED : DataBundleState.CAPTURE_ERROR;
    remoteControllerClientListeners.signalDataBundleState(spaceController, captureState);
  }

  /**
   * Handle a controller connect.
   * 
   * <p>
   * The event is sent in a separate thread.
   *
   * @param spaceController
   *          the space controller that has connected
   */
  private void handleControllerConnect(ActiveSpaceController spaceController) {
    long timestamp = System.currentTimeMillis();

    spaceEnvironment.getExecutorService().submit(new Runnable() {

      @Override
      public void run() {
        remoteControllerClientListeners.signalSpaceControllerConnect(spaceController, timestamp);
      }
    });
  }

  /**
   * Handle a controller disconnect.
   * 
   * <p>
   * The event is sent in a separate thread.
   *
   * @param spaceController
   *          the space controller that has disconnected
   */
  private void handleControllerDisconnect(ActiveSpaceController spaceController) {
    long timestamp = System.currentTimeMillis();

    spaceEnvironment.getExecutorService().submit(new Runnable() {

      @Override
      public void run() {
        remoteControllerClientListeners.signalSpaceControllerDisconnect(spaceController, timestamp);
      }
    });
  }

  /**
   * Handle a space controller heartbeat message.
   *
   * @param spaceController
   *          the space controller that sent the heartbeat
   * @param status
   *          the status message for the heartbeat
   */
  private void handleControllerHeartbeat(ActiveSpaceController spaceController, Map<String, Object> status) {
    long timestamp = System.currentTimeMillis();
    remoteControllerClientListeners.signalSpaceControllerHeartbeat(spaceController, timestamp);
  }

  /**
   * Handle activity status updates.
   *
   * @param status
   *          the status update
   */
  private void handleRemoteLiveActivityStatusUpdate(LiveActivityRuntimeStatus status) {
    remoteControllerClientListeners.signalActivityStateChange(status.getUuid(), status.getStatus(),
        status.getStatusDetail());
  }

  /**
   * Handle a container resource deployment query response.
   *
   * @param statusObject
   *          the status object containing the response
   */
  private void handleContainerResourceQueryResponse(Map<String, Object> statusObject) {
    ContainerResourceDeploymentQueryResponse response =
        messageCodec.decodeContainerResourceDeploymentQueryResponse(statusObject);
    log.info(
        String.format("Got resource deployment query response for transaction ID %s with status %s",
            response.getTransactionId(), response.getStatus()));

    remoteActivityDeploymentManager.handleResourceDeploymentQueryResponse(response);
  }

  /**
   * Handle a container resource deployment commit response.
   *
   * @param statusObject
   *          the status object containing the response
   */
  private void handleContainerResourceCommitResponse(Map<String, Object> statusObject) {
    ContainerResourceDeploymentCommitResponse response =
        messageCodec.decodeContainerResourceDeploymentCommitResponse(statusObject);
    log.info(String.format(
        "Got resource deployment commit response for transaction ID %s with status %s",
        response.getTransactionId(), response.getStatus()));

    remoteActivityDeploymentManager.handleResourceDeploymentCommitResponse(response);
  }

  /**
   * Handle the shutdown of a space controller.
   * 
   * @param spaceController
   *          the space controller
   * @param status
   *          the controller status
   */
  private void handleSpaceControllerShutdown(ActiveSpaceController spaceController, Map<String, Object> statusObject) {
    remoteControllerClientListeners.signalSpaceControllerShutdown(spaceController);
  }

  @Override
  public RemoteSpaceControllerClientListenerCollection getRemoteControllerClientListeners() {
    return remoteControllerClientListeners;
  }

  @Override
  public void addRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.addListener(listener);
  }

  @Override
  public void
      removeRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.removeListener(listener);
  }

  /**
   * Get the communicator for a given controller.
   *
   * @param controller
   *          The controller
   * @param create
   *          {@code true} is a communicator should be created if there is none
   *          associated with the controller.
   *
   * @return the communicator for the controller, {@code null} if there is none
   *         and creation wasn't specified
   */
  private SpaceControllerCommunicator getCommunicator(ActiveSpaceController controller,
      boolean create) {
    String controllerHostName = controller.spaceController().getHostId();
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(controllerHostName);
      spaceEnvironment.getLog().info(communicator);

      if (communicator == null) {
        controller.setState(SpaceControllerState.CONNECT_ATTEMPT);
        communicator = new SpaceControllerCommunicator(controller);
        communicator.startup();
        controllerCommunicators.put(controllerHostName, communicator);

      }

      return communicator;
    }
  }

  /**
   * Shutdown the communicator for a given controller.
   *
   * <p>
   * The communicator is then removed from the communicator map.
   *
   * @param controller
   *          The controller
   */
  private void shutdownCommunicator(ActiveSpaceController controller) {
    String hostId = controller.spaceController().getHostId();
    SpaceControllerCommunicator communicator = null;
    synchronized (controllerCommunicators) {
      communicator = controllerCommunicators.remove(hostId);
    }

    if (communicator != null) {
      communicator.shutdown();
      log.formatInfo("Communicator for controller %s shutdown and removed",
          controller.spaceController().getUuid());
    }
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
   * Set the logger.
   *
   * @param log
   *          the log to set
   */
  public void setLog(ExtendedLog log) {
    this.log = log;
  }

  /**
   * Set the master's data bundle manager.
   *
   * @param masterDataBundleManager
   *          the data bundle manager to use, can be {@code null}
   */
  public void setMasterDataBundleManager(MasterDataBundleManager masterDataBundleManager) {
    this.masterDataBundleManager = masterDataBundleManager;
  }

  /**
   * Directly communicates with a space controller.
   *
   * @author Keith M. Hughes
   */
  public class SpaceControllerCommunicator {

    /**
     * The space controller we are the communicator for.
     */
    private final ActiveSpaceController spaceController;

    /**
     * A latch for connection to the controller admin server.
     */
    private CountDownLatch connectionLatch = new CountDownLatch(1);

    /**
     * The client to the controller.
     */
    private TcpClientNetworkCommunicationEndpoint<String> controllerClient;

    /**
     * Construct a communicator.
     *
     * @param spaceController
     *          the space controller being communicated with
     */
    public SpaceControllerCommunicator(ActiveSpaceController spaceController) {
      this.spaceController = spaceController;
    }

    /**
     * Start the communicator up.
     *
     * @param controllerHostName
     *          the remote node
     */
    public void startup() {
      SpaceController controller = spaceController.spaceController();
      controllerClient = tcpClientService.newStringClient(
          StandardMasterSpaceControllerCodec.DELIMITERS, StandardMasterSpaceControllerCodec.CHARSET,
          controller.getHostName(), controller.getHostControlPort(), log);
      controllerClient.addListener(new TcpClientNetworkCommunicationEndpointListener<String>() {

        @Override
        public void
            onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
          log.formatInfo("Controller client connected %s", spaceController.getDisplayName());
          connectionLatch.countDown();
          
          handleControllerConnect(spaceController);
        }

        @Override
        public void
            onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
          log.formatInfo("Controller client disconnected %s", spaceController.getDisplayName());
          
          handleControllerDisconnect(spaceController);
       }

        @Override
        public void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<String> endpoint,
            String message) {
          handleRemoteControllerStatusUpdate(spaceController, message);
        }
      });
      controllerClient.startup();

      remoteControllerClientListeners.signalSpaceControllerConnectAttempt(spaceController);
    }

    /**
     * Shut the communicator down.
     */
    public void shutdown() {
      controllerClient.shutdown();

      remoteControllerClientListeners.signalSpaceControllerDisconnectAttempt(spaceController);
    }

    /**
     * Send a request to a controller.
     *
     * @param request
     *          the request to send
     */
    public void sendControllerRequest(String request) {
      try {
        if (connectionLatch.await(controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
          controllerClient.sendMessage(request);
        } else {
          remoteControllerClientListeners.signalSpaceControllerConnectFailed(spaceController,
              controllerConnectionTimeWait);

          throw SimpleSmartSpacesException.newFormattedException(
              "No connection to space controller in %d milliseconds", controllerConnectionTimeWait);
        }
      } catch (InterruptedException e) {
        // TODO(keith): Decide what to do.
        log.warn("Controller request interrupted");
      }
    }
  }
}
