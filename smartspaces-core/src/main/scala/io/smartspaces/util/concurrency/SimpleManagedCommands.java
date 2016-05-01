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

package io.smartspaces.util.concurrency;

import io.smartspaces.util.events.EventDelay;
import io.smartspaces.util.events.EventFrequency;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Keith M. Hughes
 */
public class SimpleManagedCommands implements ManagedCommands {

  /**
   * All managed commands in collection.
   */
  private final Set<ManagedCommand> managedCommands = new HashSet<>();

  /**
   * The executor service for this collection.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The logger for the collection.
   */
  private final Log log;

  /**
   * Construct a managed command collection.
   *
   * @param executorService
   *          the executor service for the command
   * @param log
   *          the logger for the command
   */
  public SimpleManagedCommands(ScheduledExecutorService executorService, Log log) {
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public synchronized ManagedCommand submit(Runnable command) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.submit(managedCommand.getTask()));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public synchronized ManagedCommand schedule(Runnable command, long delay, TimeUnit unit) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.schedule(managedCommand.getTask(), delay, unit));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleAtFixedRate(Runnable command, EventFrequency commandFrequency) {
    return scheduleAtFixedRate(command, commandFrequency, true);
  }

  @Override
  public synchronized ManagedCommand scheduleAtFixedRate(Runnable command,
      EventFrequency commandFrequency, boolean allowTerminate) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleAtFixedRate(managedCommand.getTask(), 0,
        commandFrequency.getPeriod(), commandFrequency.getUnit()));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit) {
    return scheduleAtFixedRate(command, initialDelay, period, unit, true);
  }

  @Override
  public synchronized ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay,
      long period, TimeUnit unit, boolean allowTerminate) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleAtFixedRate(managedCommand.getTask(),
        initialDelay, period, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleWithFixedDelay(Runnable command, EventDelay commandDelay) {
    return scheduleWithFixedDelay(command, commandDelay, true);
  }

  @Override
  public synchronized ManagedCommand scheduleWithFixedDelay(Runnable command,
      EventDelay commandDelay, boolean allowTerminate) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleWithFixedDelay(managedCommand.getTask(), 0,
        commandDelay.getDelay(), commandDelay.getUnit()));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit) {
    return scheduleWithFixedDelay(command, initialDelay, delay, unit, true);
  }

  @Override
  public synchronized ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay,
      long delay, TimeUnit unit, boolean allowTerminate) {
    SimpleManagedCommand managedCommand =
        new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleWithFixedDelay(managedCommand.getTask(),
        initialDelay, delay, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Shut down all executing commands or commands which haven't started yet.
   */
  public synchronized void shutdownAll() {
    // Copied into an array list so that shutdowns of individual commands do not
    // cause a concurrent modification
    // exception in the set.
    for (ManagedCommand managedCommand : Lists.newArrayList(managedCommands)) {
      managedCommand.cancel();
    }
    managedCommands.clear();
  }

  /**
   * Remove a managed command from the collection.
   *
   * @param managedCommand
   *          the command to remove
   */
  synchronized void removeManagedCommand(ManagedCommand managedCommand) {
    managedCommands.remove(managedCommand);
  }
}
