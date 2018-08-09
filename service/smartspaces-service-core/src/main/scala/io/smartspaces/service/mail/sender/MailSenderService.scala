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

package io.smartspaces.service.mail.sender

import java.util.{ Map => JMap }

import io.smartspaces.service.SupportedService
import io.smartspaces.service.mail.common.MailMessage
import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.logging.ExtendedLog

object MailSenderService {

  /**
   * The name of the service.
   */
  val SERVICE_NAME = "mail.sender"

  /**
   * Configuration property for SMTP configuration. This will be a JSON object.
   */
  val CONFIGURATION_NAME_MAIL_SMTP = "smartspaces.service.mail.sender.smtp"

  /**
   * Configuration property for SMTP host smart spaces should use.
   */
  val PROPERTY_NAME_HOST = "host"

  /**
   * Configuration property for SMTP host port smart spaces should use.
   */
  val PROPERTY_NAME_PORT = "port"

  /**
   * The default value for the {@link #PROPERTY_NAME_PORT} parameter.
   */
  val PROPERTY_VALUE_DEFAULT_PORT = "25"

  /**
   * Configuration property for SMTP to use TLS.
   */
  val PROPERTY_NAME_USE_TLS = "useTls"

  /**
   * Configuration property for SMTP username.
   */
  val PROPERTY_NAME_USERNAME = "username"

  /**
   * Configuration property for SMTP password.
   */
  val PROPERTY_NAME_PASSWORD = "password"

}
/**
 * A Smart Spaces service for sending email.
 *
 * @author Keith M. Hughes
 */
trait MailSenderService extends SupportedService {
  
  /**
   * Get a mail endpoint using the smartspaces configuration parameter name.
   * 
   * @param log
   *        the log to use for sending the email
   * 
   * @return the sender endpoint
   */
  def newMailSenderEndpoint(log: ExtendedLog): MailSenderEndpoint

  /**
   * Get a mail endpoint using the supplied connection properties.
   * 
   * @param props
   *        the properties for configuring the client
   * @param log
   *        the log to use for sending the email
   * 
   * @return the sender endpoint
   */
  def newMailSenderEndpoint(propa: JMap[String, Object], log: ExtendedLog): MailSenderEndpoint
}

/**
 * An endpoint for sending email.
 *
 * @author Keith M. HUghes
 */
trait MailSenderEndpoint extends ManagedResource {

  /**
   * Send a mail message.
   *
   * @param message
   *          the message to send
   */
  def sendMailMessage(message: MailMessage): Unit
}
