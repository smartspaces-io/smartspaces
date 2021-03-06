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

package io.smartspaces.service.alert.notifier.mail.internal;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.alert.AlertNotifier;
import io.smartspaces.service.alert.AlertService;
import io.smartspaces.service.mail.common.ComposableMailMessage;
import io.smartspaces.service.mail.common.SimpleMailMessage;
import io.smartspaces.service.mail.sender.MailSenderEndpoint;
import io.smartspaces.service.mail.sender.MailSenderService;

/**
 * A basic implementation of an {@link AlertService}.
 *
 * @author Keith M. Hughes
 */
public class BasicMailAlertNotifier extends BaseSupportedService implements AlertNotifier {

  /**
   * Configuration property prefix for all mail alert notifier properties.
   */
  public static final String CONFIGURATION_NAME_PREFIX_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL =
      "io.smartspaces.service.alert.notifier.mail.";

  /**
   * Configuration property for a space-separated list of email addresses for
   * alert notifications.
   */
  public static final String CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO =
      CONFIGURATION_NAME_PREFIX_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL + "to";

  /**
   * Configuration property for the from email address for alert notifications.
   */
  public static final String CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM =
      CONFIGURATION_NAME_PREFIX_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL + "from";

  /**
   * Configuration property for the subject for emails for alert notifications.
   */
  public static final String CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT =
      CONFIGURATION_NAME_PREFIX_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL + "subject";

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "alert.notifier.email";

  /**
   * Service for alerting.
   */
  private final AlertService alertService;

  /**
   * Service for sending mail.
   */
  private final MailSenderService mailSenderService;

  /**
   * The sender for email.
   */
  private MailSenderEndpoint mailSenderEndpoint = null;

  /**
   * Construct a new mail alert notifier.
   *
   * @param alertService
   *          the alert service to attach to
   * @param mailSenderService
   *          the mail sender service to use
   */
  public BasicMailAlertNotifier(AlertService alertService, MailSenderService mailSenderService) {
    this.alertService = alertService;
    this.mailSenderService = mailSenderService;
  }

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public void startup() {
    alertService.registerAlertNotifier(this);

    mailSenderEndpoint = mailSenderService.newMailSenderEndpoint(getSpaceEnvironment().getLog());
    mailSenderEndpoint.startup();
  }

  @Override
  public void shutdown() {
    alertService.unregisterAlertNotifier(this);
    mailSenderEndpoint.shutdown();
  }

  @Override
  public void notify(String alertType, String id, String message) {
    try {
      Configuration config = getSpaceEnvironment().getSystemConfiguration();

      ComposableMailMessage mail = new SimpleMailMessage();

      try {
        addToAddresses(alertType, mail, config);
        addFromAddress(alertType, mail, config);
        addSubject(alertType, mail, config);
      } catch (SimpleSmartSpacesException e) {
        getSpaceEnvironment().getLog().warn(e.getCompoundMessage());
        getSpaceEnvironment().getLog().formatWarn(
            "Email alert failed for alert type %s, id %s, message %s", alertType, id, message);
        return;
      }

      mail.setBody(message);

      mailSenderEndpoint.sendMailMessage(mail);
    } catch (Throwable e) {
      getSpaceEnvironment().getLog().formatError(e,
          "Email alert for alert type %s, id %s, message %s", alertType, id, message);
    }
  }

  /**
   * Add all TO addresses to the email.
   *
   * @param alertType
   *          the alert type
   * @param mail
   *          the mail message being generated
   * @param config
   *          the system configuration
   */
  private void addToAddresses(String alertType, ComposableMailMessage mail, Configuration config) {
    String tos = getConfigValue(CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO,
        alertType, config);

    for (String to : tos.trim().split("\\s+")) {
      mail.addToAddress(to.trim());
    }
  }

  /**
   * Add the From address to the email.
   *
   * @param alertType
   *          the alert type
   * @param mail
   *          the mail message being generated
   * @param config
   *          the system configuration
   */
  private void addFromAddress(String alertType, ComposableMailMessage mail, Configuration config) {
    mail.setFromAddress(getConfigValue(
        CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM, alertType, config));
  }

  /**
   * Add the subject to the email.
   *
   * @param alertType
   *          the alert type
   * @param mail
   *          the mail message being generated
   * @param config
   *          the system configuration
   */
  private void addSubject(String alertType, ComposableMailMessage mail, Configuration config) {
    mail.setSubject(getConfigValue(
        CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT, alertType, config));
  }

  /**
   * Get a config value based on the alert type. If not found, look for a
   * default value which will have the same name but without the alert type on
   * the end.
   *
   * <p>
   * The alert type is separated from the config parameter by a period.
   *
   * @param configParameter
   *          the configuration
   * @param alertType
   *          the alert type
   * @param config
   *          the system configuration
   *
   * @return the value of the configuration
   *
   * @throws SmartSpacesException
   *           no value was found
   */
  private String getConfigValue(String configParameter, String alertType, Configuration config)
      throws SmartSpacesException {
    String value = config.getPropertyString(configParameter + "." + alertType);
    if (value == null) {
      value = config.getPropertyString(configParameter);
    }

    if (value == null || value.trim().isEmpty()) {
      throw new SimpleSmartSpacesException(String.format(
          "No configuration parameter %s set for alert type %s", configParameter, alertType));
    }

    return value;
  }
}
