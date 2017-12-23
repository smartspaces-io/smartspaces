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

import io.smartspaces.SmartSpacesException

/**
 * A state machine.
 *
 * <p>
 * This class encapsulates the state and all its transitions. It will transition
 * an object which is moving through the state machine through its series of
 * states. It is an immutable object.
 *
 * @param [S]
 *          the type of the state indicator class
 * @param [T]
 *          the type of the object that specifies the transition
 * @param [SO]
 *          the class of the state object
 *
 * @author Keith M. Hughes
 */
trait StateMachine[S, T, SO <: StateMachineObject[S]] {

  /**
   * Set the initial state for the state object.
   *
   * @param stateObject
   *          the state object being transitioned
   * @param initialState
   *          the initial state
   *
   * @throws SmartSpacesException
   */
  def initialState(stateObject: SO , initialState: S): Unit

  /**
   * Transition the state object from its current state to the next transition.
   *
   * @param stateObject
   *          the state object being transitioned
   * @param nextStateTransition
   *          the state transition
   *
   * @throws SmartSpacesException
   */
  def transition(stateObject: SO ,  nextStateTransition: T): Unit
  
  /**
   * Get the set of transitions leading out of a given state.
   * 
   * @param state
   *       the state to get the transitions for
   * 
   * @return the list of transitions
   */
  def getTransitions(state: S): List[T]
}


/**
 * An object which represents a state machine state.
 *
 * @param <S>
 *          the type of the state indicator
 *
 * @author Keith M. Hughes
 */
trait StateMachineObject[S] {

  /**
   * Get the current state machine state of the object.
   *
   * @return the current state machine state of the object
   */
  def getState():  Option[S]

  /**
   * Set the current state machine state of the object.
   *
   * <p>
   * This should only be called by the {@link StateMachine}.
   *
   * @param state
   *          the new state machine state of the object
   */
  def setState( state: S): Unit
}

/**
 * A base object for storing state machine state.
 *
 * @param <S>
 *          type of the state indicator
 *
 * @author Keith M. Hughes
 */
class BaseStateMachineObject[S] extends StateMachineObject[S] {

  /**
   * The current state of the object.
   */
  private var state: Option[S] = None

  override def getState(): Option[S] = {
    return state
  }

  override def setState( state: S): Unit = {
    this.state = Option(state)
  }
}

/**
 * An action to be performed during state machine transitions.
 *
 * @param <S>
 *          the type of the state indicator
 * @param <T>
 *          the type of the transition indicator
 * @param <SO>
 *          the state machine object
 *
 * @author Keith M. Hughes
 */
trait StateMachineAction[S, T, SO <: StateMachineObject[S]] {

  /**
   * Perform the action.
   *
   * @param machineObject
   *          the state machine state object
   */
  def performAction(machineObject: SO)
}

