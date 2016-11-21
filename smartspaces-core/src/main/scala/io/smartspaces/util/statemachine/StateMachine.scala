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
trait StateMachine[S, T, SO <: StateMachineObject[S, T]] {

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
}
