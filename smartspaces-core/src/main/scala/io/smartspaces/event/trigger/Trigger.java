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

package io.smartspaces.event.trigger;

/**
 * A trigger which can be observed.
 *
 * @author Keith M. Hughes
 */
public interface Trigger {
 
  /**
   * Add a new listener to the trigger.
   *
   * @param listener
   *          the new listener to add
   */
  void addListener(TriggerListener listener);

  /**
   * Remove a listener from the trigger.
   *
   * <p>
   * Does nothing if the listener wasn't registered with the trigger.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(TriggerListener listener);

  /**
   * Get the current state of the trigger.
   *
   * @return the trigger state
   */
  TriggerState getState();
}
