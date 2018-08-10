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

package io.smartspaces.service.mail.sender.internal;

import io.smartspaces.SimpleSmartSpacesException
import io.smartspaces.SmartSpacesException
import io.smartspaces.service.BaseSupportedService
import io.smartspaces.service.mail.common.MailMessage
import io.smartspaces.service.mail.sender.MailSenderService

import scala.collection.JavaConverters._

import java.util.{ Map => JMap }
import java.util.Properties

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.mail.sender.MailSenderEndpoint
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.util.SmartSpacesUtilities

object JavaxMailMailSenderService {

  /**
   * Configuration property for SMTP host smart spaces should use.
   */
  val MAIL_TRANSPORT_SMTP = "smtp"

  /**
   * The javax.mail property for setting the host for the remote SMTP server.
   */
  val PROPERTY_MAIL_SMTP_HOST = "mail.smtp.host"

  /**
   * The javax.mail property for setting the port for the remote SMTP server.
   */
  val PROPERTY_MAIL_SMTP_PORT = "mail.smtp.port"
}

/**
 * A {@link MailMessage} which uses Javax mail.
 *
 * @author Keith M. Hughes
 */
class JavaxMailMailSenderService extends BaseSupportedService with MailSenderService with IdempotentManagedResource {

  override def getName(): String = {
    MailSenderService.SERVICE_NAME
  }

  override def newMailSenderEndpoint(log: ExtendedLog): MailSenderEndpoint = {
    val smtpConfig: JMap[String, Object] =
      getSpaceEnvironment.getSystemConfiguration.getPropertyJson(MailSenderService.CONFIGURATION_NAME_MAIL_SMTP)

    newMailSenderEndpoint(smtpConfig, log)
  }

  override def newMailSenderEndpoint(smtpConfig: JMap[String, Object], log: ExtendedLog): MailSenderEndpoint = {
    new JavaxMailMailSenderEndpoint(smtpConfig, log)
  }
}

class JavaxMailMailSenderEndpoint(smtpConfig: JMap[String, Object], log: ExtendedLog) extends MailSenderEndpoint with IdempotentManagedResource {

  /**
   * The Javamail mailerSession for sending messages.
   */
  private var mailerSession: Session = _

  /**
   * The transport that actually sends the messages.
   */
  private var mailerTransport: Transport = _

  /**
   * The username, if any.
   */
  private var username: String = _

  /**
   * The password, if any.
   */
  private var password: String = _

  override def onStartup(): Unit = {
    log.info("Mail sending service starting up")

    val props = System.getProperties()

    // Setup mail server
    if (smtpConfig != null) {
      val smtpHost = smtpConfig.get(MailSenderService.PROPERTY_NAME_HOST).asInstanceOf[String]
      val smtpPort =
        smtpConfig.getOrDefault(MailSenderService.PROPERTY_NAME_PORT, MailSenderService.PROPERTY_VALUE_DEFAULT_PORT).asInstanceOf[String]
      val useTls = smtpConfig.getOrDefault(MailSenderService.PROPERTY_NAME_USE_TLS, java.lang.Boolean.FALSE).asInstanceOf[Boolean]
      username = smtpConfig.get(MailSenderService.PROPERTY_NAME_USERNAME).asInstanceOf[String]
      password = smtpConfig.get(MailSenderService.PROPERTY_NAME_PASSWORD).asInstanceOf[String]

      props.put(JavaxMailMailSenderService.PROPERTY_MAIL_SMTP_HOST, smtpHost)
      props.put(JavaxMailMailSenderService.PROPERTY_MAIL_SMTP_PORT, smtpPort)

      props.put("mail.smtp.socketFactory.port", smtpPort)
      props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory")

      if (username != null && password != null) {
        props.put("mail.smtp.auth", "true")
      } else if (username != null) {
        log.warn("SMTP username givem, but no password")
      } else if (username != null) {
        log.warn("SMTP password givem, but no username")
      }

      if (useTls) {
        props.put("mail.smtp.ssl.enable", "false")
        props.put("mail.smtp.starttls.enable", "true")
      }

      mailerSession = Session.getDefaultInstance(props, null)

      mailerTransport = mailerSession.getTransport(JavaxMailMailSenderService.MAIL_TRANSPORT_SMTP)

      log.info(s"Mail service configured. SMTP host ${smtpHost}:${smtpPort}")
    } else {
      log.warn("Mail service not configured. No smtp host given.")
    }
  }

  override def onShutdown(): Unit = {
    mailerTransport.close
  }

  override def sendMailMessage(message: MailMessage): Unit = {
    if (mailerSession == null) {
      throw new SimpleSmartSpacesException("Mail service not configured")
    }

    // Define message
    val msg = new MimeMessage(mailerSession)
    try {
      msg.setFrom(new InternetAddress(message.getFromAddress()))

      message.getToAddresses().asScala.foreach(a => msg.addRecipient(Message.RecipientType.TO, new InternetAddress(a)))

      message.getCcAddresses().asScala.foreach(a =>
        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(a)))

      message.getBccAddresses().asScala.foreach(a =>
        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(a)))

      msg.setSubject(message.getSubject())
      msg.setContent(message.getBody(), message.getMimeType())

      msg.saveChanges

      // Send message
      ensureTransportConnected
      mailerTransport.sendMessage(msg, msg.getAllRecipients())

      log.info("Sent mail successfully")
    } catch {
      case e: Throwable =>
        throw new SmartSpacesException("Could not send mail", e)
    }
  }

  /**
   * Ensure the transport is connected.
   */
  private def ensureTransportConnected(): Unit = {
    while (!mailerTransport.isConnected()) {
      if (username != null && password != null) {
        mailerTransport.connect(username, password)
      } else {
        mailerTransport.connect
      }

      SmartSpacesUtilities.delay(100)
    }
  }
}
