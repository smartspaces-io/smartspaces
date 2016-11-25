/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.container.control.newmessage;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.util.data.json.JsonSmartSpacesException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Keith M. Hughes
 */
public class StandardMasterSpaceControllerCodec {

  public static final String CONTROLLER_MESSAGE_CONTROLLER_UUID = "controllerUuid";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE = "statusType";

  public static final String CONTROLLER_MESSAGE_STATUS_CODE = "statusCode";

  public static final String CONTROLLER_MESSAGE_STATUS_DETAIL = "statusDetail";

  public static final String CONTROLLER_MESSAGE_PAYLOAD = "payload";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_CONTROLLER_FULL_STATUS =
      "controllerFullStatus";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_HEARTBEAT = "heartbeat";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_LIVE_ACTIVITY_RUNTIME_STATUS =
      "liveActivityRuntimeStatus";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_INSTALL = "activityInstall";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_ACTIVITY_DELETE = "activityDelete";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_DATA_CAPTURE = "dataCapture";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_DATA_RESTORE = "dataRestore";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_QUERY =
      "containerResourceQuery";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_CONTAINER_RESOURCE_COMMIT =
      "containerResourceCommit";

  public static final String CONTROLLER_MESSAGE_STATUS_TYPE_SHUTDOWN = "shutdown";

  /**
   * Request the full status of the controller.
   */
  public static final String OPERATION_CONTROLLER_STATUS = "status";

  /**
   * The field in a controller message that gives the operation requested.
   */
  public static final String MESSAGE_CONTROLLER_OPERATION_OPERATION = "operation";

  /**
   * Shut down the entire controller.
   */
  public static final String OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER = "shutdownController";

  /**
   * Shut down all activities in the controller.
   */
  public static final String OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES = "shutdownActivities";

  /**
   * Deploy a live activity on the controller.
   */
  public static final String OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY = "deployLiveActivity";

  /**
   * Delete a live activity from the controller.
   */
  public static final String OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY = "deleteLiveActivity";

  /**
   * A request for a live activity The payload will be a
   * LiveActivityRuntimeRequest.
   */
  public static final String OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST =
      "liveActivityRuntimeRequest";

  /**
   * Clean the controller's tmp data folder.
   */
  public static final String OPERATION_CONTROLLER_CLEAN_DATA_TMP = "cleanDataTmp";

  /**
   * Clean the controller's permanent data folder.
   */
  public static final String OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT = "cleanDataPermanent";

  /**
   * Clean the temp data of all live activities on the controller.
   */
  public static final String OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES =
      "cleanDataTmpActivities";

  /**
   * Clean the permanent data of all live activities on the controller.
   */
  public static final String OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES =
      "cleanDataPermanentActivities";

  /**
   * Initiate capture for a controller/activity data bundle.
   * 
   * <p>
   * Payload will be a ControllerDataRequest message.
   */
  public static final String OPERATION_CONTROLLER_CAPTURE_DATA = "captureData";

  /**
   * Initiate restore for a controller/activity data bundle.
   * 
   * <p>
   * Payload will be a ControllerDataRequest message.
   */
  public static final String OPERATION_CONTROLLER_RESTORE_DATA = "restoreData";

  /**
   * Query whether a set of resources are on the controller.
   * 
   * <p>
   * Payload will be a ContainerResourceQueryRequest
   */
  public static final String OPERATION_CONTROLLER_RESOURCE_QUERY = "resourceQuery";

  /**
   * Commit a set of resources to the controller.
   * 
   * <p>
   * Payload will be a ContainerResourceQueryRequest
   */
  public static final String OPERATION_CONTROLLER_RESOURCE_COMMIT = "resourceCommit";

  /**
   * Configure the controller.
   * 
   * <p>
   * Payload will be a ConfigurationRequest
   */
  public static final String OPERATION_CONTROLLER_CONFIGURE = "configure";

  public static final byte[][] DELIMITERS = new byte[][] { new byte[] { '\n', '\n' } };

  public static final Charset CHARSET = Charsets.UTF_8;

  public static final int CONTROLLER_SERVER_PORT = 8100;

  /**
   * The JSON mapper.
   */
  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.getFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
  }

  public Map<String, Object> encodeBaseControllerRequestMessage(String operation,
      Object payload) {
    Map<String, Object> message = new HashMap<>();
    message.put(MESSAGE_CONTROLLER_OPERATION_OPERATION, operation);
 
    if (payload != null) {
      try {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = MAPPER.convertValue(payload, Map.class);
        message.put(CONTROLLER_MESSAGE_PAYLOAD, value);
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return message;
  }

  public Map<String, Object> encodeBaseControllerStatusMessage(String statusType, String controllerUuid,
      Object payload) {
    Map<String, Object> message = new HashMap<>();
    message.put(CONTROLLER_MESSAGE_CONTROLLER_UUID, controllerUuid);
    message.put(CONTROLLER_MESSAGE_STATUS_TYPE, statusType);

    if (payload != null) {
      try {
        @SuppressWarnings("unchecked")
        Map<String, Object> value = MAPPER.convertValue(payload, Map.class);
        message.put(CONTROLLER_MESSAGE_PAYLOAD, value);
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return message;
  }

  public String encodeFinalMessage(Map<String, Object> message) {
    try {
      return MAPPER.writeValueAsString(message) + "\n\n";
    } catch (Throwable e) {
      throw new JsonSmartSpacesException("Could not serialize JSON object as string", e);
    }

  }

  public Map<String, Object> parseMessage(String message) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> value = MAPPER.readValue(message, Map.class);
      return value;
    } catch (Throwable e) {
      throw new JsonSmartSpacesException("Could not parse JSON string", e);
    }
  }

  @SuppressWarnings("unchecked")
  public ControllerFullStatus decodeControllerFullStatus(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        ControllerFullStatus value = MAPPER.convertValue(payload, ControllerFullStatus.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public LiveActivityRuntimeStatus decodeLiveActivityRuntimeStatus(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        LiveActivityRuntimeStatus value =
            MAPPER.convertValue(payload, LiveActivityRuntimeStatus.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public ContainerResourceDeploymentQueryResponse
      decodeContainerResourceDeploymentQueryResponse(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        ContainerResourceDeploymentQueryResponse value =
            MAPPER.convertValue(payload, ContainerResourceDeploymentQueryResponse.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public ContainerResourceDeploymentCommitResponse
      decodeContainerResourceDeploymentCommitResponse(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        ContainerResourceDeploymentCommitResponse value =
            MAPPER.convertValue(payload, ContainerResourceDeploymentCommitResponse.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public LiveActivityDeploymentResponse
      decodeLiveActivityDeploymentResponse(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        LiveActivityDeploymentResponse value =
            MAPPER.convertValue(payload, LiveActivityDeploymentResponse.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public LiveActivityDeleteResponse decodeLiveActivityDeleteResponse(Map<String, Object> message) {
    Map<String, Object> payload = (Map<String, Object>) message.get(CONTROLLER_MESSAGE_PAYLOAD);

    if (payload != null) {
      try {
        LiveActivityDeleteResponse value =
            MAPPER.convertValue(payload, LiveActivityDeleteResponse.class);
        return value;
      } catch (Throwable e) {
        throw new JsonSmartSpacesException("Could not parse JSON string", e);
      }

    }

    return null;
  }

}
