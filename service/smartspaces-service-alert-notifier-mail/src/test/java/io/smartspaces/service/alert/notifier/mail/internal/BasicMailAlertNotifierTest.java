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

import static org.junit.Assert.assertEquals;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.alert.AlertNotifier;
import io.smartspaces.service.alert.AlertService;
import io.smartspaces.service.mail.common.MailMessage;
import io.smartspaces.service.mail.sender.MailSenderEndpoint;
import io.smartspaces.service.mail.sender.MailSenderService;
import io.smartspaces.system.SmartSpacesEnvironment;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link BasicMailAlertNotifier}.
 *
 * @author Keith M. Hughes
 */
public class BasicMailAlertNotifierTest {

  private BasicMailAlertNotifier notifier;

  private SmartSpacesEnvironment spaceEnvironment;
  private ExtendedLog log;
  private MailSenderService mailSenderService;
  private MailSenderEndpoint mailSenderEndpoint;
  private AlertService alertService;
  private Configuration configuration;

  @Before
  public void setup() {
    configuration = Mockito.mock(Configuration.class);
    log = Mockito.mock(ExtendedLog.class);
    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);
    Mockito.when(spaceEnvironment.getSystemConfiguration()).thenReturn(configuration);
    Mockito.when(spaceEnvironment.getLog()).thenReturn(log);

    mailSenderService = Mockito.mock(MailSenderService.class);
    mailSenderEndpoint = Mockito.mock(MailSenderEndpoint.class);
    alertService = Mockito.mock(AlertService.class);
    
    Mockito.when(mailSenderService.newMailSenderEndpoint(log)).thenReturn(mailSenderEndpoint);

    notifier = new BasicMailAlertNotifier(alertService, mailSenderService);
    notifier.setSpaceEnvironment(spaceEnvironment);
  }

  /**
   * Make sure it gets properly registered with the alert service.
   */
  @Test
  public void testAlertServiceRegistration() {
    notifier.startup();
    Mockito.verify(alertService, Mockito.times(1)).registerAlertNotifier(notifier);
    Mockito.verify(alertService, Mockito.never()).unregisterAlertNotifier(
        Mockito.any(AlertNotifier.class));
  }

  /**
   * Make sure it gets properly unregistered with the alert service.
   */
  @Test
  public void testAlertServiceUnRegistration() {
    notifier.startup();
    notifier.shutdown();
    Mockito.verify(alertService, Mockito.times(1)).registerAlertNotifier(notifier);
    Mockito.verify(alertService, Mockito.times(1)).unregisterAlertNotifier(notifier);
  }

  /**
   * Email with default values.
   */
  @Test
  public void testSendingMailDefaults() {
    String fromAddress = "snidley.whiplash@gmail.com";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM))
        .thenReturn(fromAddress);
    String toAddress = "dudley.doright@gmail.com";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO))
        .thenReturn(toAddress);
    String subject = "But Nell...";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT))
        .thenReturn(subject);

    String alertType = "kiwi";
    String id = "orange";
    String message = "mango";

    ArgumentCaptor<MailMessage> argument = ArgumentCaptor.forClass(MailMessage.class);

    notifier.startup();
    notifier.notify(alertType, id, message);

    Mockito.verify(mailSenderEndpoint).sendMailMessage(argument.capture());

    assertEquals(fromAddress, argument.getValue().getFromAddress());
    assertEquals(toAddress, argument.getValue().getToAddresses().get(0));
    assertEquals(subject, argument.getValue().getSubject());
    assertEquals(message, argument.getValue().getBody());
  }

  /**
   * Email with values specific to alert types.
   *
   * <p>
   * Also have multiple to addresses
   */
  @Test
  public void testSendingMailByAlertType() {
    String alertType = "kiwi";
    String id = "orange";
    String message = "mango";

    String fromAddress = "elmer.fudd@gmail.com";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM
                    + "." + alertType)).thenReturn(fromAddress);
    String toAddress1 = "bugs.bunny@gmail.com";
    String toAddress2 = "road.runner@gmail.com";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO
                    + "." + alertType)).thenReturn("  " + toAddress1 + "     " + toAddress2);
    String subject = "Wascally wabbit...";
    Mockito
        .when(
            configuration
                .getPropertyString(BasicMailAlertNotifier.CONFIGURATION_NAME_SMARTSPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT
                    + "." + alertType)).thenReturn(subject);

    ArgumentCaptor<MailMessage> argument = ArgumentCaptor.forClass(MailMessage.class);

    notifier.startup();
    notifier.notify(alertType, id, message);

    Mockito.verify(mailSenderEndpoint).sendMailMessage(argument.capture());

    assertEquals(fromAddress, argument.getValue().getFromAddress());
    assertEquals(Lists.newArrayList(toAddress1, toAddress2), argument.getValue().getToAddresses());
    assertEquals(subject, argument.getValue().getSubject());
    assertEquals(message, argument.getValue().getBody());
  }
}
