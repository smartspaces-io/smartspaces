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

package io.smartspaces.service.mail.receiver;

import io.smartspaces.resource.managed.ManagedResource;

/**
 * A receiver for email.
 *
 * @author Keith M. Hughes
 */

public interface MailReceiver extends ManagedResource {

  /**
   * Add a listener to the service.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(MailReceiverListener listener);

  /**
   * Remove a listener from the service.
   *
   * <p>
   * This method does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(MailReceiverListener listener);
}
