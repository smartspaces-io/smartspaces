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

package io.smartspaces.tasks;

/**
 * Reference to a Managed Task.
 *
 * <p>
 * These are tasks placed into an instance {@link ManagedTasks} and will either
 * be shut down automatically with the entire collection of managed tasks or can
 * be shut down by the user.
 *
 * <p>
 * The API for the Managed Task is very simple. It can be cancelled if it should
 * be stopped early, and it is possible to check its running state.
 *
 * <p>
 * There is no way to restart the task. It must be submitted again.
 *
 * @author Keith M. Hughes
 */
public interface ManagedTask {

  /**
   * Cancel the task whether or not it was running.
   */
  void cancel();

  /**
   * Has the task been cancelled?
   *
   * @return {@code true} if cancelled
   */
  boolean isCancelled();

  /**
   * Is the task done?
   *
   * @return {@code true} if done
   */
  boolean isDone();
}
