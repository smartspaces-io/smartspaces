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

package io.smartspaces.master.server.remote;

/**
 * @author Keith M. Hughes
 */
public class RemoteMasterServerMessages {

  /**
   * The URI prefix for the handler for space controller operations in the
   * Remote Master server.
   */
  public static final String URI_PREFIX_MASTER_SPACECONTROLLER = "/master/spacecontroller";

  /**
   * The space controller method on the Remote Master server for registering
   * space controllers.
   */
  public static final String MASTER_SPACE_CONTROLLER_METHOD_REGISTER = "/register";

  /**
   * The field in the space controller registration method that provides the
   * data for the registration.
   */
  public static final String MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA = "data";

  /**
   * The failure response for a master method.
   */
  public static final String MASTER_METHOD_RESPONSE_FAILURE = "FAILURE";

  /**
   * The description field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_DESCRIPTION = "description";

  /**
   * The name field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_NAME = "name";

  /**
   * The host ID field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_HOST_ID = "hostId";

  /**
   * The UUID field of the space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_UUID = "uuid";

  /**
   * The content type for responses from the remote master.
   */
  public static final String REMOTE_MASTER_RESPONSE_CONTENT_TYPE = "text/plain";

  /**
   * The success response for a space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_SUCCESS = "SUCCESS";

  /**
   * The failure response for a space controller registration.
   */
  public static final String CONTROLLER_REGISTRATION_FAILURE = "FAILURE";

  /**
   * Configuration property for the network host of the master.
   *
   */
  public static final String CONFIGURATION_MASTER_HOST = "smartspaces.master.host";

  /**
   * Configuration property for the network port for master communications.
   *
   */
  public static final String CONFIGURATION_MASTER_COMMUNICATION_PORT =
      "smartspaces.master.communication.port";

  /**
   * Default value for configuration property for the network port for master
   * communications.
   */
  public static final int CONFIGURATION_MASTER_COMMUNICATION_PORT_DEFAULT = 8090;
}
