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

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;

/**
 * A simple implementation of a managed task.
 * 
 * @author Keith M. Hughes
 */
public class SimpleManagedTask implements ManagedTask {

  /**
   * The task to be run.
   */
  private final Runnable task;

  /**
   * The wrapper for the task.
   */
  private final WrappedTask wrappedTask;

  /**
   * The managed tasks this task is part of.
   */
  private final SimpleManagedTasks managedCommands;

  /**
   * The future associated with this task.
   */
  private Future<?> future;

  /**
   * The logger to use.
   */
  private final Log log;

  /**
   * Construct a managed task for the given wrapped task.
   *
   * @param task
   *          the task to run
   * @param managedCommands
   *          the managed tasks that the task is part of
   * @param repeating
   *          {@code true} if this task will be repeating
   * @param allowTerminate
   *          {@code true} if the repeating task should be allowed to
   *          terminate if an exception is thrown
   * @param log
   *          the log to use
   */
  SimpleManagedTask(Runnable task, SimpleManagedTasks managedCommands, boolean repeating,
      boolean allowTerminate, Log log) {
    this.task = task;
    this.wrappedTask = new WrappedTask(task, repeating, allowTerminate);
    this.managedCommands = managedCommands;
    this.log = log;
  }

  /**
   * Set the future for this task.
   *
   * @param future
   *          the future
   */
  void setFuture(Future<?> future) {
    this.future = future;
  }

  /**
   * Cancel the task whether or not it was running.
   */
  public void cancel() {
    synchronized (managedCommands) {
      future.cancel(true);
      managedCommands.removeManagedTask(this);
    }
  }

  /**
   * Has the task been cancelled?
   *
   * @return {@code true} if cancelled
   */
  public boolean isCancelled() {
    return future.isCancelled();
  }

  /**
   * Is the task done?
   *
   * @return {@code true} if done
   */
  public boolean isDone() {
    return future.isDone();
  }

  /**
   * Get the task for the task.
   *
   * @return the wrapped task
   */
  WrappedTask getWrappedTask() {
    return wrappedTask;
  }

  /**
   * A runnable that logs exceptions.
   *
   * @author Keith M. Hughes
   */
  class WrappedTask implements Runnable {

    /**
     * The actual runnable to be run.
     */
    private final Runnable delegate;

    /**
     * {@code true} if this task is a repeating task.
     */
    private final boolean repeating;

    /**
     * {@code true} if this task should terminate if an exception happens.
     */
    private final boolean allowTerminate;

    /**
     * Construct a new wrapped task.
     *
     * @param delegate
     *          the actual runnable to run
     * @param repeating
     *          {@code true} if this delegate will be repeating
     * @param allowTerminate
     *          {@code true} if the potentially periodic running of the delegate
     *          can stop the periodic running
     */
    WrappedTask(Runnable delegate, boolean repeating, boolean allowTerminate) {
      this.delegate = delegate;
      this.repeating = repeating;
      this.allowTerminate = allowTerminate;
    }

    @Override
    public void run() {
      try {
        delegate.run();
      } catch (Throwable e) {
        log.error("Exception caught during Managed Command", e);

        if (allowTerminate) {
          // This guarantees the future will stop immediately
          throw new RuntimeException();
        }
      } finally {
        if (!repeating) {
          managedCommands.removeManagedTask(SimpleManagedTask.this);
        }
      }
    }
  }
}
