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

package io.smartspaces.service.mail.receiver;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A Smart Spaces service for receiving email.
 *
 * @author Keith M. Hughes
 */
public interface MailReceiverService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "mail.receiver";

  /**
   * Create a new mail receiver listening on a given port.
   *
   * @param port
   *          the port for the mail receiver to listen on
   * @param log
   *          the logger to use
   *
   * @return the new mail receiver
   */
  MailReceiver newMailReceiver(int port, ExtendedLog log);
}
