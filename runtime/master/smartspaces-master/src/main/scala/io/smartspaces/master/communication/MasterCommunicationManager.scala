/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.master.communication;

import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.service.web.server.WebServer;

/**
 * The manager for master communications.
 *
 * @author Keith M. Hughes
 */
trait MasterCommunicationManager extends ManagedResource {

  /**
   * Get the master web server.
   *
   * @return the master web server
   */
  def getWebServer(): WebServer

  /**
   * Register a handler with the manager.
   *
   * @param handler
   *        the handler to register
   */
  def registerHander(handler: MasterCommunicationHandler): Unit
}
