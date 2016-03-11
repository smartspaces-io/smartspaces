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

package io.smartspaces.spacecontroller.runtime.ros;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityState;
import io.smartspaces.activity.ActivityStatus;
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
import io.smartspaces.container.controller.common.ros.RosSpaceControllerConstants;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.liveactivity.runtime.LiveActivityRunner;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.master.server.remote.client.RemoteMasterServerClient;
import io.smartspaces.master.server.remote.client.internal.StandardRemoteMasterServerClient;
import io.smartspaces.spacecontroller.SpaceControllerStatus;
import io.smartspaces.spacecontroller.runtime.SpaceControllerCommunicator;
import io.smartspaces.spacecontroller.runtime.SpaceControllerControl;
import io.smartspaces.spacecontroller.runtime.SpaceControllerDataOperation;
import io.smartspaces.spacecontroller.runtime.SpaceControllerHeartbeat;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.SmartSpacesUtilities;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.osgi.common.RosEnvironment;

import smartspaces_msgs.ConfigurationParameterRequest;
import smartspaces_msgs.ConfigurationRequest;
import smartspaces_msgs.ContainerResourceCommitRequestMessage;
import smartspaces_msgs.ContainerResourceCommitResponseMessage;
import smartspaces_msgs.ContainerResourceQueryRequestMessage;
import smartspaces_msgs.ContainerResourceQueryResponseMessage;
import smartspaces_msgs.ControllerDataRequest;
import smartspaces_msgs.ControllerFullStatus;
import smartspaces_msgs.ControllerRequest;
import smartspaces_msgs.ControllerStatus;
import smartspaces_msgs.LiveActivityDeleteRequestMessage;
import smartspaces_msgs.LiveActivityDeleteResponseMessage;
import smartspaces_msgs.LiveActivityDeployRequestMessage;
import smartspaces_msgs.LiveActivityDeployResponseMessage;
import smartspaces_msgs.LiveActivityRuntimeRequest;
import smartspaces_msgs.LiveActivityRuntimeStatus;

import com.google.common.annotations.VisibleForTesting;

/**
 * An {@link SpaceControllerCommunicator} using ROS for communication.
 *
 * @author Keith M. Hughes
 */
public class RosSpaceControllerCommunicator implements SpaceControllerCommunicator {

  /**
   * The amount of time between sending a shutdown message and closing the
   * master connection, in milliseconds.
   */
  public static final int SHUTDOWN_DELAY = 500;

  /**
   * Startup delay for space controller startup notification. In milliseconds.
   */
  public static final int STARTUP_NOTIFICATION_DELAY = 1000;

  /**
   * The controller being controlled.
   */
  private SpaceControllerControl controllerControl;

  /**
   * The ROS environment this controller is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The node for the controller.
   */
  private ConnectedNode node;

  /**
   * Message factory for creating messages.
   */
  private MessageFactory rosMessageFactory;

  /**
   * Publisher for controller status.
   */
  private Publisher<ControllerStatus> controllerStatusPublisher;

  /**
   * Subscriber for controller requests.
   */
  private Subscriber<ControllerRequest> controllerRequestSubscriber;

  /**
   * A ROS message serialize for controller full status messages.
   */
  private MessageSerializer<ControllerFullStatus> controllerFullStatusMessageSerializer;

  /**
   * ROS message deserializer for live activity runtime requests.
   */
  private MessageDeserializer<LiveActivityRuntimeRequest> liveActivityRuntimeRequestDeserializer;

  /**
   * ROS message serializer for live activity runtime statuses.
   */
  private MessageSerializer<LiveActivityRuntimeStatus> liveActivityRuntimeStatusSerializer;

  /**
   * ROS message deserializer for live activity deployment requests.
   */
  private MessageDeserializer<LiveActivityDeployRequestMessage> liveActivityDeployRequestDeserializer;

  /**
   * ROS message serializer for live activity deployment statuses.
   */
  private MessageSerializer<LiveActivityDeployResponseMessage> liveActivityDeployResponseSerializer;

  /**
   * ROS message deserializer for live activity deletetion requests.
   */
  private MessageDeserializer<LiveActivityDeleteRequestMessage> liveActivityDeleteRequestDeserializer;

  /**
   * ROS message serializer for live activity deletion statuses.
   */
  private MessageSerializer<LiveActivityDeleteResponseMessage> liveActivityDeleteResponseSerializer;

  /**
   * ROS message deserializer for the configuration requests.
   */
  private MessageDeserializer<ConfigurationRequest> configurationRequestDeserializer;

  /**
   * ROS message deserializer for live activity deletion requests.
   */
  private MessageDeserializer<ControllerDataRequest> controllerDataRequestMessageDeserializer;

  /**
   * ROS message deserializer for container resource deployment query requests.
   */
  private MessageDeserializer<ContainerResourceQueryRequestMessage> containerResourceQueryRequestDeserializer;

  /**
   * ROS message serializer for container resource deployment query response.
   */
  private MessageSerializer<ContainerResourceQueryResponseMessage> containerResourceQueryResponseSerializer;

  /**
   * ROS message deserializer for container resource deployment commit requests.
   */
  private MessageDeserializer<ContainerResourceCommitRequestMessage> containerResourceCommitRequestDeserializer;

  /**
   * ROS message serializer for container resource deployment commit response.
   */
  private MessageSerializer<ContainerResourceCommitResponseMessage> containerResourceCommitResponseSerializer;

  /**
   * The space environment for this communicator.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Create a new space controller communicator.
   *
   * @param rosEnvironment
   *          ROS environment
   * @param spaceEnvironment
   *          space environment
   */
  public RosSpaceControllerCommunicator(RosEnvironment rosEnvironment,
      SmartSpacesEnvironment spaceEnvironment) {
    this.rosEnvironment = rosEnvironment;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void onStartup() {
    final NodeConfiguration nodeConfiguration =
        rosEnvironment.getPublicNodeConfigurationWithNodeName();
    nodeConfiguration.setNodeName("smartspaces/controller");

    node = rosEnvironment.newNode(nodeConfiguration);
    rosMessageFactory = node.getTopicMessageFactory();

    PublisherListener<ControllerStatus> controllerStatusPublisherListener =
        new DefaultPublisherListener<ControllerStatus>() {
          @Override
          public void onNewSubscriber(Publisher<ControllerStatus> publisher,
              SubscriberIdentifier subscriber) {
            if (spaceEnvironment.getLog().isInfoEnabled()) {
              spaceEnvironment.getLog().info(
                  String.format("New subscriber for controller status %s",
                      subscriber.getNodeIdentifier()));
            }
          }
        };

    controllerStatusPublisher =
        node.newPublisher(RosSpaceControllerConstants.CONTROLLER_STATUS_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_STATUS_MESSAGE_TYPE);
    controllerStatusPublisher.addListener(controllerStatusPublisherListener);

    controllerRequestSubscriber =
        node.newSubscriber(RosSpaceControllerConstants.CONTROLLER_REQUEST_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_REQUEST_MESSAGE_TYPE);
    controllerRequestSubscriber.addMessageListener(new MessageListener<ControllerRequest>() {
      @Override
      public void onNewMessage(ControllerRequest request) {
        handleControllerRequest(request);
      }
    });

    MessageSerializationFactory messageSerializationFactory = node.getMessageSerializationFactory();
    configurationRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(ConfigurationRequest._TYPE);

    controllerFullStatusMessageSerializer =
        messageSerializationFactory.newMessageSerializer(ControllerFullStatus._TYPE);

    liveActivityRuntimeRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityRuntimeRequest._TYPE);

    liveActivityRuntimeStatusSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityRuntimeStatus._TYPE);

    liveActivityDeployResponseSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeployResponseMessage._TYPE);

    liveActivityDeployRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeployRequestMessage._TYPE);

    liveActivityDeleteResponseSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeleteResponseMessage._TYPE);

    liveActivityDeleteRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeleteRequestMessage._TYPE);

    controllerDataRequestMessageDeserializer =
        messageSerializationFactory.newMessageDeserializer(ControllerDataRequest._TYPE);

    containerResourceQueryRequestDeserializer =
        messageSerializationFactory
            .newMessageDeserializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceQueryResponseSerializer =
        messageSerializationFactory
            .newMessageSerializer(ContainerResourceQueryResponseMessage._TYPE);

    containerResourceCommitRequestDeserializer =
        messageSerializationFactory
            .newMessageDeserializer(ContainerResourceCommitRequestMessage._TYPE);

    containerResourceCommitResponseSerializer =
        messageSerializationFactory
            .newMessageSerializer(ContainerResourceCommitResponseMessage._TYPE);
  }

  @Override
  public void registerControllerWithMaster(SimpleSpaceController controllerInfo) {
    // Yes, this is inside of a mostly ROS based class, but the ROS class is
    // going away to use the new comm system and
    // this code will be in here then anyway. So is confusing, but is the final
    // home, so is here now.
    RemoteMasterServerClient masterServerClient =
        new StandardRemoteMasterServerClient(spaceEnvironment);
    masterServerClient.startup();
    try {
      masterServerClient.registerSpaceController(controllerInfo);
    } finally {
      masterServerClient.shutdown();
    }
  }

  @Override
  public void onShutdown() {
    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_SHUTDOWN, null);
    SmartSpacesUtilities.delay(SHUTDOWN_DELAY);

    if (node != null) {
      node.shutdown();
      node = null;
    }
  }

  @Override
  public SpaceControllerHeartbeat newSpaceControllerHeartbeat() {
    return new RosControllerHeartbeat();
  }

  /**
   * Handle a ROS controller control request coming in.
   *
   * @param request
   *          The ROS request.
   */
  @VisibleForTesting
  void handleControllerRequest(smartspaces_msgs.ControllerRequest request) {
    switch (request.getOperation()) {
      case ControllerRequest.OPERATION_CONTROLLER_STATUS:
        publishControllerFullStatus();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES:
        controllerControl.shutdownAllLiveActivities();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER:
        controllerControl.shutdownControllerContainer();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY:
        handleLiveActivityDeployment(liveActivityDeployRequestDeserializer.deserialize(request
            .getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY:
        handleLiveActivityDeletion(liveActivityDeleteRequestDeserializer.deserialize(request
            .getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST:
        handleLiveActivityRuntimeRequest(liveActivityRuntimeRequestDeserializer.deserialize(request
            .getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP:
        controllerControl.cleanControllerTempData();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT:
        controllerControl.cleanControllerPermanentData();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES:
        controllerControl.cleanControllerTempDataAll();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES:
        controllerControl.cleanControllerPermanentDataAll();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CAPTURE_DATA:
        ControllerDataRequest captureDataRequest =
            controllerDataRequestMessageDeserializer.deserialize(request.getPayload());
        controllerControl.captureControllerDataBundle(captureDataRequest.getTransferUri());
        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESTORE_DATA:
        ControllerDataRequest restoreDataRequest =
            controllerDataRequestMessageDeserializer.deserialize(request.getPayload());
        controllerControl.restoreControllerDataBundle(restoreDataRequest.getTransferUri());
        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESOURCE_QUERY:
        ContainerResourceQueryRequestMessage containerResourceQueryRequest =
            containerResourceQueryRequestDeserializer.deserialize(request.getPayload());
        handleContainerResourceQueryRequest(containerResourceQueryRequest);

        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESOURCE_COMMIT:
        ContainerResourceCommitRequestMessage containerResourceCommitRequest =
            containerResourceCommitRequestDeserializer.deserialize(request.getPayload());
        handleContainerResourceCommitRequest(containerResourceCommitRequest);

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CONFIGURE:
        handleControllerConfigurationRequest(configurationRequestDeserializer.deserialize(request
            .getPayload()));

        break;

      default:
        spaceEnvironment.getLog().error(
            String.format("Unknown ROS controller request %d", request.getOperation()));
    }
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param rosRequest
   *          the ROS request
   */
  private void handleContainerResourceQueryRequest(ContainerResourceQueryRequestMessage rosRequest) {
    switch (rosRequest.getType()) {
      case ContainerResourceQueryRequestMessage.TYPE_SPECIFIC_QUERY:
        spaceEnvironment.getLog().info(
            String.format("Got resource deployment query with transaction ID %s",
                rosRequest.getTransactionId()));
        ContainerResourceDeploymentQueryRequest request =
            RosLiveActivityDeploymentMessageTranslator
                .deserializeContainerResourceDeploymentQuery(rosRequest);

        ContainerResourceDeploymentQueryResponse queryResponse =
            controllerControl.handleContainerResourceDeploymentQueryRequest(request);
        spaceEnvironment.getLog().info(
            String.format("Resource deployment query with transaction ID %s has status %s",
                rosRequest.getTransactionId(), queryResponse.getStatus()));

        ContainerResourceQueryResponseMessage response =
            rosMessageFactory.newFromType(ContainerResourceQueryResponseMessage._TYPE);
        RosLiveActivityDeploymentMessageTranslator.serializeResourceDeploymentQueryResponse(
            queryResponse, response);

        publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_QUERY,
            containerResourceQueryResponseSerializer.serialize(response));
        break;

      default:
        spaceEnvironment.getLog().error(
            String.format("Unknown ContainerResourceDeploymentRequest %d", rosRequest.getType()));
    }
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param rosRequest
   *          the ROS request
   */
  private void
      handleContainerResourceCommitRequest(ContainerResourceCommitRequestMessage rosRequest) {
    spaceEnvironment.getLog().info(
        String.format("Got resource deployment commit with transaction ID %s",
            rosRequest.getTransactionId()));
    ContainerResourceDeploymentCommitRequest request =
        RosLiveActivityDeploymentMessageTranslator.deserializeResourceDeploymentCommit(rosRequest);
    ContainerResourceDeploymentCommitResponse commitResponse =
        controllerControl.handleContainerResourceDeploymentCommitRequest(request);
    ContainerResourceCommitResponseMessage rosCommitResponse =
        rosMessageFactory.newFromType(ContainerResourceCommitResponseMessage._TYPE);
    RosLiveActivityDeploymentMessageTranslator.serializeResourceDeploymentCommitResponse(
        commitResponse, rosCommitResponse);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_COMMIT,
        containerResourceCommitResponseSerializer.serialize(rosCommitResponse));
  }

  /**
   * Create and publish controller full status.
   */
  private void publishControllerFullStatus() {
    spaceEnvironment.getLog().info("Getting full controller status");

    SimpleSpaceController controllerInfo = controllerControl.getControllerInfo();

    ControllerFullStatus fullStatus = rosMessageFactory.newFromType(ControllerFullStatus._TYPE);
    fullStatus.setName(controllerInfo.getName());
    fullStatus.setDescription(controllerInfo.getDescription());
    fullStatus.setHostId(controllerInfo.getHostId());

    for (InstalledLiveActivity activity : controllerControl.getAllInstalledLiveActivities()) {
      LiveActivityRuntimeStatus cas =
          rosMessageFactory.newFromType(LiveActivityRuntimeStatus._TYPE);
      cas.setUuid(activity.getUuid());

      LiveActivityRunner activeActivity =
          controllerControl.getLiveActivityRunnerByUuid(cas.getUuid());
      if (activeActivity != null) {
        Activity instance = activeActivity.getInstance();
        ActivityState state = null;
        if (instance != null) {
          ActivityStatus activityStatus = instance.getActivityStatus();
          state = activityStatus.getState();
          if (activityStatus.getException() != null) {
            cas.setStatusDetail(SmartSpacesException.getStackTrace(activityStatus.getException()));
          }
        } else {
          state = ActivityState.READY;
        }
        cas.setStatus(translateActivityState(state));

        if (spaceEnvironment.getLog().isInfoEnabled()) {
          spaceEnvironment.getLog().info(
              String.format("Full status live activity %s status %s, returning %d", cas.getUuid(),
                  state, cas.getStatus()));
        }
      } else {
        cas.setStatus(LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY);
        spaceEnvironment.getLog()
            .warn(
                String.format("Full status live activity %s not found, returning READY",
                    cas.getUuid()));
      }

      fullStatus.getLiveActivityStatuses().add(cas);
    }

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_FULL_STATUS,
        controllerFullStatusMessageSerializer.serialize(fullStatus));
  }

  @Override
  public void publishControllerDataStatus(SpaceControllerDataOperation type,
      SpaceControllerStatus statusCode, Exception e) {

    int status =
        SpaceControllerDataOperation.DATA_CAPTURE.equals(type) ? ControllerStatus.STATUS_CONTROLLER_DATA_CAPTURE
            : ControllerStatus.STATUS_CONTROLLER_DATA_RESTORE;

    ControllerStatus statusMsg = newControllerStatus(status);
    statusMsg.setStatusCode(statusCode.getDescription());

    if (e != null) {
      statusMsg.setStatusDetail(SmartSpacesException.getStackTrace(e));
    }

    controllerStatusPublisher.publish(statusMsg);
  }

  /**
   * Handle a live activity deployment request.
   *
   * @param deployRequest
   *          the deployment request
   */
  private void handleLiveActivityDeployment(LiveActivityDeployRequestMessage deployRequest) {
    LiveActivityDeploymentRequest activityDeployRequest =
        RosLiveActivityDeploymentMessageTranslator
            .deserializeActivityDeploymentRequest(deployRequest);

    LiveActivityDeploymentResponse activityDeployResponse =
        controllerControl.installLiveActivity(activityDeployRequest);

    LiveActivityDeployResponseMessage rosResponseMessage =
        rosMessageFactory.newFromType(LiveActivityDeployResponseMessage._TYPE);

    RosLiveActivityDeploymentMessageTranslator.serializeDeploymentResponse(activityDeployResponse,
        rosResponseMessage);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_ACTIVITY_INSTALL,
        liveActivityDeployResponseSerializer.serialize(rosResponseMessage));
  }

  /**
   * Handle a live activity deletion request.
   *
   * @param rosRequestMessage
   *          the deletion request
   */
  private void handleLiveActivityDeletion(LiveActivityDeleteRequestMessage rosRequestMessage) {
    LiveActivityDeleteRequest liveActivityDeleteRequest =
        RosLiveActivityDeleteMessageTranslator
            .deserializeLiveActivityDeleteRequest(rosRequestMessage);

    LiveActivityDeleteResponse liveActivityDeleteResponse =
        controllerControl.deleteLiveActivity(liveActivityDeleteRequest);

    LiveActivityDeleteResponseMessage rosResponseMessage =
        rosMessageFactory.newFromType(LiveActivityDeleteResponseMessage._TYPE);

    RosLiveActivityDeleteMessageTranslator.serializeLiveActivityDeleteResponseMessage(
        liveActivityDeleteResponse, rosResponseMessage);

    ChannelBuffer payload = liveActivityDeleteResponseSerializer.serialize(rosResponseMessage);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_ACTIVITY_DELETE, payload);
  }

  /**
   * Get the communicator live activity delete status from the controller delete
   * status.
   *
   * @param controllerDeleteResponse
   *          the controller delete response
   *
   * @return the communicator live activity delete response
   */
  private LiveActivityDeleteResponseMessage getLiveActivityDeleteStatus(
      LiveActivityDeleteResponse controllerDeleteResponse) {
    LiveActivityDeleteResponseMessage rosMessage =
        rosMessageFactory.newFromType(LiveActivityDeleteResponseMessage._TYPE);

    RosLiveActivityDeleteMessageTranslator.serializeLiveActivityDeleteResponseMessage(
        controllerDeleteResponse, rosMessage);
    return rosMessage;
  }

  /**
   * Handle a ROS activity control request coming in.
   *
   * @param request
   *          The ROS request.
   */
  @VisibleForTesting
  void handleLiveActivityRuntimeRequest(final LiveActivityRuntimeRequest request) {
    spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        String uuid = request.getLiveActivityUuid();
        switch (request.getOperation()) {
          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STARTUP:
            controllerControl.startupLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_ACTIVATE:
            controllerControl.activateLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_DEACTIVATE:
            controllerControl.deactivateLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_SHUTDOWN:
            controllerControl.shutdownLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STATUS:
            controllerControl.statusLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CONFIGURE:
            handleLiveActivityConfigurationRequest(uuid,
                configurationRequestDeserializer.deserialize(request.getPayload()));

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_PERMANENT:
            controllerControl.cleanLiveActivityPermanentData(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_TMP:
            controllerControl.cleanLiveActivityTmpData(uuid);

            break;

          default:
            spaceEnvironment.getLog().error(
                String.format("Unknown ROS activity runtime request %d", request.getOperation()));
        }
      }
    });
  }

  /**
   * Handle a controller configuration request.
   *
   * @param configurationRequest
   *          the configuration request
   */
  private void handleControllerConfigurationRequest(ConfigurationRequest configurationRequest) {
    controllerControl.configureController(extractConfigurationUpdate(configurationRequest));
  }

  /**
   * Handle a live activity configuration request.
   *
   * @param uuid
   *          uuid of the live activity
   * @param configurationRequest
   *          the configuration request
   */
  private void handleLiveActivityConfigurationRequest(String uuid,
      ConfigurationRequest configurationRequest) {
    controllerControl.configureLiveActivity(uuid, extractConfigurationUpdate(configurationRequest));
  }

  /**
   * Extract a configuration map from the configuration request.
   *
   * @param configurationRequest
   *          the request
   *
   * @return map of the request
   */
  private Map<String, String> extractConfigurationUpdate(ConfigurationRequest configurationRequest) {
    Map<String, String> values = new HashMap<>();

    for (ConfigurationParameterRequest parameterRequest : configurationRequest.getParameters()) {
      if (parameterRequest.getOperation() == ConfigurationParameterRequest.OPERATION_ADD) {
        values.put(parameterRequest.getName(), parameterRequest.getValue());
      }
    }
    return values;
  }

  @Override
  public void publishActivityStatus(String uuid, ActivityStatus astatus) {
    try {
      LiveActivityRuntimeStatus status =
          rosMessageFactory.newFromType(LiveActivityRuntimeStatus._TYPE);
      status.setUuid(uuid);
      status.setStatus(translateActivityState(astatus.getState()));

      status.setStatusDetail(astatus.getCombinedDetail());

      publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS,
          liveActivityRuntimeStatusSerializer.serialize(status));
    } catch (Exception e) {
      spaceEnvironment.getLog()
          .error(
              String.format("Could not publish Status change %s for Live Activity %s\n", uuid,
                  astatus), e);
    }
  }

  /**
   * Publish a controller status update with a payload.
   *
   * @param statusCode
   *          the status code
   * @param payload
   *          the payload, can be {@code null}
   */
  private void publishControllerStatus(int statusCode, ChannelBuffer payload) {
    ControllerStatus status = newControllerStatus(statusCode);

    if (payload != null) {
      status.setPayload(payload);
    }

    controllerStatusPublisher.publish(status);
  }

  /**
   * Create a new {@link ControllerStatus} object with most fields filled in.
   *
   * @param statusCode
   *          the status code
   *
   * @return a newly created status message
   */
  private ControllerStatus newControllerStatus(int statusCode) {
    ControllerStatus status = rosMessageFactory.newFromType(ControllerStatus._TYPE);
    status.setControllerUuid(controllerControl.getControllerInfo().getUuid());
    status.setStatus(statusCode);
    return status;
  }

  /**
   * Translate an Smart Spaces activity status to its ROS message equivalent.
   *
   * @param state
   *          object
   * @return status code
   */
  private int translateActivityState(ActivityState state) {
    switch (state) {
      case UNKNOWN:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN;

      case READY:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY;

      case RUNNING:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_RUNNING;

      case ACTIVE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVE;

      case CRASHED:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_CRASH;

      case STARTUP_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_ATTEMPT;

      case STARTUP_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_FAILURE;

      case ACTIVATE_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_ATTEMPT;

      case ACTIVATE_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_FAILURE;

      case DEACTIVATE_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_ATTEMPT;

      case DEACTIVATE_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_FAILURE;

      case SHUTDOWN_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_ATTEMPT;

      case SHUTDOWN_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_FAILURE;

      case DEPLOY_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_ATTEMPT;

      case DEPLOY_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_FAILURE;

      case DOESNT_EXIST:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DOESNT_EXIST;

      default:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN;
    }
  }

  /**
   * Set the Ros Environment the controller should run in.
   *
   * @param rosEnvironment
   *          the ros environment for this communicator
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * @param controllerControl
   *          the controllerControl to set
   */
  @Override
  public void setSpaceControllerControl(SpaceControllerControl controllerControl) {
    this.controllerControl = controllerControl;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Give heartbeats from controller using ROS.
   *
   * @author Keith M. Hughes
   */
  private class RosControllerHeartbeat implements SpaceControllerHeartbeat {

    /**
     * heartbeatLoop status is always the same, so create once.
     */
    private final ControllerStatus status;

    /**
     * Construct a heartbeat object.
     */
    public RosControllerHeartbeat() {
      status = rosMessageFactory.newFromType(ControllerStatus._TYPE);
      status.setStatus(ControllerStatus.STATUS_CONTROLLER_HEARTBEAT);
    }

    @Override
    public void sendHeartbeat() {
      // In case the UUID changed.
      status.setControllerUuid(controllerControl.getControllerInfo().getUuid());
      controllerStatusPublisher.publish(status);
    }
  }
}
