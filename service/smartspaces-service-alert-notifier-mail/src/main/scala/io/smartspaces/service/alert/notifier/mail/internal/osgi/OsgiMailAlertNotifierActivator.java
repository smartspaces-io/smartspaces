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

package io.smartspaces.service.alert.notifier.mail.internal.osgi;

import io.smartspaces.service.alert.AlertService;
import io.smartspaces.service.alert.notifier.mail.internal.BasicMailAlertNotifier;
import io.smartspaces.service.mail.sender.MailSenderService;
import io.smartspaces.system.osgi.OsgiServiceTrackerCollection.MyServiceTracker;
import io.smartspaces.system.osgi.SmartSpacesOsgiBundleActivator;

/**
 * An OSGi activator for a mail alert notifier.
 *
 * @author Keith M. Hughes
 */
public class OsgiMailAlertNotifierActivator extends SmartSpacesOsgiBundleActivator {

  /**
   * OSGi service tracker for the mail sender service.
   */
  private MyServiceTracker<MailSenderService> mailSenderServiceTracker;

  /**
   * OSGi service tracker for the alert service.
   */
  private MyServiceTracker<AlertService> alertServiceTracker;

  @Override
  public void onStart() {
    mailSenderServiceTracker = newMyServiceTracker(MailSenderService.class.getName());

    alertServiceTracker = newMyServiceTracker(AlertService.class.getName());
  }

  @Override
  protected void allRequiredServicesAvailable() {
    MailSenderService mailSenderService = mailSenderServiceTracker.getMyService();
    AlertService alertService = alertServiceTracker.getMyService();

    BasicMailAlertNotifier mailAlertNotifier =
        new BasicMailAlertNotifier(alertService, mailSenderService);
    registerNewSmartSpacesService(mailAlertNotifier);
  }
}
