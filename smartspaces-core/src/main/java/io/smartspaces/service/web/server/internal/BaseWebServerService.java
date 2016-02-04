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

package io.smartspaces.service.web.server.internal;

import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.service.web.server.WebServerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Support for creating an instance of a {@link WebServerService}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseWebServerService extends BaseSupportedService implements WebServerService {

  /**
   * Map from server name to server.
   */
  private List<WebServer> servers = new ArrayList<>();

  /**
   * Add in a new server to the list.
   *
   * <p>
   * This is expected to be synchronized in the calling method.
   *
   * @param server
   *          the server to add
   */
  protected void addServer(WebServer server) {
    servers.add(server);
  }

  @Override
  public synchronized void shutdown() {
    for (WebServer server : servers) {
      server.shutdown();
    }

    servers.clear();
  }

  @Override
  public synchronized WebServer getWebServer(String serverName) {
    for (WebServer server : servers) {
      if (serverName.equals(server.getServerName())) {
        return server;
      }
    }

    return null;
  }

  @Override
  public synchronized void shutdownServer(String serverName) {
    WebServer server = getWebServer(serverName);
    if (server != null) {
      server.shutdown();
      servers.remove(server);
    }
  }
}
