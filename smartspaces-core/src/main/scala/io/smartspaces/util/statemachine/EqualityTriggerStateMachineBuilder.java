/*
 * Copyright (C) 2015 Keith M. Hughes.
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

package io.smartspaces.util.statemachine;

import io.smartspaces.SimpleSmartSpacesException;

/**
 * A builder for simple, deterministic equality state machines.
 *
 * @author Keith M. Hughes
 */
public class EqualityTriggerStateMachineBuilder<S, T, SO extends StateMachineObject<S, T>> {

  /**
   * Create a new builder.
   *
   * @return the new builder
   */
  public static <S, T, SO extends StateMachineObject<S, T>>
      EqualityTriggerStateMachineBuilder<S, T, SO> newBuilder() {
    return new EqualityTriggerStateMachineBuilder<S, T, SO>();
  }

  /**
   * The current state being built.
   */
  private EqualityTriggerStateMachine<S, T, SO>.InternalStateMachineState currentState;

  /**
   * The machine being built.
   */
  private EqualityTriggerStateMachine<S, T, SO> machine =
      new EqualityTriggerStateMachine<S, T, SO>();

  /**
   * Build the state machine.
   *
   * @return the state machine
   */
  public EqualityTriggerStateMachine<S, T, SO> build() {
    machine.validate();

    return machine;
  }

  /**
   * Add in a new state to the state machine.
   *
   * @param state
   *          the new state to add
   *
   * @return this builder
   *
   * @throws SimpleSmartSpacesException
   *           the new state was already part of the state machine
   */
  public EqualityTriggerStateMachineBuilder<S, T, SO> addState(S state) {
    currentState = machine.addInternalState(state);

    return this;
  }

  /**
   * Set the enter action for the current state.
   *
   * @param enterAction
   *          the enter action
   *
   * @return this builder
   *
   * @throws SimpleSmartSpacesException
   *           there is no state currently being built
   */
  public EqualityTriggerStateMachineBuilder<S, T, SO> onEntry(
      StateMachineAction<S, T, SO> enterAction) {
    if (currentState != null) {
      currentState.setEnterAction(enterAction);
      return this;
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder");
    }
  }

  /**
   * Set the exit action for the current state.
   *
   * @param exitAction
   *          the exit action
   *
   * @return this builder
   *
   * @throws SimpleSmartSpacesException
   *           there is no state currently being built
   */
  public EqualityTriggerStateMachineBuilder<S, T, SO>
      onExit(StateMachineAction<S, T, SO> exitAction) {
    if (currentState != null) {
      currentState.setExitAction(exitAction);
      return this;
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder");
    }
  }

  /**
   * Add a new transition to the state currently being built
   *
   * @param transition
   *          the transition designator
   * @param newState
   *          the new state to go to
   *
   * @return this builder
   *
   * @throws SimpleSmartSpacesException
   *           there is no state currently being built
   */
  public EqualityTriggerStateMachineBuilder<S, T, SO> transition(T transition, S newState) {
    if (currentState != null) {
      currentState.addTransition(transition, newState);

      return this;
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder");
    }
  }
}
