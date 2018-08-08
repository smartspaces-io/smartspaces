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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.mail.common.MailMessage;
import io.smartspaces.service.mail.sender.MailSenderService;

import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A {@link MailMessage} which uses Javax mail.
 *
 * @author Keith M. Hughes
 */
public class JavaxMailMailSenderService extends BaseSupportedService implements MailSenderService {

  /**
   * Configuration property for SMTP host smart spaces should use.
   */
  public static final String MAIL_TRANSPORT_SMTP = "smtp";

  /**
   * The javax.mail property for setting the host for the remote SMTP server.
   */
  public static final String PROPERTY_MAIL_SMTP_HOST = "mail.smtp.host";

  /**
   * The javax.mail property for setting the port for the remote SMTP server.
   */
  public static final String PROPERTY_MAIL_SMTP_PORT = "mail.smtp.port";

  /**
   * The Javamail mailerSession for sending messages.
   */
  private Session mailerSession;

  private String username;

  private String password;

  @Override
  public String getName() {
    return MailSenderService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    getSpaceEnvironment().getLog().info("Mail sending service starting up");

    Properties props = System.getProperties();

    Map<String, Object> smtpParams = getSpaceEnvironment().getSystemConfiguration()
        .getPropertyJson(CONFIGURATION_NAME_MAIL_SMTP);
    // Setup mail server
    if (smtpParams != null) {
      String smtpHost = (String) smtpParams.get(PROPERTY_NAME_HOST);
      String smtpPort =
          (String) smtpParams.getOrDefault(PROPERTY_NAME_PORT, PROPERTY_VALUE_DEFAULT_PORT);
      Boolean useTls = (Boolean) smtpParams.get(PROPERTY_NAME_USE_TLS);
      username = (String) smtpParams.get(PROPERTY_NAME_USERNAME);
      password = (String) smtpParams.get(PROPERTY_NAME_PASSWORD);

      props.put(PROPERTY_MAIL_SMTP_HOST, smtpHost);
      props.put(PROPERTY_MAIL_SMTP_PORT, smtpPort);

      props.put("mail.smtp.socketFactory.port", smtpPort);
      props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory");

      if (username != null && password != null) {
        props.put("mail.smtp.auth", "true");
      } else if (username != null) {
        getSpaceEnvironment().getLog().warn("SMTP username givem, but no password");
      } else if (username != null) {
        getSpaceEnvironment().getLog().warn("SMTP password givem, but no username");
      }

      if (useTls != null && useTls) {
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.starttls.enable", "true");
      }

      mailerSession = Session.getDefaultInstance(props, null);

      getSpaceEnvironment().getLog().formatInfo("Mail service configured. SMTP host %s:%s",
          smtpHost, smtpPort);
    } else {
      getSpaceEnvironment().getLog().warn("Mail service not configured. No smtp host given.");
    }
  }

  @Override
  public void sendMailMessage(MailMessage message) {
    if (mailerSession == null) {
      throw new SimpleSmartSpacesException("Mail service not configured");
    }

    // Define message
    MimeMessage msg = new MimeMessage(mailerSession);
    try {
      msg.setFrom(new InternetAddress(message.getFromAddress()));

      for (String address : message.getToAdresses()) {
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
      }

      for (String address : message.getCcAdresses()) {
        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(address));
      }

      for (String address : message.getBccAdresses()) {
        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(address));
      }

      msg.setSubject(message.getSubject());
      msg.setText(message.getBody());

      // Send message
      Transport transport = mailerSession.getTransport(MAIL_TRANSPORT_SMTP);
      transport.connect(username, password);
      transport.sendMessage(msg, msg.getAllRecipients());
      transport.close();

      getSpaceEnvironment().getLog().info("Sent mail successfully");
    } catch (Throwable e) {
      throw new SmartSpacesException("Could not send mail", e);
    }
  }
}
