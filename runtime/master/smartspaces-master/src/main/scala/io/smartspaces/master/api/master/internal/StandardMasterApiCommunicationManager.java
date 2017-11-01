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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.activity.ActivityState;
import io.smartspaces.master.api.master.MasterApiActivityManager;
import io.smartspaces.master.api.master.MasterApiAutomationManager;
import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.api.master.MasterApiSpaceControllerManager;
import io.smartspaces.master.api.master.MasterApiCommunicationManager;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.communication.MasterCommunicationManager;
import io.smartspaces.master.event.BaseMasterEventListener;
import io.smartspaces.master.event.MasterEventListener;
import io.smartspaces.master.event.MasterEventManager;
import io.smartspaces.master.server.services.ExtensionManager;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.messaging.CaptureMessageSender;
import io.smartspaces.messaging.MessageSender;
import io.smartspaces.messaging.codec.MapByteArrayMessageCodec;
import io.smartspaces.messaging.codec.MapStringMessageCodec;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;
import io.smartspaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory;
import io.smartspaces.service.web.server.HttpDynamicPostRequestHandler;
import io.smartspaces.service.web.server.HttpPostBody;
import io.smartspaces.service.web.server.HttpRequest;
import io.smartspaces.service.web.server.HttpResponse;
import io.smartspaces.service.web.server.MultipleConnectionWebServerWebSocketHandlerFactory;
import io.smartspaces.service.web.server.MultipleConnectionWebSocketHandler;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.web.CommonMimeTypes;
import io.smartspaces.util.web.HttpResponseCode;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.util.Map;

/**
 * A basic {@link MasterApiCommunicationManager} implementation.
 *
 * <p>
 * At the moment this only sends activity and controller events to anyone
 * listening.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiCommunicationManager extends BaseMasterApiManager
    implements MasterApiCommunicationManager, MultipleConnectionWebSocketHandler<Map<String, Object>> {

  /**
   * The file name prefix for an activity upload.
   */
  public static final String ACTIVITY_UPLOAD_NAME_PREFIX = "activity-";

  /**
   * The file name suffix for an activity upload.
   */
  public static final String ACTIVITY_UPLOAD_NAME_SUFFIX = ".upload";

  /**
   * The space environment.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Web socket handler for the connection to the browser.
   */
  private MultipleConnectionWebServerWebSocketHandlerFactory<Map<String, Object>> webSocketHandlerFactory;

  /**
   * The manager for extensions.
   */
  private ExtensionManager extensionManager;

  /**
   * The Master API manager for activities.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API manager for controllers.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API manager for automation.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  /**
   * The Master API manager for master support.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * The master communication manager.
   */
  private MasterCommunicationManager masterCommunicationManager;
  
  /**
   * Message codec for POST messages.
   */
  private MapByteArrayMessageCodec messageCodec = new MapByteArrayMessageCodec();

  /**
   * The master event listener.
   */
  private MasterEventListener masterEventListener = new BaseMasterEventListener() {
    @Override
    public void onLiveActivityStateChange(ActiveLiveActivity liveActivity, ActivityState oldState,
        ActivityState newState) {
      handleLiveActivityStateChange(liveActivity, oldState, newState);
    }
  };

  /**
   * The master event manager.
   */
  private MasterEventManager masterEventManager;

  /**
   * The command processor for the commands that come in.
   */
  private MasterApiCommandProcessor masterApiCommandProcessor;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * The JSON mapper to use.
   */
  private JsonMapper jsonMapper = StandardJsonMapper.INSTANCE;

  @Override
  public void startup() {
    WebServer webServer = masterCommunicationManager.getWebServer();

    webSocketHandlerFactory =
        new BasicMultipleConnectionWebServerWebSocketHandlerFactory<Map<String, Object>>(this,
            spaceEnvironment.getLog());

    webServer.setWebSocketHandlerFactory(MasterApiCommunicationManager.MASTERAPI_WEBSOCKET_URI_PREFIX,
        webSocketHandlerFactory, new MapStringMessageCodec());

    masterEventManager.addListener(masterEventListener);

    masterApiCommandProcessor = new StandardMasterApiCommandProcessor(masterApiActivityManager,
        masterApiSpaceControllerManager, masterApiAutomationManager, masterApiMasterSupportManager,
        extensionManager, spaceEnvironment.getTimeProvider(), webSocketHandlerFactory,
        spaceEnvironment.getLog());

    webServer.addDynamicPostRequestHandler(MASTERAPI_PATH_PREFIX_ACTIVITY_UPLOAD, false,
        new HttpDynamicPostRequestHandler() {
          @Override
          public void handle(HttpRequest request, HttpPostBody upload, HttpResponse response) {
            handleMasterApiActivityUpload(request, upload, response);
          }
        });

    webServer.addDynamicPostRequestHandler("/masterapi/post", false,
        new HttpDynamicPostRequestHandler() {
          @Override
          public void handle(HttpRequest request, HttpPostBody upload, HttpResponse response) {
            handleMasterApiPostCommand(request, upload, response);
          }
        });
  }

  @Override
  public void shutdown() {
    masterEventManager.removeListener(masterEventListener);
  }

  /**
   * Handle an activity upload.
   *
   * @param request
   *          the HTTP request
   * @param upload
   *          the file upload
   * @param response
   *          the HTTP response to write back
   */
  private void handleMasterApiActivityUpload(HttpRequest request, HttpPostBody upload,
      HttpResponse response) {
    File tempFile = null;
    try {
      tempFile =
          fileSupport.createTempFile(ACTIVITY_UPLOAD_NAME_PREFIX, ACTIVITY_UPLOAD_NAME_SUFFIX);
      upload.moveTo(tempFile);

      Map<String, Object> activityResponse =
          masterApiActivityManager.saveActivity(null, fileSupport.newFileInputStream(tempFile));

      writeActivityUploadResponse(response, activityResponse);
    } catch (Throwable e) {
      Map<String, Object> failureResponse = SmartSpacesMessagesSupport
          .getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);

      spaceEnvironment.getLog().error("Could not upload activity via Master API\n"
          + SmartSpacesMessagesSupport.getResponseDetail(failureResponse));

      writeActivityUploadResponse(response, failureResponse);
    } finally {
      if (tempFile != null) {
        fileSupport.delete(tempFile);
      }
    }
  }

  /**
   * Handle an activity upload.
   *
   * @param request
   *          the HTTP request
   * @param postBody
   *          the post body
   * @param response
   *          the HTTP response to write back
   */
  private void handleMasterApiPostCommand(HttpRequest request, HttpPostBody postBody,
      HttpResponse response) {
    CaptureMessageSender<Map<String, Object>> captureSender =
        new CaptureMessageSender<Map<String, Object>>();
    
    try {
      masterApiCommandProcessor.handleApiCall(messageCodec.decode(postBody.getContent()), captureSender);

      response.setContentType(CommonMimeTypes.MIME_TYPE_APPLICATION_JSON);
      response.getOutputStream().write(messageCodec.encode(captureSender.capturedMessage()));
    } catch (Throwable e) {
      spaceEnvironment.getLog().formatError(e,  "Master API POST command failed");
      
      response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Write the activity upload response.
   *
   * @param response
   *          the HTTP response
   * @param activityResponse
   *          the API response
   */
  private void writeActivityUploadResponse(HttpResponse response,
      Map<String, Object> activityResponse) {
    try {
      response.setResponseCode(
          SmartSpacesMessagesSupport.isSuccessResponse(activityResponse) ? HttpResponseCode.OK
              : HttpResponseCode.INTERNAL_SERVER_ERROR);
      response.setContentType(CommonMimeTypes.MIME_TYPE_APPLICATION_JSON);
      response.getOutputStream().write(jsonMapper.toString(activityResponse).getBytes());
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Could not write response for upload activity via Master API",
          e);
    }
  }

  /**
   * Handle the state change for a live activity from the master event bus.
   *
   * @param activeLiveActivity
   *          the live activity
   * @param oldState
   *          the old state of the live activity
   * @param newState
   *          the new state of the live activity
   */
  private void handleLiveActivityStateChange(ActiveLiveActivity activeLiveActivity,
      ActivityState oldState, ActivityState newState) {
    masterApiCommandProcessor.sendLiveActivityStateChangeMessage(activeLiveActivity, oldState,
        newState);
  }

  @Override
  public void handleNewWebSocketConnection(String channelId) {
    spaceEnvironment.getLog().formatInfo("New web socket connection %s", channelId);
  }

  @Override
  public void handleWebSocketClose(String channelId) {
    spaceEnvironment.getLog().formatInfo("Closed web socket connection %s", channelId);
  }

  @Override
  public void handleNewWebSocketMessage(String channelId, Map<String, Object> message) {
    MessageSender<Map<String, Object>> responseMessageSender =
        webSocketHandlerFactory.getChannelMessageSender(channelId);

    masterApiCommandProcessor.handleApiCall(message, responseMessageSender);
  }

  /**
   * Get the master event listener.
   *
   * @return the master event listener
   */
  @VisibleForTesting
  MasterEventListener getMasterEventListener() {
    return masterEventListener;
  }

  /**
   * Set the master event manager.
   *
   * @param masterEventManager
   *          the master event manager
   */
  public void setMasterEventManager(MasterEventManager masterEventManager) {
    this.masterEventManager = masterEventManager;
  }

  /**
   * Set the master communication manager.
   *
   * @param masterCommunicationManager
   *          the master communication manager
   */
  public void setMasterCommunicationManager(MasterCommunicationManager masterCommunicationManager) {
    this.masterCommunicationManager = masterCommunicationManager;
  }

  /**
   * Set the websocket handler factory.
   *
   * @param websocketHandlerFactory
   *          the websocket handler factory
   */
  @VisibleForTesting
  void setWebSocketHandlerFactory(
      MultipleConnectionWebServerWebSocketHandlerFactory<Map<String, Object>> websocketHandlerFactory) {
    this.webSocketHandlerFactory = websocketHandlerFactory;
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  @Override
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param extensionManager
   *          the extensionManager to set
   */
  public void setExtensionManager(ExtensionManager extensionManager) {
    this.extensionManager = extensionManager;
  }

  /**
   * Set the master API activity manager.
   *
   * @param masterApiActivityManager
   *          the master api activity manager
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * Set the master API controller manager.
   *
   * @param masterApiControllerManager
   *          the master API controller manager
   */
  public void setMasterApiSpaceControllerManager(
      MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }

  /**
   * Set the Master API Manager for automation.
   *
   * @param masterApiAutomationManager
   *          the manager
   */
  public void setMasterApiAutomationManager(MasterApiAutomationManager masterApiAutomationManager) {
    this.masterApiAutomationManager = masterApiAutomationManager;
  }

  /**
   * Set the Master API Manager for Master Support.
   *
   * @param masterApiMasterSupportManager
   *          the manager
   */
  public void setMasterApiMasterSupportManager(
      MasterApiMasterSupportManager masterApiMasterSupportManager) {
    this.masterApiMasterSupportManager = masterApiMasterSupportManager;
  }
}
