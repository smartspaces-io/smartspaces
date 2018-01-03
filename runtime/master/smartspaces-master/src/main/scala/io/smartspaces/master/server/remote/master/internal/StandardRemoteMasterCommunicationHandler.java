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

package io.smartspaces.master.server.remote.master.internal;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.communication.MasterCommunicationManager;
import io.smartspaces.master.server.remote.RemoteMasterServerMessages;
import io.smartspaces.master.server.remote.master.RemoteMasterCommunicationHandler;
import io.smartspaces.master.server.remote.master.RemoteMasterServerListener;
import io.smartspaces.service.web.server.HttpDynamicGetRequestHandler;
import io.smartspaces.service.web.server.HttpRequest;
import io.smartspaces.service.web.server.HttpResponse;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.web.HttpResponseCode;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * A {@link RemoteMasterCommunicationHandler} using a web server.
 *
 * @author Keith M. Hughes
 */
public class StandardRemoteMasterCommunicationHandler implements RemoteMasterCommunicationHandler {

  /**
   * The JSON mapper to use for the server.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The space environment.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Logger for the controller.
   */
  private ExtendedLog log;

  /**
   * All listeners for master server events.
   */
  private List<RemoteMasterServerListener> listeners = Lists.newCopyOnWriteArrayList();

  @Override
  public void register(MasterCommunicationManager masterCommunicationManager) {
    masterCommunicationManager.getWebServer().addDynamicGetRequestHandler(
        RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER, true,
        new MyHttpDynamicRequestHandler());
  }

  @Override
  public void addListener(RemoteMasterServerListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(RemoteMasterServerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Register a space controller.
   *
   * @param spaceControllerRemoteAddress
   *          the remote address of the space controller
   * @param data
   *          the registration data
   */
  @SuppressWarnings("unchecked")
  private void registerSpaceController(SocketAddress spaceControllerRemoteAddress,
      Map<String, Object> data) {
    SpaceController controller = new SimpleSpaceController();
    controller.setUuid((String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_UUID));
    controller.setName((String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_NAME));
    controller.setDescription(
        (String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_DESCRIPTION));
    controller
        .setHostId((String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_ID));
    String hostName =
        (String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_NAME);
    if (hostName == null) {
      hostName = ((InetSocketAddress) spaceControllerRemoteAddress).getHostString();
    }
    controller.setHostName(hostName);
    controller.setHostControlPort(
        (Integer) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_CONTROL_PORT));
    controller.setMetadata(
        (Map<String,Object>) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_METADATA));

    log.formatInfo("Controller %s (Host ID: %s, Host: %s:%d) is online.", controller.getUuid(),
        controller.getHostId(), controller.getHostName(), controller.getHostControlPort());

    signalSpaceControllerRegistration(controller);
  }

  /**
   * Signal all listeners about a new controller registration.
   *
   * @param controller
   *          information about the controller
   */
  private void signalSpaceControllerRegistration(SpaceController controller) {
    spaceEnvironment.getExecutorService().submit(new Runnable() {

      @Override
      public void run() {
        log.info(listeners);
        for (RemoteMasterServerListener listener : listeners) {
          try {
            listener.onSpaceControllerRegistration(controller);
          } catch (Throwable e) {
            log.error("Exception while signaling space controller registration", e);
          }
        }
      }
    });
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
   * HTTP dynamic request handler for calls coming into the remote master
   * server.
   *
   * @author Keith M. Hughes
   */
  private class MyHttpDynamicRequestHandler implements HttpDynamicGetRequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response) {
      log.formatInfo("Got registration from %s", request.getRemoteAddress());
      String methodName = request.getUri().getPath()
          .substring(RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER.length());
      if (methodName.equals(RemoteMasterServerMessages.MASTER_SPACE_CONTROLLER_METHOD_REGISTER)) {
        handleSpaceControllerRegistration(request, response);
      } else {
        log.formatWarn("Received unknown remote master server method name %s", methodName);
        response.setResponseCode(HttpResponseCode.NOT_FOUND);
        try {
          response.getOutputStream()
              .write(RemoteMasterServerMessages.MASTER_METHOD_RESPONSE_FAILURE.getBytes());
        } catch (IOException e) {
          throw SmartSpacesException.newFormattedException(e,
              "Could not write failure response for remote master server request %s",
              request.getUri());
        }
      }
    }

    /**
     * Handle a space controller registration request.
     *
     * @param request
     *          the http request
     * @param response
     *          the http response
     */
    private void handleSpaceControllerRegistration(HttpRequest request, HttpResponse response) {
      try {
        String data = request.getUriQueryParameters()
            .get(RemoteMasterServerMessages.MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA);
        if (data != null) {
          Map<String, Object> registrationData = MAPPER.parseObject(data);
          registerSpaceController(request.getRemoteAddress(), registrationData);
        }
        response.setContentType(RemoteMasterServerMessages.REMOTE_MASTER_RESPONSE_CONTENT_TYPE);
        response.getOutputStream()
            .write(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_SUCCESS.getBytes());
      } catch (Throwable e) {
        response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
        try {
          response.getOutputStream()
              .write(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_FAILURE.getBytes());
        } catch (IOException e1) {
          throw SmartSpacesException.newFormattedException(e,
              "Could not write failure response for controller registration %s", request.getUri());
        }

        throw SmartSpacesException.newFormattedException(e,
            "Could not process controller registration %s", request.getUri());
      }
    }
  }

  /**
   * Set a group of listeners to the handler.
   * 
   * @param listeners
   *          the listeners
   */
  public void setListeners(List<RemoteMasterServerListener> listeners) {
    for (RemoteMasterServerListener listener : listeners) {
      addListener(listener);
    }
  }
}
