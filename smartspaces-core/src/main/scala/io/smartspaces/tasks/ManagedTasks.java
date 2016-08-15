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

package io.smartspaces.tasks;

import io.smartspaces.time.TimeDelay;
import io.smartspaces.time.TimeFrequency;

import java.util.concurrent.TimeUnit;

/**
 * A collection of scheduled tasks which can be shut down as a whole.
 *
 * <p>
 * Instances of this class are thread-safe.
 *
 * @author Keith M. Hughes
 */
public interface ManagedTasks {

  /**
   * Submit a task to start immediately.
   *
   * @param task
   *          the task to run
   *
   * @return the managed task
   */
  ManagedTask submit(Runnable task);

  /**
   * Schedule a new task with a delay.
   *
   * @param task
   *          the task to run
   * @param delay
   *          how soon in the future to run it
   * @param unit
   *          units on how soon to start
   *
   * @return the managed task
   */
  ManagedTask schedule(Runnable task, long delay, TimeUnit unit);

  /**
   * Executes a periodic task executes at the given frequency period between
   * the commencement of one execution and the commencement of the next. This
   * means it is possible for more than one execution to be happening at the
   * same time if any of the task executions run longer than the period.
   *
   * <p>
   * The task will start immediately.
   *
   * <p>
   * If the task throws an exception, it will not be run again. The exception
   * will be logged.
   *
   * @param task
   *          the task to run
   * @param taskFrequency
   *          the frequency at which the tasks should happen
   *
   * @return the managed task
   */
  ManagedTask scheduleAtFixedRate(Runnable task, TimeFrequency taskFrequency);

  /**
   * Executes a periodic task executes at the given frequency period between
   * the commencement of one execution and the commencement of the next. This
   * means it is possible for more than one execution to be happening at the
   * same time if any of the task executions run longer than the period.
   *
   * <p>
   * The task will start immediately.
   *
   * <p>
   * If the task throws an exception, the exception will be logged.
   *
   * @param task
   *          the task to run
   * @param taskFrequency
   *          the frequency at which the tasks should happen
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed task
   */
  ManagedTask scheduleAtFixedRate(Runnable task, TimeFrequency taskFrequency,
      boolean allowTerminate);

  /**
   * Executes a periodic task that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via
   * cancellation or termination of the executor.
   *
   * <p>
   * If the task throws an exception, it will not be repeated. The exception
   * will be logged.
   *
   * @param task
   *          the task to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param period
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   *
   * @return the managed task
   */
  ManagedTask
      scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

  /**
   * Executes a periodic task that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, the exception is ignored. The task will only terminate via
   * cancellation or termination of the executor.
   *
   * <p>
   * If the task throws an exception, the exception will be logged.
   *
   * @param task
   *          the task to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param period
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed task
   */
  ManagedTask scheduleAtFixedRate(Runnable task, long initialDelay, long period,
      TimeUnit unit, boolean allowTerminate);

  /**
   * Executes a periodic task that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task
   * will only terminate via cancellation or termination of the executor.
   *
   * <p>
   * If the task throws an exception, it will not be repeated. The exception
   * will be logged.
   *
   * @param task
   *          the task to run
   * @param taskDelay
   *          the delay between invocations
   *
   * @return the managed task
   */
  ManagedTask scheduleWithFixedDelay(Runnable task, TimeDelay taskDelay);

  /**
   * Executes a periodic task where the time between the termination of one
   * execution and the commencement of the next is specified by the period of
   * the task frequency.
   *
   * <p>
   * The task will start running immediately.
   *
   * <p>
   * If the task throws an exception, the exception will be logged.
   *
   * @param task
   *          the task to run
   * @param taskDelay
   *          the delay between invocations
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed task
   */
  ManagedTask scheduleWithFixedDelay(Runnable task, TimeDelay taskDelay,
      boolean allowTerminate);

  /**
   * Executes a periodic task that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task
   * will only terminate via cancellation or termination of the executor.
   *
   * <p>
   * If the task throws an exception, it will not be repeated. The exception
   * will be logged.
   *
   * @param task
   *          the task to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param delay
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   *
   * @return the managed task
   */
  ManagedTask scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
      TimeUnit unit);

  /**
   * Executes a periodic task that starts after the given initial delay, and
   * subsequently with the given delay between the termination of one execution
   * and the commencement of the next. If any execution of the task encounters
   * an exception, subsequent executions are suppressed. Otherwise, the task
   * will only terminate via cancellation or termination of the executor.
   *
   * @param task
   *          the task to run
   * @param initialDelay
   *          how soon in the future to run it the first time
   * @param delay
   *          how long to delay between subsequent executions
   * @param unit
   *          time units for both delays
   * @param allowTerminate
   *          {@code true} if the task should be allowed to terminate if it
   *          throws an exception
   *
   * @return the managed task
   */
  ManagedTask scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
      TimeUnit unit, boolean allowTerminate);
}
