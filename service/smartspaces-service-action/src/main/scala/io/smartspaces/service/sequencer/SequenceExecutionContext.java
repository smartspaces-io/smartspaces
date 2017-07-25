/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.service.sequencer;

import io.smartspaces.evaluation.StandardExecutionContext;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.system.SmartSpacesEnvironment;

/**
 * An environment for executing a sequence.
 * 
 * @author Keith M. Hughes
 */
public class SequenceExecutionContext extends StandardExecutionContext {

  /**
   * The sequencer running the sequence
   */
  private final Sequencer sequencer;

  /**
   * The sequence being run
   */
  private final Sequence sequence;

  /**
   * Construct a new context.
   * 
   * @param sequencer
   *          the sequencer running the sequence
   * @param sequence
   *          the sequence being run
   * @param managedScope
   *          the managed scope for the sequence
   * @param spaceEnvironment
   *          the space environment for the sequence environment
   * @param log
   *          the log for the context
   */
  public SequenceExecutionContext(Sequencer sequencer, Sequence sequence, ManagedScope managedScope,
      SmartSpacesEnvironment spaceEnvironment, ExtendedLog log) {
    super(managedScope, spaceEnvironment, log);
    this.sequencer = sequencer;
    this.sequence = sequence;
  }

  /**
   * Get the sequencer the sequence is running under.
   * 
   * @return the sequencer
   */
  public Sequencer getSequencer() {
    return sequencer;
  }

  /**
   * Get the complete sequence being run.
   * 
   * @return the sequence
   */
  public Sequence getSequence() {
    return sequence;
  }
}
