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

import io.smartspaces.time.TimeDelay;
import io.smartspaces.time.TimeFrequency;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple implementation of a collection of managed tasks.
 * 
 * @author Keith M. Hughes
 */
public class SimpleManagedTasks implements InternalManagedTasks {

  /**
   * All managed tasks in collection.
   */
  private final Set<ManagedTask> managedTasks = new HashSet<>();

  /**
   * The executor service for this collection.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The logger for the collection.
   */
  private final Log log;

  /**
   * Construct a managed task collection.
   *
   * @param executorService
   *          the executor service for the tasks
   * @param log
   *          the logger for the task
   */
  public SimpleManagedTasks(ScheduledExecutorService executorService, Log log) {
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public synchronized ManagedTask submit(Runnable task) {
    SimpleManagedTask managedTask = new SimpleManagedTask(task, this, false, false, log);
    managedTask.setFuture(executorService.submit(managedTask.getWrappedTask()));
    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public synchronized ManagedTask schedule(Runnable task, long delay, TimeUnit unit) {
    SimpleManagedTask managedTask = new SimpleManagedTask(task, this, false, false, log);
    managedTask.setFuture(executorService.schedule(managedTask.getWrappedTask(), delay, unit));
    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public ManagedTask scheduleAtFixedRate(Runnable task, TimeFrequency taskFrequency) {
    return scheduleAtFixedRate(task, taskFrequency, true);
  }

  @Override
  public synchronized ManagedTask scheduleAtFixedRate(Runnable task,
      TimeFrequency taskFrequency, boolean allowTerminate) {
    SimpleManagedTask managedTask =
        new SimpleManagedTask(task, this, true, allowTerminate, log);
    managedTask.setFuture(executorService.scheduleAtFixedRate(managedTask.getWrappedTask(), 0,
        taskFrequency.getPeriod(), taskFrequency.getUnit()));

    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public ManagedTask scheduleAtFixedRate(Runnable task, long initialDelay, long period,
      TimeUnit unit) {
    return scheduleAtFixedRate(task, initialDelay, period, unit, true);
  }

  @Override
  public synchronized ManagedTask scheduleAtFixedRate(Runnable task, long initialDelay,
      long period, TimeUnit unit, boolean allowTerminate) {
    SimpleManagedTask managedTask =
        new SimpleManagedTask(task, this, true, allowTerminate, log);
    managedTask.setFuture(
        executorService.scheduleAtFixedRate(managedTask.getWrappedTask(), initialDelay, period, unit));

    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public ManagedTask scheduleWithFixedDelay(Runnable task, TimeDelay taskDelay) {
    return scheduleWithFixedDelay(task, taskDelay, true);
  }

  @Override
  public synchronized ManagedTask scheduleWithFixedDelay(Runnable task, TimeDelay taskDelay,
      boolean allowTerminate) {
    SimpleManagedTask managedTask =
        new SimpleManagedTask(task, this, true, allowTerminate, log);
    managedTask.setFuture(executorService.scheduleWithFixedDelay(managedTask.getWrappedTask(), 0,
        taskDelay.getDelay(), taskDelay.getUnit()));

    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public ManagedTask scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
      TimeUnit unit) {
    return scheduleWithFixedDelay(task, initialDelay, delay, unit, true);
  }

  @Override
  public synchronized ManagedTask scheduleWithFixedDelay(Runnable task, long initialDelay,
      long delay, TimeUnit unit, boolean allowTerminate) {
    SimpleManagedTask managedTask =
        new SimpleManagedTask(task, this, true, allowTerminate, log);
    managedTask.setFuture(executorService.scheduleWithFixedDelay(managedTask.getWrappedTask(),
        initialDelay, delay, unit));

    managedTasks.add(managedTask);

    return managedTask;
  }

  @Override
  public synchronized void shutdownAll() {
    // Copied into an array list so that shutdowns of individual tasks do not
    // cause a concurrent modification
    // exception in the set.
    for (ManagedTask managedTask : Lists.newArrayList(managedTasks)) {
      managedTask.cancel();
    }
    managedTasks.clear();
  }

  /**
   * Remove a managed task from the collection.
   *
   * @param managedTask
   *          the task to remove
   */
  synchronized void removeManagedTask(ManagedTask managedTask) {
    managedTasks.remove(managedTask);
  }

  /**
   * Does the collection contain the given task?
   *
   * @param managedTask
   *          the task check
   *          
   * @return {@code true} if the collection contains the task
   */
  synchronized boolean containsTask(ManagedTask managedTask) {
    return managedTasks.contains(managedTask);
  }
}
