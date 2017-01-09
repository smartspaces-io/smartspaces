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

package io.smartspaces.service.mail.sender;

import io.smartspaces.service.SupportedService;
import io.smartspaces.service.mail.common.MailMessage;

/**
 * A Smart Spaces service for sending email.
 *
 * @author Keith M. Hughes
 */
public interface MailSenderService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "mail.sender";

  /**
   * Configuration property for SMTP host smart spaces should use.
   */
  String CONFIGURATION_NAME_MAIL_SMTP_HOST = "smartspaces.service.mail.sender.smtp.host";

  /**
   * Configuration property for SMTP host port smart spaces should use.
   */
  String CONFIGURATION_NAME_MAIL_SMTP_PORT = "smartspaces.service.mail.sender.smtp.port";

  /**
   * The default value for the {@link #CONFIGURATION_NAME_MAIL_SMTP_PORT} parameter.
   */
  String CONFIGURATION_VALUE_DEFAULT_MAIL_SMTP_PORT = "25";

  /**
   * Send a mail message.
   *
   * @param message
   *          the message to send
   */
  void sendMailMessage(MailMessage message);
}
