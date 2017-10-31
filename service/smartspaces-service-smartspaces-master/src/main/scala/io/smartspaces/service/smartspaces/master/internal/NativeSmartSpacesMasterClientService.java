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
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.smartspaces.master.SmartSpacesMasterClient;
import io.smartspaces.service.smartspaces.master.SmartSpacesMasterClientService;
import io.smartspaces.service.web.client.WebSocketClientService;

/**
 * An implementation of the service for obtaining Smart Spaces Master API
 * clients.
 *
 * @author Keith M. Hughes
 */
public class NativeSmartSpacesMasterClientService extends BaseSupportedService implements
    SmartSpacesMasterClientService {

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public SmartSpacesMasterClient newMasterClient(String masterApiHost, int masterApiPort,
      ExtendedLog log) {
    WebSocketClientService clientService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            WebSocketClientService.SERVICE_NAME);

    return new NativeSmartSpacesMasterClient(clientService, masterApiHost, masterApiPort, log);
  }
}
