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

package io.smartspaces.master.server.services.internal.ros;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.ros.RosLiveActivityDeleteMessageTranslator;
import io.smartspaces.container.control.message.activity.ros.RosLiveActivityDeploymentMessageTranslator;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.container.control.newmessage.ControllerFullStatus;
import io.smartspaces.container.control.newmessage.LiveActivityRuntimeStatus;
import io.smartspaces.container.control.newmessage.StandardMasterSpaceControllerCodec;
import io.smartspaces.container.controller.common.ros.RosSpaceControllerSupport;
import io.smartspaces.controller.client.master.RemoteActivityDeploymentManager;
import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.SpaceControllerConfiguration;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.server.services.ActiveLiveActivity;
import io.smartspaces.master.server.services.ActiveSpaceController;
import io.smartspaces.master.server.services.RemoteSpaceControllerClient;
import io.smartspaces.master.server.services.RemoteSpaceControllerClientListener;
import io.smartspaces.master.server.services.internal.DataBundleState;
import io.smartspaces.master.server.services.internal.MasterDataBundleManager;
import io.smartspaces.master.server.services.internal.RemoteSpaceControllerClientListenerCollection;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;
import io.smartspaces.spacecontroller.SpaceControllerState;
import io.smartspaces.spacecontroller.SpaceControllerStatus;
import io.smartspaces.system.SmartSpacesEnvironment;

import com.google.common.collect.Maps;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import smartspaces_msgs.ConfigurationParameterRequest;
import smartspaces_msgs.ConfigurationRequest;
import smartspaces_msgs.ContainerResourceCommitRequestMessage;
import smartspaces_msgs.ContainerResourceQueryRequestMessage;
import smartspaces_msgs.ControllerRequest;
import smartspaces_msgs.LiveActivityDeleteRequestMessage;
import smartspaces_msgs.LiveActivityDeployRequestMessage;
import smartspaces_msgs.LiveActivityRuntimeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RemoteSpaceControllerClient} which uses ROS.
 *
 * @author Keith M. Hughes
 */
public class RosRemoteSpaceControllerClient implements RemoteSpaceControllerClient {

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
  private final Map<String, SpaceControllerCommunicator> controllerCommunicators =
      Maps.newHashMap();

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
   * The ROS Master context the client is running in.
   */
  private MasterRosContext masterRosContext;

  /**
   * The main ROS node for the master.
   */
  private ConnectedNode masterNode;

  /**
   * Message factory for ROS messages.
   */
  private MessageFactory rosMessageFactory;

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

  /**
   * ROS message serializer for a live activity runtime request.
   */
  private MessageSerializer<LiveActivityRuntimeRequest> liveActivityRuntimeRequestSerializer;

  /**
   * ROS message serializer for a live activity deployment request.
   */
  private MessageSerializer<LiveActivityDeployRequestMessage> liveActivityDeployRequestSerializer;

  /**
   * ROS message serializer for a live activity delete request.
   */
  private MessageSerializer<LiveActivityDeleteRequestMessage> liveActivityDeleteRequestSerializer;

  /**
   * ROS message serializer for the full controller status.
   */
  private MessageSerializer<ConfigurationRequest> configurationRequestSerializer;

  /**
   * ROS message serializer for the container deployment query request.
   */
  private MessageSerializer<ContainerResourceQueryRequestMessage> containerResourceQueryRequestSerializer;

  /**
   * ROS message serializer for the container deployment commit request.
   */
  private MessageSerializer<ContainerResourceCommitRequestMessage> containerResourceCommitRequestSerializer;

  @Override
  public void startup() {
    log.info("Starting up ROS remote controller");

    tcpClientService = spaceEnvironment.getServiceRegistry()
        .getRequiredService(TcpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    remoteControllerClientListeners = new RemoteSpaceControllerClientListenerCollection(log);

    masterNode = masterRosContext.getMasterNode();
    rosMessageFactory = masterNode.getTopicMessageFactory();

    MessageSerializationFactory messageSerializationFactory =
        masterNode.getMessageSerializationFactory();
    liveActivityRuntimeRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityRuntimeRequest._TYPE);

    liveActivityDeployRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeployRequestMessage._TYPE);

    liveActivityDeleteRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeleteRequestMessage._TYPE);

    configurationRequestSerializer =
        messageSerializationFactory.newMessageSerializer(ConfigurationRequest._TYPE);

    containerResourceQueryRequestSerializer = messageSerializationFactory
        .newMessageSerializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceCommitRequestSerializer = messageSerializationFactory
        .newMessageSerializer(ContainerResourceCommitRequestMessage._TYPE);

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
    List<ConfigurationParameterRequest> parameterRequests = new ArrayList<>();
    SpaceControllerConfiguration configuration = controller.getSpaceController().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter =
            rosMessageFactory.newFromType(ConfigurationParameterRequest._TYPE);
        newParameter.setOperation(ConfigurationParameterRequest.OPERATION_ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        parameterRequests.add(newParameter);
      }
    }

    ConfigurationRequest request = rosMessageFactory.newFromType(ConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = configurationRequestSerializer.serialize(request);

    sendSpaceControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CONFIGURE,
        serialize);
  }

  @Override
  public void deployLiveActivity(ActiveLiveActivity liveActivity,
      LiveActivityDeploymentRequest request) {
    LiveActivityDeployRequestMessage rosRequest =
        rosMessageFactory.newFromType(LiveActivityDeployRequestMessage._TYPE);

    RosLiveActivityDeploymentMessageTranslator.serializeActivityDeploymentRequest(request,
        rosRequest);

    sendSpaceControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY,
        liveActivityDeployRequestSerializer.serialize(rosRequest));
  }

  @Override
  public void deleteLiveActivity(ActiveLiveActivity liveActivity,
      LiveActivityDeleteRequest request) {
    LiveActivityDeleteRequestMessage rosMessage =
        rosMessageFactory.newFromType(LiveActivityDeleteRequestMessage._TYPE);

    RosLiveActivityDeleteMessageTranslator.serializeLiveActivityDeleteRequest(request, rosMessage);

    sendSpaceControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY,
        liveActivityDeleteRequestSerializer.serialize(rosMessage));
  }

  @Override
  public void querySpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentQueryRequest query) {
    ContainerResourceQueryRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceQueryRequestMessage._TYPE);

    RosLiveActivityDeploymentMessageTranslator.serializeResourceDeploymentQuery(query, rosMessage,
        rosMessageFactory);

    sendSpaceControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_RESOURCE_QUERY,
        containerResourceQueryRequestSerializer.serialize(rosMessage));
  }

  @Override
  public void commitSpaceControllerResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentCommitRequest request) {
    ContainerResourceCommitRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceCommitRequestMessage._TYPE);
    RosLiveActivityDeploymentMessageTranslator.serializeResourceDeploymentCommit(request,
        rosMessage, rosMessageFactory);
    ChannelBuffer payload = containerResourceCommitRequestSerializer.serialize(rosMessage);
    sendSpaceControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_RESOURCE_COMMIT,
        payload);
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
    List<ConfigurationParameterRequest> parameterRequests = new ArrayList<>();
    ActivityConfiguration configuration = activity.getLiveActivity().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter =
            rosMessageFactory.newFromType(ConfigurationParameterRequest._TYPE);
        newParameter.setOperation(ConfigurationParameterRequest.OPERATION_ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        parameterRequests.add(newParameter);
      }
    }

    ConfigurationRequest request = rosMessageFactory.newFromType(ConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = configurationRequestSerializer.serialize(request);

    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CONFIGURE, serialize);
  }

  @Override
  public void startupLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STARTUP, null);
  }

  @Override
  public void activateLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_ACTIVATE, null);
  }

  @Override
  public void deactivateLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_DEACTIVATE, null);
  }

  @Override
  public void shutdownLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_SHUTDOWN, null);
  }

  @Override
  public void statusLiveActivity(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STATUS, null);
  }

  @Override
  public void cleanLiveActivityPermanentData(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_PERMANENT, null);
  }

  @Override
  public void cleanLiveActivityTempData(ActiveLiveActivity activity) {
    sendLiveActivityRuntimeRequest(activity,
        LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_TMP, null);
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
  private void sendControllerRequest(ActiveSpaceController controller, int operation) {
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
  void sendSpaceControllerRequest(ActiveSpaceController controller, int operation,
      ChannelBuffer payload) {
    ControllerRequest request = rosMessageFactory.newFromType(ControllerRequest._TYPE);
    request.setOperation(operation);

    if (payload != null) {
      request.setPayload(payload);
    }

    SpaceControllerCommunicator communicator = getCommunicator(controller, true);

    communicator.sendControllerRequest(request);
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
  private void sendLiveActivityRuntimeRequest(ActiveLiveActivity activity, int operation,
      ChannelBuffer payload) {
    LiveActivityRuntimeRequest request =
        rosMessageFactory.newFromType(LiveActivityRuntimeRequest._TYPE);
    request.setLiveActivityUuid(activity.getLiveActivity().getUuid());

    if (payload != null) {
      request.setPayload(payload);
    }

    request.setOperation(operation);

    sendSpaceControllerRequest(activity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST,
        liveActivityRuntimeRequestSerializer.serialize(request));
  }

  /**
   * Handle controller status updates.
   *
   * @param status
   *          The status update.
   */
  private void handleRemoteControllerStatusUpdate(String statusMessage) {
    Map<String, Object> statusObject = messageCodec.parseMessage(statusMessage);

    String statusType = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE);
    log.info("Master got controller status type " + statusType);
    switch (statusType) {
      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_HEARTBEAT:
        handleControllerHeartbeat(statusObject);

        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTROLLER_FULL_STATUS:
        // A full status request will also be treated as a heartbeat event
        // since the controller will only respond if it is alive.
        handleControllerHeartbeat(statusObject);

        ControllerFullStatus fullStatus = messageCodec.decodeControllerFullStatus(statusObject);

        List<LiveActivityRuntimeStatus> liveActivityStatuses = fullStatus.getLiveActivityStatuses();
        if (log.isInfoEnabled()) {
          String controllerUuid = (String) statusObject
              .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID);
          log.info(String.format("Received controller full status %s, %d activities",
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
        handleControllerStatusDataCapture(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_DATA_RESTORE:

        handleControllerStatusDataRestore(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_QUERY:
        handleContainerResourceQueryResponse(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_COMMIT:
        handleContainerResourceCommitResponse(statusObject);
        break;

      case StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_SHUTDOWN:
        handleSpaceControllerShutdown(statusObject);
        break;

      default:
        log.warn(String.format("Unknown status type %s, for controller %s", statusType, statusObject
            .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID)));
    }
  }

  /**
   * Handle a data restore from a controller status message.
   * 
   * @param statusObject
   *          the controller status object
   */
  private void handleControllerStatusDataRestore(Map<String, Object> statusObject) {
    String statusCode = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_CODE);
    String controllerUuid = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID);
    log.info("Received data restore response " + statusCode);
    DataBundleState restoreState = SpaceControllerStatus.isSuccessDescription(statusCode)
        ? DataBundleState.RESTORE_RECEIVED : DataBundleState.RESTORE_ERROR;
    remoteControllerClientListeners.signalDataBundleState(controllerUuid, restoreState);
  }

  /**
   * Handle a data capture from a controller status message.
   * 
   * @param statusObject
   *          the controller status object
   */
  private void handleControllerStatusDataCapture(Map<String, Object> statusObject) {
    String statusCode = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_CODE);
    String controllerUuid = (String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID);
    log.info("Received data capture response " + statusCode);
    DataBundleState captureState = SpaceControllerStatus.isSuccessDescription(statusCode)
        ? DataBundleState.CAPTURE_RECEIVED : DataBundleState.CAPTURE_ERROR;
    remoteControllerClientListeners.signalDataBundleState(controllerUuid, captureState);
  }

  /**
   * Handle a controller heartbeat message.
   *
   * @param status
   *          the status message for the heartbeat
   */
  private void handleControllerHeartbeat(Map<String, Object> status) {
    String controllerUuid =
        (String) status.get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID);
    long timestamp = System.currentTimeMillis();
    remoteControllerClientListeners.signalSpaceControllerHeartbeat(controllerUuid, timestamp);
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
   * @param status
   *          the controller status
   */
  private void handleSpaceControllerShutdown(Map<String, Object> statusObject) {
    remoteControllerClientListeners.signalSpaceControllerShutdown((String) statusObject
        .get(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID));
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
    String remoteNode = controller.getSpaceController().getHostId();
    spaceEnvironment.getLog().info("Attempting to get communicator");
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(remoteNode);
      spaceEnvironment.getLog().info(communicator);

      if (communicator == null) {
        controller.setState(SpaceControllerState.CONNECT_ATTEMPT);
        communicator = new SpaceControllerCommunicator(controller);
        communicator.startup(masterRosContext.getMasterNode(), remoteNode);
        controllerCommunicators.put(remoteNode, communicator);

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
    String remoteNode = controller.getSpaceController().getHostId();
    SpaceControllerCommunicator communicator = null;
    synchronized (controllerCommunicators) {
      communicator = controllerCommunicators.remove(remoteNode);
    }

    if (communicator != null) {
      communicator.shutdown();
      log.formatInfo("Communicator for controller %s shutdown and removed",
          controller.getSpaceController().getUuid());
    }
  }

  /**
   * Set the Master ROS context.
   *
   * @param masterRosContext
   *          the master ROS context
   */
  public void setMasterRosContext(MasterRosContext masterRosContext) {
    this.masterRosContext = masterRosContext;
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
   * Bundles the subscribers and publishers for communication with a space
   * controller.
   *
   * @author Keith M. Hughes
   */
  public class SpaceControllerCommunicator {

    /**
     * The space controller we are the communicator for.
     */
    private final ActiveSpaceController spaceController;

    /**
     * The publisher for activity runtime requests.
     */
    private Publisher<ControllerRequest> controllerRequestPublisher;

    /**
     * Publisher listener for publisher events.
     */
    private CountDownPublisherListener<ControllerRequest> publisherListener;

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
     * @param node
     *          the node which is running the communicator
     * @param remoteNode
     *          the remote node
     */
    public void startup(ConnectedNode node, String remoteNode) {
      controllerClient = tcpClientService.newStringClient(
          StandardMasterSpaceControllerCodec.DELIMITERS, StandardMasterSpaceControllerCodec.CHARSET,
          "localhost", StandardMasterSpaceControllerCodec.CONTROLLER_SERVER_PORT, log);
      controllerClient.addListener(new TcpClientNetworkCommunicationEndpointListener<String>() {

        @Override
        public void
            onTcpClientConnectionSuccess(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
          log.info("Controller client connected");
          connectionLatch.countDown();
        }

        @Override
        public void
            onTcpClientConnectionClose(TcpClientNetworkCommunicationEndpoint<String> endpoint) {
          // TODO Auto-generated method stub

        }

        @Override
        public void onNewTcpClientMessage(TcpClientNetworkCommunicationEndpoint<String> endpoint,
            String message) {
          handleRemoteControllerStatusUpdate(message);
        }
      });
      controllerClient.startup();

      publisherListener = CountDownPublisherListener.newFromCounts(1, 1, 1, 1, 1);
      controllerRequestPublisher = RosSpaceControllerSupport.getControllerRequestPublisher(node,
          remoteNode, publisherListener);

      remoteControllerClientListeners.signalSpaceControllerConnectAttempt(spaceController);
    }

    /**
     * Shut the communicator down.
     */
    public void shutdown() {
      controllerClient.shutdown();

      controllerRequestPublisher.shutdown();
      controllerRequestPublisher = null;

      remoteControllerClientListeners.signalSpaceControllerDisconnectAttempt(spaceController);
    }

    /**
     * Send a request to a controller.
     *
     * @param request
     *          the request to send
     */
    public void sendControllerRequest(ControllerRequest request) {
      try {
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait,
            TimeUnit.MILLISECONDS)) {
          controllerRequestPublisher.publish(request);
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

    /**
     * Send a request to a controller.
     *
     * @param request
     *          the request to send
     */
    public void sendControllerRequest(String request) {
      try {
        if (connectionLatch.await(controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
          controllerClient.write(request);
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
