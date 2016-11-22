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

package io.smartspaces.util.statemachine

import io.smartspaces.SimpleSmartSpacesException

/**
 * A builder for simple, deterministic equality state machines.
 *
 * @author Keith M. Hughes
 */
class EqualityTriggerStateMachineBuilder[S, T, SO <: StateMachineObject[S, T]] {

  /**
   * The machine being built.
   */
  private val machine = new EqualityTriggerStateMachine[S, T, SO]()

  /**
   * The current state being built.
   */
  private var currentState: Option[S] = None

  /**
   * Build the state machine.
   *
   * @return the state machine
   */
  def build(): EqualityTriggerStateMachine[S, T, SO] = {
    machine.validate()

    return machine
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
  def addState(state: S): EqualityTriggerStateMachineBuilder[S, T, SO] = {
    currentState = Option(state)
    
    machine.addInternalState(state)

    return this
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
  def onEntry(enterAction: StateMachineAction[S, T, SO]): EqualityTriggerStateMachineBuilder[S, T, SO] = {
    if (currentState.isDefined) {
      machine.getTransitionInternalState(currentState.get).enterAction = enterAction
      return this
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder")
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
  def onExit(exitAction: StateMachineAction[S, T, SO]): EqualityTriggerStateMachineBuilder[S, T, SO] = {
    if (currentState.isDefined) {
      machine.getTransitionInternalState(currentState.get).exitAction = exitAction

      return this
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder")
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
  def transition(transition: T, newState: S): EqualityTriggerStateMachineBuilder[S, T, SO] = {
    if (currentState.isDefined) {
      machine.getTransitionInternalState(currentState.get).addTransition(transition, newState)

      return this
    } else {
      throw new SimpleSmartSpacesException("no current state set for the builder")
    }
  }
}
