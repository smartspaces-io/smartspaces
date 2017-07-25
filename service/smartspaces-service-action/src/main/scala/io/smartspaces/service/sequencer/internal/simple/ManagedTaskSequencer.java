/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.service.sequencer.internal.simple;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.service.sequencer.Sequence;
import io.smartspaces.service.sequencer.SequenceElements;
import io.smartspaces.service.sequencer.Sequencer;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.StandaloneSmartSpacesEnvironment;
import io.smartspaces.tasks.ManagedTask;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;
import io.smartspaces.util.SmartSpacesUtilities;

/**
 * An implementation of the Sequencer interface that uses {@link ManagedTasks}.
 *
 * @author Keith M. Hughes
 */
public class ManagedTaskSequencer implements Sequencer {

  public static void main(String[] args) {
    StandaloneSmartSpacesEnvironment spaceEnvironment =
        StandaloneSmartSpacesEnvironment.newStandaloneSmartSpacesEnvironment();

    ManagedTaskSequencer sequencer =
        new ManagedTaskSequencer(spaceEnvironment, spaceEnvironment.getLog());

    Sequence sequence = sequencer.newSequence(spaceEnvironment.getContainerManagedScope(), spaceEnvironment.getLog());
    sequence.add(SequenceElements.runnable(new Runnable() {

      @Override
      public void run() {
        System.out.println("Hello!");
      }

    })).add(SequenceElements.repeat(10, SequenceElements.runnable(new Runnable() {

      @Override
      public void run() {
        System.out.println("Repeat");
      }

    })));

    sequence.startup();
    SmartSpacesUtilities.delay(10000);

    sequencer.shutdown();
    spaceEnvironment.shutdown();
  }

  /**
   * THe space environment for the sequencer.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * The instance of {@link ManagedTasks} to use for scheduling.
   */
  private final StandardManagedTasks managedTasks;

  /**
   * The logger for the sequencer.
   */
  private final ExtendedLog log;

  /**
   * Create a ManagedCommandScheduler with the given ManagedCommands instance.
   *
   * @param spaceEnvironment
   *          the space environment to execute under
   * @param log
   *          the logger to use
   */
  public ManagedTaskSequencer(SmartSpacesEnvironment spaceEnvironment, ExtendedLog log) {
    this.spaceEnvironment = spaceEnvironment;
    this.managedTasks = new StandardManagedTasks(spaceEnvironment.getExecutorService(), log);
    this.log = log;
  }

  @Override
  public void startup() {
    // Nothing to do.
  }

  @Override
  public void shutdown() {
    managedTasks.shutdownAll();
  }

  @Override
  public Sequence newSequence(ManagedScope managedScope, ExtendedLog log) {
    return new ManagedTaskSequence(this, managedScope, log);
  }

  @Override
  public ExtendedLog getLog() {
    return log;
  }

  /**
   * Start executing the sequence.
   *
   * @param sequence
   *          the sequence to start
   *
   * @return the managed command running the sequence
   */
  ManagedTask startSequence(final ManagedTaskSequence sequence) {
    return managedTasks.submit(new Runnable() {
      @Override
      public void run() {
        sequence.runSequence(ManagedTaskSequencer.this);
      }
    });
  }

  /**
   * Get the space environment.
   * 
   * @return the space environment
   */
  SmartSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }
}
