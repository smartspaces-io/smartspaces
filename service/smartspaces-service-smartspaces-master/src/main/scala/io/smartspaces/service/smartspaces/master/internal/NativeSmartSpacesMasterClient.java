/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.service.smartspaces.master.internal;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.messaging.codec.MapStringMessageCodec;
import io.smartspaces.messaging.dynamic.SmartSpacesMessages;
import io.smartspaces.service.smartspaces.master.SmartSpacesMasterApiMessageHandler;
import io.smartspaces.service.smartspaces.master.SmartSpacesMasterClient;
import io.smartspaces.service.web.WebSocketMessageHandler;
import io.smartspaces.service.web.client.WebSocketClient;
import io.smartspaces.service.web.client.WebSocketClientService;
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of the Smart Spaces Master API Client.
 *
 * @author Keith M. Hughes
 */
public class NativeSmartSpacesMasterClient implements SmartSpacesMasterClient {

  /**
   * Web socket URI prefix for the master API.
   */
  public static final String MASTERAPI_WEBSOCKET_URI_PREFIX = "/masterapi/websocket";

  /**
   * The web socket client for communicating with the master.
   */
  private final WebSocketClient<Map<String, Object>> webSocketClient;

  /**
   * Log for the endpoint.
   */
  private final ExtendedLog log;

  /**
   * The map of request IDs to their requests.
   */
  private final Map<String, Request> requestIdToRequest = Maps.newConcurrentMap();

  /**
   * The generator for request IDs.
   */
  private final AtomicLong requestIdGenerator = new AtomicLong();

  /**
   * The URL the client is pointing to.
   */
  private String masterAddress;

  /**
   * The list of callbacks for event messages from the master.
   */
  private List<SmartSpacesMasterApiMessageHandler> eventMessageHandlers =
      Lists.newCopyOnWriteArrayList();

  /**
   * Construct a new endpoint.
   *
   * @param webSocketClientService
   *          the web socket client service
   * @param masterApiHost
   *          host for the Master API
   * @param masterApiPort
   *          port for the Master API
   * @param log
   *          the logger
   */
  @SuppressWarnings("unchecked")
  public NativeSmartSpacesMasterClient(WebSocketClientService webSocketClientService,
      String masterApiHost, int masterApiPort, ExtendedLog log) {
    masterAddress = "ws://" + masterApiHost + ":" + masterApiPort + MASTERAPI_WEBSOCKET_URI_PREFIX;
    this.webSocketClient = webSocketClientService.newWebSocketClient(masterAddress,
        new WebSocketMessageHandler<Map<String, Object>>() {

          @Override
          public void onNewMessage(Map<String, Object> data) {
            handleMasterMessage(data);
          }

          @Override
          public void onConnect() {
            handleMasterConnect();
          }

          @Override
          public void onClose() {
            handleMasterClose();
          }
        }, new MapStringMessageCodec(), log);
    this.log = log;

  }

  @Override
  public void startup() {
    log.info("Starting up Smart Spaces Master API Client");
    webSocketClient.startup();
  }

  @Override
  public void shutdown() {
    log.info("Shutting down Smart Spaces Master API Client");
    webSocketClient.shutdown();
  }

  @Override
  public String getMasterAddress() {
    return masterAddress;
  }

  @Override
  public void getAllLiveActivities(SmartSpacesMasterApiMessageHandler callback) {
    getAllLiveActivities(null, callback);
  }

  @Override
  public void getAllLiveActivities(String filter, SmartSpacesMasterApiMessageHandler callback) {
    sendApiCall(newFilteredSearchCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ALL,
        filter, callback));
  }

  @Override
  public void getLiveActivityFullView(String typedId, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW_FULL, typedId, callback);
  }

  @Override
  public void getLiveActivityConfiguration(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET, id,
        callback);
  }

  @Override
  public void setLiveActivityConfiguration(String id, Map<String, String> newConfiguration,
      SmartSpacesMasterApiMessageHandler callback) {
    DynamicObjectBuilder call = newEntityCall(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET, id, callback);
    call.newObject(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_CONFIG);
    for (Entry<String, String> entry : newConfiguration.entrySet()) {
      call.setProperty(entry.getKey(), entry.getValue());
    }
    sendApiCall(call);
  }

  @Override
  public void startupLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP, id, callback);
  }

  @Override
  public void activateLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE, id, callback);
  }

  @Override
  public void deactivateLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE, id, callback);
  }

  @Override
  public void shutdownLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN, id, callback);
  }

  @Override
  public void getLiveActivityGroupFullView(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW_FULL, id,
        callback);
  }

  @Override
  public void getAllLiveActivityGroups(SmartSpacesMasterApiMessageHandler callback) {
    getAllLiveActivityGroups(null, callback);
  }

  @Override
  public void getAllLiveActivityGroups(String filter, SmartSpacesMasterApiMessageHandler callback) {
    DynamicObjectBuilder builder = newFilteredSearchCall(
        MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ALL, filter, callback);

    sendApiCall(builder);
  }

  @Override
  public void startupLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP, id, callback);
  }

  @Override
  public void activateLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE, id, callback);
  }

  @Override
  public void deactivateLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE, id,
        callback);
  }

  @Override
  public void shutdownLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN, id, callback);
  }

  @Override
  public void getAllSpaces(SmartSpacesMasterApiMessageHandler callback) {
    getAllSpaces(null, callback);
  }

  @Override
  public void getSpaceFullView(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_VIEW_FULL, id, callback);
  }

  @Override
  public void getAllSpaces(String filter, SmartSpacesMasterApiMessageHandler callback) {
    DynamicObjectBuilder builder =
        newFilteredSearchCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_ALL, filter, callback);

    sendApiCall(builder);
  }

  @Override
  public void startupSpace(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_STARTUP, id, callback);
  }

  @Override
  public void activateSpace(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_ACTIVATE, id, callback);
  }

  @Override
  public void deactivateSpace(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_DEACTIVATE, id, callback);
  }

  @Override
  public void shutdownSpace(String id, SmartSpacesMasterApiMessageHandler callback) {
    sendEntityCall(MasterApiMessages.MASTER_API_COMMAND_SPACE_SHUTDOWN, id, callback);
  }

  @Override
  public void addEventMessageHandler(SmartSpacesMasterApiMessageHandler handler) {
    eventMessageHandlers.add(handler);
  }

  @Override
  public void removeEventMessageHandler(SmartSpacesMasterApiMessageHandler handler) {
    eventMessageHandlers.remove(handler);
  }

  /**
   * Send a method call to an entity on the master.
   *
   * @param method
   *          the method to be done
   * @param entityId
   *          the ID of the entity that the method should be done on
   * @param callback
   *          the callback to be run
   */
  private void sendEntityCall(String method, String entityId,
      SmartSpacesMasterApiMessageHandler callback) {
    sendApiCall(newEntityCall(method, entityId, callback));
  }

  /**
   * Create a search command message.
   *
   * @param searchCommand
   *          the search command
   * @param filter
   *          the filter for the search, can be {@code null}
   * @param callback
   *          the callback handler for the message
   *
   * @return a message builder in the data section of the message
   */
  private DynamicObjectBuilder newFilteredSearchCall(String searchCommand, String filter,
      SmartSpacesMasterApiMessageHandler callback) {
    DynamicObjectBuilder builder = newReponseCall(searchCommand, callback);

    if (filter != null) {
      builder.setProperty(MasterApiMessages.MASTER_API_PARAMETER_NAME_FILTER, filter);
    }

    return builder;
  }

  /**
   * Create a new entity call.
   *
   * @param command
   *          the command for the entity
   * @param entityId
   *          the ID for the entity
   * @param callback
   *          the callback to be executed when the method response comes back
   *
   * @return the request message in the data section
   */
  private DynamicObjectBuilder newEntityCall(String command, String entityId,
      SmartSpacesMasterApiMessageHandler callback) {
    DynamicObjectBuilder builder = newReponseCall(command, callback);
    builder.setProperty(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, entityId);

    return builder;
  }

  /**
   * Create a new call that requires a response.
   *
   * @param command
   *          the command to be run
   * @param callback
   *          the callback to be executed when the method response comes back
   *
   * @return the builder in the data section
   */
  private DynamicObjectBuilder newReponseCall(String command,
      SmartSpacesMasterApiMessageHandler callback) {
    Request request = newRequest(callback);

    DynamicObjectBuilder builder = new StandardDynamicObjectBuilder();
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, command);
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_REQUEST_ID, request.getRequestId());
    builder.newObject(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);

    return builder;
  }

  /**
   * Create a new request.
   *
   * @param callback
   *          the callback for the message response
   *
   * @return the request
   */
  private Request newRequest(SmartSpacesMasterApiMessageHandler callback) {
    String requestId = newRequestId();
    Request request = new Request(requestId, callback);
    requestIdToRequest.put(requestId, request);
    return request;
  }

  /**
   * Send a call to the API.
   *
   * @param builder
   *          the builder containing the message to send
   */
  private void sendApiCall(DynamicObjectBuilder builder) {
    webSocketClient.sendMessage(builder.toMap());
  }

  /**
   * Handle the connection response from the Master.
   */
  private void handleMasterConnect() {
    log.info("Master API Client is connected to the Smart Spaces master");
  }

  /**
   * handle a connection close from the Master.
   */
  private void handleMasterClose() {
    log.info("Master API Client is no longer connected to the Smart Spaces master");
  }

  /**
   * Handle a response from the master.
   *
   * @param message
   *          the response
   */
  private void handleMasterMessage(Map<String, Object> message) {
    String requestId = (String) message.get(SmartSpacesMessages.MESSAGE_ENVELOPE_REQUEST_ID);
    if (requestId != null) {
      Request request = requestIdToRequest.remove(requestId);
      if (request != null) {
        request.responseReceived(message);
      } else {
        log.warn(String.format("Received response with unknown request ID %s", requestId));
      }
    } else {
      handleEventMessage(message);
    }
  }

  /**
   * Handle an event message.
   *
   * @param message
   *          the message to handle
   */
  private void handleEventMessage(Map<String, Object> message) {
    for (SmartSpacesMasterApiMessageHandler handler : eventMessageHandlers) {
      try {
        handler.onNewMasterApiMessage(this, message);
      } catch (Throwable e) {
        log.error("Error handling Master API message", e);
      }
    }
  }

  /**
   * Generate a new request ID.
   *
   * @return a new request ID
   */
  private String newRequestId() {
    return Long.toHexString(requestIdGenerator.getAndIncrement());
  }

  @Override
  public String toString() {
    return "NativeSmartSpacesMasterClient [masterAddress=" + getMasterAddress() + "]";
  }

  /**
   * A request which has gone to the master.
   *
   * @author Keith M. Hughes
   */
  private class Request {

    /**
     * The ID of the request which went to the master.
     */
    private String requestId;

    /**
     * The callback to execute when the call comes back.
     */
    private SmartSpacesMasterApiMessageHandler callback;

    /**
     * Construct a new request.
     *
     * @param requestId
     *          the ID of the request which went to the master
     * @param callback
     *          the callback to execute when the call comes back
     */
    public Request(String requestId, SmartSpacesMasterApiMessageHandler callback) {
      this.requestId = requestId;
      this.callback = callback;
    }

    /**
     * Get the request ID for the call.
     *
     * @return the request ID for the call
     */
    public String getRequestId() {
      return requestId;
    }

    /**
     * Handle the response received.
     *
     * @param response
     *          the response
     */
    public void responseReceived(Map<String, Object> response) {
      if (callback != null) {
        callback.onNewMasterApiMessage(NativeSmartSpacesMasterClient.this, response);
      }
    }
  }
}
