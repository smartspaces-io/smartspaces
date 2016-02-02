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
import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.ros.RosLiveActivityDeleteMessageTranslator;
import io.smartspaces.container.control.message.activity.ros.RosLiveActivityDeploymentMessageTranslator;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
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
import io.smartspaces.spacecontroller.SpaceControllerState;
import io.smartspaces.spacecontroller.SpaceControllerStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import smartspaces_msgs.ConfigurationParameterRequest;
import smartspaces_msgs.ConfigurationRequest;
import smartspaces_msgs.ContainerResourceCommitRequestMessage;
import smartspaces_msgs.ContainerResourceCommitResponseMessage;
import smartspaces_msgs.ContainerResourceQueryRequestMessage;
import smartspaces_msgs.ContainerResourceQueryResponseMessage;
import smartspaces_msgs.ControllerFullStatus;
import smartspaces_msgs.ControllerRequest;
import smartspaces_msgs.ControllerStatus;
import smartspaces_msgs.LiveActivityDeleteRequestMessage;
import smartspaces_msgs.LiveActivityDeleteResponseMessage;
import smartspaces_msgs.LiveActivityDeployRequestMessage;
import smartspaces_msgs.LiveActivityDeployResponseMessage;
import smartspaces_msgs.LiveActivityRuntimeRequest;
import smartspaces_msgs.LiveActivityRuntimeStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
  private final Map<String, SpaceControllerCommunicator> controllerCommunicators = Maps
      .newHashMap();

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
   * Listener for all controller status message updates.
   */
  private MessageListener<ControllerStatus> controllerStatusListener;

  /**
   * Logger for the controller.
   */
  private ExtendedLog log;

  /**
   * ROS message serializer for a live activity runtime request.
   */
  private MessageSerializer<LiveActivityRuntimeRequest> liveActivityRuntimeRequestSerializer;

  /**
   * ROS message deserializer for a live activity runtime status.
   */
  private MessageDeserializer<LiveActivityRuntimeStatus> liveActivityRuntimeStatusDeserializer;

  /**
   * ROS message serializer for a live activity deployment request.
   */
  private MessageSerializer<LiveActivityDeployRequestMessage> liveActivityDeployRequestSerializer;

  /**
   * ROS message deserializer for a live activity deployment status.
   */
  private MessageDeserializer<LiveActivityDeployResponseMessage> liveActivityDeployResponseDeserializer;

  /**
   * ROS message serializer for a live activity delete request.
   */
  private MessageSerializer<LiveActivityDeleteRequestMessage> liveActivityDeleteRequestSerializer;

  /**
   * ROS message deserializer for a live activity delete status.
   */
  private MessageDeserializer<LiveActivityDeleteResponseMessage> liveActivityDeleteResponseDeserializer;

  /**
   * ROS message deserializer for the full controller status.
   */
  private MessageDeserializer<ControllerFullStatus> controllerFullStatusDeserializer;

  /**
   * ROS message serializer for the full controller status.
   */
  private MessageSerializer<ConfigurationRequest> configurationRequestSerializer;

  /**
   * ROS message serializer for the container deployment query request.
   */
  private MessageSerializer<ContainerResourceQueryRequestMessage> containerResourceQueryRequestSerializer;

  /**
   * ROS message deserializer for the container deployment query response.
   */
  private MessageDeserializer<ContainerResourceQueryResponseMessage> containerResourceQueryResponseDeserializer;

  /**
   * ROS message serializer for the container deployment commit request.
   */
  private MessageSerializer<ContainerResourceCommitRequestMessage> containerResourceCommitRequestSerializer;

  /**
   * ROS message deserializer for the container deployment commit response.
   */
  private MessageDeserializer<ContainerResourceCommitResponseMessage> containerResourceCommitResponseDeserializer;

  @Override
  public void startup() {
    log.info("Starting up ROS remote controller");

    remoteControllerClientListeners = new RemoteSpaceControllerClientListenerCollection(log);

    masterNode = masterRosContext.getMasterNode();
    rosMessageFactory = masterNode.getTopicMessageFactory();

    MessageSerializationFactory messageSerializationFactory =
        masterNode.getMessageSerializationFactory();
    liveActivityRuntimeRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityRuntimeRequest._TYPE);

    liveActivityRuntimeStatusDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityRuntimeStatus._TYPE);

    liveActivityDeployRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeployRequestMessage._TYPE);

    liveActivityDeployResponseDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeployResponseMessage._TYPE);

    liveActivityDeleteRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeleteRequestMessage._TYPE);

    liveActivityDeleteResponseDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeleteResponseMessage._TYPE);

    controllerFullStatusDeserializer =
        messageSerializationFactory.newMessageDeserializer(ControllerFullStatus._TYPE);

    configurationRequestSerializer =
        messageSerializationFactory.newMessageSerializer(ConfigurationRequest._TYPE);

    containerResourceQueryRequestSerializer =
        messageSerializationFactory
            .newMessageSerializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceQueryResponseDeserializer =
        messageSerializationFactory
            .newMessageDeserializer(ContainerResourceQueryResponseMessage._TYPE);

    containerResourceCommitRequestSerializer =
        messageSerializationFactory
            .newMessageSerializer(ContainerResourceCommitRequestMessage._TYPE);

    containerResourceCommitResponseDeserializer =
        messageSerializationFactory
            .newMessageDeserializer(ContainerResourceCommitResponseMessage._TYPE);

    controllerStatusListener = new MessageListener<ControllerStatus>() {
      @Override
      public void onNewMessage(ControllerStatus status) {
        handleRemoteControllerStatusUpdate(status);
      }
    };

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
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER);

    // Heartbeat is shut down once controller acknowledges shutdown.
  }

  @Override
  public void requestSpaceControllerStatus(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_STATUS);
  }

  @Override
  public void shutdownSpacecontrollerAllActivities(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES);
  }

  @Override
  public void configureSpaceController(ActiveSpaceController controller) {
    List<ConfigurationParameterRequest> parameterRequests = Lists.newArrayList();
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
  public void
      deleteLiveActivity(ActiveLiveActivity liveActivity, LiveActivityDeleteRequest request) {
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
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP);
  }

  @Override
  public void cleanSpaceControllerPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT);
  }

  @Override
  public void cleanSpaceControllerActivitiesTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES);
  }

  @Override
  public void cleanSpaceControllerActivitiesPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller,
        ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES);
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
    List<ConfigurationParameterRequest> parameterRequests = Lists.newArrayList();
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
  private void handleRemoteControllerStatusUpdate(ControllerStatus status) {
    switch (status.getStatus()) {
      case ControllerStatus.STATUS_CONTROLLER_HEARTBEAT:
        handleControllerHeartbeat(status);

        break;

      case ControllerStatus.STATUS_CONTROLLER_FULL_STATUS:
        // A full status request will also be treated as a heartbeat event
        // since the controller will only respond if it is alive.
        handleControllerHeartbeat(status);

        ControllerFullStatus fullStatus =
            controllerFullStatusDeserializer.deserialize(status.getPayload());

        List<LiveActivityRuntimeStatus> liveActivityStatuses = fullStatus.getLiveActivityStatuses();
        if (log.isInfoEnabled()) {
          log.info(String.format("Received controller full status %s, %d activities",
              status.getControllerUuid(), liveActivityStatuses.size()));
        }
        for (LiveActivityRuntimeStatus liveActivityStatus : liveActivityStatuses) {
          if (log.isInfoEnabled()) {
            log.info(String.format("\tActivity %s, %d\n", liveActivityStatus.getUuid(),
                liveActivityStatus.getStatus()));
          }
          handleRemoteLiveActivityStatusUpdate(liveActivityStatus);
        }

        break;

      case ControllerStatus.STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS:
        LiveActivityRuntimeStatus liveActivityStatus =
            liveActivityRuntimeStatusDeserializer.deserialize(status.getPayload());
        if (log.isDebugEnabled()) {
          log.debug(String.format("Activity status %s, %d\n", liveActivityStatus.getUuid(),
              liveActivityStatus.getStatus()));
        }
        handleRemoteLiveActivityStatusUpdate(liveActivityStatus);

        break;

      case ControllerStatus.STATUS_CONTROLLER_ACTIVITY_INSTALL:
        LiveActivityDeployResponseMessage deployResponse =
            liveActivityDeployResponseDeserializer.deserialize(status.getPayload());

        LiveActivityDeploymentResponse dstatus =
            RosLiveActivityDeploymentMessageTranslator
                .deserializeDeploymentResponseMessage(deployResponse);
        remoteActivityDeploymentManager.handleLiveDeployResult(dstatus);
        break;

      case ControllerStatus.STATUS_CONTROLLER_ACTIVITY_DELETE:
        LiveActivityDeleteResponseMessage deleteResponse =
            liveActivityDeleteResponseDeserializer.deserialize(status.getPayload());

        remoteControllerClientListeners.signalActivityDelete(deleteResponse.getUuid(),
            RosLiveActivityDeleteMessageTranslator
                .deserializeLiveActivityDeleteResponseMessage(deleteResponse));

        break;

      case ControllerStatus.STATUS_CONTROLLER_DATA_CAPTURE:
        log.info("Received data capture response " + status.getStatusCode());
        DataBundleState captureState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.CAPTURE_RECEIVED
                : DataBundleState.CAPTURE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getControllerUuid(),
            captureState);
        break;

      case ControllerStatus.STATUS_CONTROLLER_DATA_RESTORE:
        log.info("Received data restore response " + status.getStatusCode());
        DataBundleState restoreState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.RESTORE_RECEIVED
                : DataBundleState.RESTORE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getControllerUuid(),
            restoreState);
        break;

      case ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_QUERY:
        handleContainerResourceQueryResponse(containerResourceQueryResponseDeserializer
            .deserialize(status.getPayload()));
        break;

      case ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_COMMIT:
        handleContainerResourceCommitResponse(containerResourceCommitResponseDeserializer
            .deserialize(status.getPayload()));
        break;

      case ControllerStatus.STATUS_CONTROLLER_SHUTDOWN:
        handleSpaceControllerShutdown(status);
        break;

      default:
        log.warn(String.format("Unknown status type %d, for controller %s", status.getStatus(),
            status.getControllerUuid()));
    }
  }

  /**
   * Handle a controller heartbeat message.
   *
   * @param status
   *          the status message for the heartbeat
   */
  private void handleControllerHeartbeat(ControllerStatus status) {
    long timestamp = System.currentTimeMillis();
    remoteControllerClientListeners.signalSpaceControllerHeartbeat(status.getControllerUuid(),
        timestamp);
  }

  /**
   * Handle activity status updates.
   *
   * @param status
   *          the status update
   */
  private void handleRemoteLiveActivityStatusUpdate(LiveActivityRuntimeStatus status) {
    ActivityState newState;

    switch (status.getStatus()) {
      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN:
        newState = ActivityState.UNKNOWN;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DOESNT_EXIST:
        newState = ActivityState.DOESNT_EXIST;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_FAILURE:
        newState = ActivityState.DEPLOY_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY:
        newState = ActivityState.READY;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_FAILURE:
        newState = ActivityState.STARTUP_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_RUNNING:
        newState = ActivityState.RUNNING;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_FAILURE:
        newState = ActivityState.ACTIVATE_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVE:
        newState = ActivityState.ACTIVE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_FAILURE:
        newState = ActivityState.DEACTIVATE_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_FAILURE:
        newState = ActivityState.SHUTDOWN_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_CRASH:
        newState = ActivityState.CRASHED;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_ATTEMPT:
        newState = ActivityState.STARTUP_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_ATTEMPT:
        newState = ActivityState.DEPLOY_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_ATTEMPT:
        newState = ActivityState.ACTIVATE_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_ATTEMPT:
        newState = ActivityState.DEACTIVATE_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_ATTEMPT:
        newState = ActivityState.SHUTDOWN_ATTEMPT;
        break;

      default:
        newState = ActivityState.UNKNOWN;
    }

    remoteControllerClientListeners.signalActivityStateChange(status.getUuid(), newState,
        status.getStatusDetail());
  }

  /**
   * Handle a container resource deployment query response.
   *
   * @param rosResponse
   *          the ROS response
   */
  private void handleContainerResourceQueryResponse(
      ContainerResourceQueryResponseMessage rosResponse) {
    ContainerResourceDeploymentQueryResponse response =
        RosLiveActivityDeploymentMessageTranslator
            .deserializeResourceDeploymentQueryResponse(rosResponse);
    log.info(String.format(
        "Got resource deployment query response for transaction ID %s with status %s",
        response.getTransactionId(), response.getStatus()));

    remoteActivityDeploymentManager.handleResourceDeploymentQueryResponse(response);
  }

  /**
   * Handle a container resource deployment commit response.
   *
   * @param rosResponse
   *          the ROS response
   */
  private void handleContainerResourceCommitResponse(
      ContainerResourceCommitResponseMessage rosResponse) {
    ContainerResourceDeploymentCommitResponse response =
        RosLiveActivityDeploymentMessageTranslator
            .deserializeResourceDeploymentCommitResponse(rosResponse);
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
  private void handleSpaceControllerShutdown(ControllerStatus status) {
    remoteControllerClientListeners.signalSpaceControllerShutdown(status.getControllerUuid());
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
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(remoteNode);

      if (communicator == null) {
        controller.setState(SpaceControllerState.CONNECT_ATTEMPT);
        communicator = new SpaceControllerCommunicator(controller);
        communicator
            .startup(masterRosContext.getMasterNode(), remoteNode, controllerStatusListener);
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
      log.formatInfo("Communicator for controller %s shutdown and removed", controller
          .getSpaceController().getUuid());
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
     * The subscriber for controller status updates.
     */
    private Subscriber<ControllerStatus> controllerStatusSubscriber;

    /**
     * Publisher listener for publisher events.
     */
    private CountDownPublisherListener<ControllerRequest> publisherListener;

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
     * @param controllerStatusListener
     *          the listener for controller status messages
     */
    public void startup(ConnectedNode node, String remoteNode,
        MessageListener<ControllerStatus> controllerStatusListener) {
      publisherListener = CountDownPublisherListener.newFromCounts(1, 1, 1, 1, 1);
      controllerStatusSubscriber =
          RosSpaceControllerSupport.getControllerStatusSubscriber(node, remoteNode,
              controllerStatusListener, null);
      controllerRequestPublisher =
          RosSpaceControllerSupport.getControllerRequestPublisher(node, remoteNode,
              publisherListener);

      remoteControllerClientListeners.signalSpaceControllerConnectAttempt(spaceController);
    }

    /**
     * Shut the communicator down.
     */
    public void shutdown() {
      controllerRequestPublisher.shutdown();
      controllerRequestPublisher = null;
      controllerStatusSubscriber.shutdown();
      controllerStatusSubscriber = null;

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
  }
}
