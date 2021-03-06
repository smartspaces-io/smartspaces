/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.interaction.event.trigger;

/**
 * A {@link TriggerWithData} that is resettable.
 *
 * @param <D>
 *          the type of the data
 *
 * @author Keith M. Hughes
 */
trait ResettableTriggerWithData[D] extends TriggerWithData[D] {

  /**
   * Reset the trigger.
   *
   * <p>
   * If the state changes because of the reset, an event will be published to
   * the listeners.
   *
   * @param data
   *          data for the reset event
   */
  def reset(data: D): Unit
}
