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

package io.smartspaces.spacecontroller.runtime.comm;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityState;
import io.smartspaces.activity.ActivityStatus;
import io.smartspaces.container.control.message.StandardMasterSpaceControllerCodec;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeRequestOperation;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeStatus;
import io.smartspaces.container.control.message.common.ConfigurationParameterRequest;
import io.smartspaces.container.control.message.common.ConfigurationRequest;
import io.smartspaces.container.control.message.common.ConfigurationParameterRequest.ConfigurationParameterRequestOperation;
import io.smartspaces.container.control.message.activity.LiveActivityRuntimeRequest;
import io.smartspaces.container.control.message.container.ControllerFullStatus;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ControllerDataRequest;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.liveactivity.runtime.LiveActivityRunner;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.master.server.remote.client.RemoteMasterServerClient;
import io.smartspaces.master.server.remote.client.internal.StandardRemoteMasterServerClient;
import io.smartspaces.service.comm.network.server.TcpServerClientConnection;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointService;
import io.smartspaces.service.comm.network.server.TcpServerRequest;
import io.smartspaces.spacecontroller.SpaceControllerStatus;
import io.smartspaces.spacecontroller.runtime.SpaceControllerCommunicator;
import io.smartspaces.spacecontroller.runtime.SpaceControllerControl;
import io.smartspaces.spacecontroller.runtime.SpaceControllerDataOperation;
import io.smartspaces.spacecontroller.runtime.SpaceControllerHeartbeat;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.SmartSpacesUtilities;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link SpaceControllerCommunicator} using TCP for communication.
 *
 * @author Keith M. Hughes
 */
public class SimpleTcpSpaceControllerCommunicator implements SpaceControllerCommunicator {

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
   * The message codec for controller/master communications.
   */
  private StandardMasterSpaceControllerCodec messageCodec =
      new StandardMasterSpaceControllerCodec();

  /**
   * The space environment for this communicator.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The server for controller control and status updates.
   */
  private TcpServerNetworkCommunicationEndpoint<String> controllerAdminServer;

  private RemoteMasterServerClient masterServerClient;

  /**
   * Create a new space controller communicator.
   *
   * @param spaceEnvironment
   *          space environment
   */
  public SimpleTcpSpaceControllerCommunicator(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void onStartup(SimpleSpaceController controllerInfo) {
    TcpServerNetworkCommunicationEndpointService tcpService = spaceEnvironment.getServiceRegistry()
        .getRequiredService(TcpServerNetworkCommunicationEndpointService.SERVICE_NAME);
    controllerAdminServer = tcpService.newStringServer(
        StandardMasterSpaceControllerCodec.DELIMITERS, StandardMasterSpaceControllerCodec.CHARSET,
        controllerInfo.getHostControlPort(), spaceEnvironment.getLog());
    controllerAdminServer.addListener(new TcpServerNetworkCommunicationEndpointListener<String>() {

      @Override
      public void onTcpRequest(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerRequest<String> request) {
        handleControllerRequest(request.getMessage());
      }

      @Override
      public void onNewTcpConnection(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerClientConnection<String> connection) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onCloseTcpConnection(TcpServerNetworkCommunicationEndpoint<String> endpoint,
          TcpServerClientConnection<String> connection) {
        // TODO Auto-generated method stub

      }
    });
    controllerAdminServer.startup();

    masterServerClient = new StandardRemoteMasterServerClient(spaceEnvironment);
    masterServerClient.startup();
  }

  @Override
  public void registerControllerWithMaster(SimpleSpaceController controllerInfo) {
    masterServerClient.registerSpaceController(controllerInfo);
  }

  @Override
  public void onShutdown() {
    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_SHUTDOWN, null);
    SmartSpacesUtilities.delay(SHUTDOWN_DELAY);
    
    masterServerClient.shutdown();

    controllerAdminServer.shutdown();
  }

  @Override
  public SpaceControllerHeartbeat newSpaceControllerHeartbeat() {
    return new ControllerAdminServerSpaceControllerHeartbeat();
  }

  /**
   * Handle a controller control request coming in.
   *
   * @param request
   *          the request
   */
  @VisibleForTesting
  void handleControllerRequest(String request) {
    Map<String, Object> requestObject = messageCodec.parseMessage(request);

    String operation = (String) requestObject
        .get(StandardMasterSpaceControllerCodec.MESSAGE_CONTROLLER_OPERATION_OPERATION);

    switch (operation) {
      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_STATUS:
        publishControllerFullStatus();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES:
        controllerControl.shutdownAllLiveActivities();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER:
        controllerControl.shutdownControllerContainer();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY:
        handleLiveActivityDeployment(requestObject);

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY:
        handleLiveActivityDeletion(requestObject);

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST:
        handleLiveActivityRuntimeRequest(requestObject);

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_TMP:
        controllerControl.cleanControllerTempData();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT:
        controllerControl.cleanControllerPermanentData();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES:
        controllerControl.cleanControllerTempDataAll();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES:
        controllerControl.cleanControllerPermanentDataAll();

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CAPTURE_DATA:
        ControllerDataRequest captureDataRequest =
            messageCodec.decodeControllerDataRequest(requestObject);
        controllerControl.captureControllerDataBundle(captureDataRequest.getTransferUri());

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESTORE_DATA:
        ControllerDataRequest restoreDataRequest =
            messageCodec.decodeControllerDataRequest(requestObject);
        controllerControl.restoreControllerDataBundle(restoreDataRequest.getTransferUri());

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESOURCE_QUERY:
        handleContainerResourceQueryRequest(requestObject);

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESOURCE_COMMIT:
        handleContainerResourceCommitRequest(requestObject);

        break;

      case StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CONFIGURE:
        handleControllerConfigurationRequest(requestObject);

        break;

      default:
        spaceEnvironment.getLog()
            .error(String.format("Unknown space controller request %s", operation));
    }
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param requestObject
   *          the request object
   */
  private void handleContainerResourceQueryRequest(Map<String, Object> requestObject) {
    ContainerResourceDeploymentQueryRequest request =
        messageCodec.decodeContainerResourceDeploymentQueryRequest(requestObject);
    spaceEnvironment.getLog().info(String.format(
        "Got resource deployment query with transaction ID %s", request.getTransactionId()));

    ContainerResourceDeploymentQueryResponse queryResponse =
        controllerControl.handleContainerResourceDeploymentQueryRequest(request);
    spaceEnvironment.getLog()
        .info(String.format("Resource deployment query with transaction ID %s has status %s",
            request.getTransactionId(), queryResponse.getStatus()));

    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_QUERY,
        queryResponse);
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param requestObject
   *          the ROS request
   */
  private void handleContainerResourceCommitRequest(Map<String, Object> requestObject) {
    ContainerResourceDeploymentCommitRequest request =
        messageCodec.decodeContainerResourceDeploymentCommitRequest(requestObject);
    spaceEnvironment.getLog().info(String.format(
        "Got resource deployment commit with transaction ID %s", request.getTransactionId()));

    ContainerResourceDeploymentCommitResponse commitResponse =
        controllerControl.handleContainerResourceDeploymentCommitRequest(request);

    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_COMMIT,
        commitResponse);
  }

  /**
   * Create and publish controller full status.
   */
  private void publishControllerFullStatus() {
    spaceEnvironment.getLog().info("Getting full controller status");

    SimpleSpaceController controllerInfo = controllerControl.getControllerInfo();

    ControllerFullStatus fullStatus = new ControllerFullStatus();
    fullStatus.setName(controllerInfo.getName());
    fullStatus.setDescription(controllerInfo.getDescription());
    fullStatus.setHostId(controllerInfo.getHostId());

    List<LiveActivityRuntimeStatus> liveActivityStatuses = new ArrayList<>();
    fullStatus.setLiveActivityStatuses(liveActivityStatuses);
    for (InstalledLiveActivity activity : controllerControl.getAllInstalledLiveActivities()) {
      LiveActivityRuntimeStatus cas = new LiveActivityRuntimeStatus();
      liveActivityStatuses.add(cas);

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
        cas.setStatus(state);

        if (spaceEnvironment.getLog().isInfoEnabled()) {
          spaceEnvironment.getLog()
              .info(String.format("Full status live activity %s status %s, returning %s",
                  cas.getUuid(), state, cas.getStatus()));
        }
      } else {
        cas.setStatus(ActivityState.READY);
        spaceEnvironment.getLog().warn(String
            .format("Full status live activity %s not found, returning READY", cas.getUuid()));
      }
    }

    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_CONTROLLER_FULL_STATUS,
        fullStatus);
  }

  @Override
  public void publishControllerDataStatus(SpaceControllerDataOperation type,
      SpaceControllerStatus statusCode, Exception e) {

    // TODO(keith): This is icky. Needs to go into codec class
    String statusType = SpaceControllerDataOperation.DATA_CAPTURE.equals(type)
        ? StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_DATA_CAPTURE
        : StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_DATA_RESTORE;

    Map<String, Object> statusMsg = messageCodec.encodeBaseControllerStatusMessage(statusType,
        controllerControl.getControllerInfo().getUuid(), null);
    statusMsg.put(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_CODE,
        statusCode.getDescription());

    if (e != null) {
      statusMsg.put(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_DETAIL,
          SmartSpacesException.getStackTrace(e));
    }

    publishFullyCodedControllerStatus(statusMsg);
  }

  /**
   * Handle a live activity deployment request.
   *
   * @param deployRequest
   *          the deployment request
   */
  private void handleLiveActivityDeployment(Map<String, Object> requestObject) {
    LiveActivityDeploymentRequest activityDeployRequest =
        messageCodec.decodeLiveActivityDeploymentRequest(requestObject);

    LiveActivityDeploymentResponse activityDeployResponse =
        controllerControl.installLiveActivity(activityDeployRequest);

    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_INSTALL,
        activityDeployResponse);
  }

  /**
   * Handle a live activity deletion request.
   *
   * @param rosRequestMessage
   *          the deletion request
   */
  private void handleLiveActivityDeletion(Map<String, Object> requestObject) {
    LiveActivityDeleteRequest liveActivityDeleteRequest =
        messageCodec.decodeLiveActivityDeleteRequest(requestObject);

    LiveActivityDeleteResponse liveActivityDeleteResponse =
        controllerControl.deleteLiveActivity(liveActivityDeleteRequest);

    publishControllerStatus(
        StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_DELETE,
        liveActivityDeleteResponse);
  }

  /**
   * Handle an activity control request coming in.
   *
   * @param requestObject
   *          the request object
   */
  @VisibleForTesting
  void handleLiveActivityRuntimeRequest(Map<String, Object> requestObject) {
    final LiveActivityRuntimeRequest request =
        messageCodec.decodeLiveActivityRuntimeRequest(requestObject);
    spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        String uuid = request.getUuid();
        switch (request.getOperation()) {
          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_STARTUP:
            controllerControl.startupLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_ACTIVATE:
            controllerControl.activateLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_DEACTIVATE:
            controllerControl.deactivateLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_SHUTDOWN:
            controllerControl.shutdownLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_STATUS:
            controllerControl.statusLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CONFIGURE:
            handleLiveActivityConfigurationRequest(uuid, request.getConfigurationRequest());

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CLEAN_DATA_PERMANENT:
            controllerControl.cleanLiveActivityPermanentData(uuid);

            break;

          case LiveActivityRuntimeRequestOperation.LIVE_ACTIVITY_CLEAN_DATA_TMP:
            controllerControl.cleanLiveActivityTmpData(uuid);

            break;

          default:
            spaceEnvironment.getLog().error(
                String.format("Unknown activity runtime request %s", request.getOperation()));
        }
      }
    });
  }

  /**
   * Handle a controller configuration request.
   *
   * @param requestObject
   *          the configuration request
   */
  private void handleControllerConfigurationRequest(Map<String, Object> requestObject) {
    ConfigurationRequest request = messageCodec.decodeConfigurationRequest(requestObject);

    controllerControl.configureController(extractConfigurationUpdate(request));
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
  private Map<String, String>
      extractConfigurationUpdate(ConfigurationRequest configurationRequest) {
    Map<String, String> values = new HashMap<>();

    for (ConfigurationParameterRequest parameterRequest : configurationRequest.getParameters()) {
      if (parameterRequest.getOperation() == ConfigurationParameterRequestOperation.ADD) {
        values.put(parameterRequest.getName(), parameterRequest.getValue());
      }
    }
    return values;
  }

  @Override
  public void publishActivityStatus(String uuid, ActivityStatus astatus) {
    try {
      LiveActivityRuntimeStatus status = new LiveActivityRuntimeStatus();
      status.setUuid(uuid);
      status.setStatus(astatus.getState());

      status.setStatusDetail(astatus.getCombinedDetail());

      publishControllerStatus(
          StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_LIVE_ACTIVITY_RUNTIME_STATUS,
          status);

      // LiveActivityRuntimeStatus status =
      // rosMessageFactory.newFromType(LiveActivityRuntimeStatus._TYPE);
      // status.setUuid(uuid);
      // status.setStatus(translateActivityState(astatus.getState()));
      //
      // status.setStatusDetail(astatus.getCombinedDetail());
      //
      // publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS,
      // liveActivityRuntimeStatusSerializer.serialize(status));
    } catch (Exception e) {
      spaceEnvironment.getLog().error(
          String.format("Could not publish Status change %s for Live Activity %s\n", uuid, astatus),
          e);
    }
  }

  /**
   * Publish a controller status update with a payload.
   *
   * @param statusType
   *          the type of the status message
   * @param payload
   *          the payload, can be {@code null}
   */
  private void publishControllerStatus(String statusType, Object payload) {
    String controllerUuid = controllerControl.getControllerInfo().getUuid();

    Map<String, Object> statusObject =
        messageCodec.encodeBaseControllerStatusMessage(statusType, controllerUuid, payload);

    publishFullyCodedControllerStatus(statusObject);
  }

  /**
   * Public a fully-encoded controller status.
   * 
   * @param statusObject
   *          the fully encoded status
   */
  private void publishFullyCodedControllerStatus(Map<String, Object> statusObject) {
    controllerAdminServer.writeMessageAllConnections(messageCodec.encodeFinalMessage(statusObject));
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
   * Give heartbeats from controller to anyone on the controller admin server.
   *
   * @author Keith M. Hughes
   */
  private class ControllerAdminServerSpaceControllerHeartbeat implements SpaceControllerHeartbeat {

    /**
     * Heartbeat status is always the same, so create once.
     */
    private final Map<String, Object> statusObject;

    /**
     * Construct a heartbeat object.
     */
    public ControllerAdminServerSpaceControllerHeartbeat() {
      statusObject = messageCodec.encodeBaseControllerStatusMessage(
          StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_STATUS_TYPE_HEARTBEAT, null, null);
    }

    @Override
    public void sendHeartbeat() {
      // In case the UUID changed.
      statusObject.put(StandardMasterSpaceControllerCodec.CONTROLLER_MESSAGE_CONTROLLER_UUID,
          controllerControl.getControllerInfo().getUuid());
      controllerAdminServer
          .writeMessageAllConnections(messageCodec.encodeFinalMessage(statusObject));
    }
  }
}
