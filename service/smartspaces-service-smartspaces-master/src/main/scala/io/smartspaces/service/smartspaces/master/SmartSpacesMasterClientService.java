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

package io.smartspaces.service.smartspaces.master;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A service for obtaining clients to work with the Smart Spaces Master API.
 *
 * @author Keith M. Hughes
 */
public interface SmartSpacesMasterClientService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "smartspaces.master.client";

  /**
   * Create a new Smart Spaces Master client.
   *
   * <p>
   * This connects to the web socket Master API.
   *
   * @param masterApiHost
   *          the hostname for the machine running the Smart Spaces Master API
   * @param masterApiPort
   *          the port on the machine running the Smart Spaces Master API
   * @param log
   *          the logger for this connection
   *
   * @return the new client
   */
  SmartSpacesMasterClient newMasterClient(String masterApiHost, int masterApiPort, ExtendedLog log);
}
