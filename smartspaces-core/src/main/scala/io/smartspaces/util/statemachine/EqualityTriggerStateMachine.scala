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

import java.util.HashMap

import io.smartspaces.SimpleSmartSpacesException
import io.smartspaces.SmartSpacesException

import scala.collection.JavaConverters._

/**
 * A state machine which uses equality on the trigger class to transition to the
 * next state.
 *
 * @tparam S
 *          The state indicator type
 * @tparam T
 *          The transition indicator type
 * @tparam SO
 *          The state object
 *
 * @author Keith M. Hughes
 */
class EqualityTriggerStateMachine[S, T, SO <: StateMachineObject[S]] extends StateMachine[S, T, SO] {

  /**
   * A map from states to the internal state object representation.
   */
  private val internalStates = new HashMap[S, InternalStateMachineState]()

  override def initialState(stateObject: SO, initialState: S): Unit = {
    val currentInternalState = internalStates.get(initialState)
    if (currentInternalState != null) {
      stateObject.setState(initialState)
      currentInternalState.doEnterState(stateObject)
    } else {
      throw new SmartSpacesException(s"Cannot set initial state ${initialState}")
    }
  }

  override def transition(stateObject: SO, nextStateTransition: T): Unit = {
    val currentState = stateObject.getState.get

    val currentInternalState = internalStates.get(currentState)

    val nextInternalState =
      currentInternalState.getTransitionState(nextStateTransition)

    if (nextInternalState == null) {
      throw new SimpleSmartSpacesException(
        s"There is no transition named ${nextStateTransition} from state ${currentState}")
    }

    currentInternalState.doExitState(stateObject)

    stateObject.setState(nextInternalState.state.get)
    nextInternalState.doEnterState(stateObject)
  }

  override def getTransitions(state: S): List[T] = {
    val currentInternalState = internalStates.get(state)
    if (currentInternalState != null) {
      
      currentInternalState.getTransitions()
    } else {
      throw new SmartSpacesException(s"Cannot find state ${state} to get transitions")
    }
  }

  /**
   * Add in a new internal state to the machine.
   *
   * @param state
   *          the new state
   *
   * @return the internal state
   */
  private[statemachine] def addInternalState(state: S): InternalStateMachineState = {
    var internalState = internalStates.get(state)
    if (internalState != null) {
      if (internalState.state.isEmpty) {
        internalState.state = Option(state)
      } else {
        throw new SimpleSmartSpacesException(s"State machine already contained state ${state}")
      }
    } else {
      internalState = new InternalStateMachineState(Option(state))
      internalStates.put(state, internalState)
    }

    internalState
  }

  /**
   * Get an internal state for a transition.
   *
   * @param state
   *          the state the transition should go to
   *
   * @return the internal state
   */
  private[statemachine] def getTransitionInternalState(state: S): InternalStateMachineState = {
    var internalState = internalStates.get(state)
    if (internalState == null) {
      // Don't want the state associated yet so we can fail validation if the
      // state was never
      // defined, only transitioned to.
      internalState = new InternalStateMachineState()
      internalStates.put(state, internalState)
    }

    internalState
  }

  /**
   * Make sure the state machine is valid.
   *
   * @throws SmartSpacesException
   *           the state machine is invalid
   */
  def validate (): Unit = {
    internalStates.entrySet().asScala.foreach { (internalState) =>
      if (internalState.getValue().state.isEmpty) {
        throw new SimpleSmartSpacesException(
          s"State ${internalState.getKey()} is not explicity defined")
      }
    }

  }

  /**
   * The internal representation of a state.
   *
   * @author Keith M. Hughes
   */
  private[statemachine] class InternalStateMachineState(var state: Option[S]) {

    /**
     * A mapping of transition objects to the state for that transition.
     */
    private val transitionToState = new HashMap[T, InternalStateMachineState]()

    /**
     * The action to perform when entering the state. Can be {@code null}.
     */
    var enterAction: StateMachineAction[S, T, SO] = null

    /**
     * The action to perform when exiting the state. Can be {@code null}.
     */
    var exitAction: StateMachineAction[S, T, SO] = null

    /**
     * Construct an internal state without a state marker.
     */
    def this() = {
      this(None)
    }

    /**
     * Enter a state.
     *
     * @param stateObject
     *          the state object
     */
    def doEnterState (stateObject: SO ): Unit= {
      if (enterAction != null) {
        enterAction.performAction(stateObject)
      }
    }

    /**
     * Exit a state.
     *
     * @param stateObject
     *          the state object
     */
    def doExitState (stateObject: SO) = {
      if (exitAction != null) {
        exitAction.performAction(stateObject)
      }
    }

    /**
     * Add in a new transition.
     *
     * @param transition
     *          the transition marker
     * @param newState
     *          the new state the transition should go to
     */
    def addTransition(transition: T, newState: S): Unit = {
      transitionToState.put(transition, getTransitionInternalState(newState))
    }

    /**
     * Get the state associated with a transition.
     *
     * @param transition
     *          the transition
     *
     * @return the state associated with the transition, or {@code null} if none
     */
    def getTransitionState(transition: T): InternalStateMachineState = {
      transitionToState.get(transition)
    }
    
    def getTransitions(): List[T] = {
      transitionToState.keySet().asScala.toList
    }
  }
}



