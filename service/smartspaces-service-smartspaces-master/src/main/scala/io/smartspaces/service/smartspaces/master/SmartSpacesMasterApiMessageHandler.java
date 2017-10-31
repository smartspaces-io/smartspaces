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

import java.util.Map;

/**
 * A handler for incoming messages from the Smart Spaces Master API.
 *
 * @author Keith M. Hughes
 */
public interface SmartSpacesMasterApiMessageHandler {

  /**
   * Handle a message from the Master.
   *
   * @param client
   *          the client which got the message from the Master
   * @param message
   *          the message
   */
  void onNewMasterApiMessage(SmartSpacesMasterClient client, Map<String, Object> message);
}
