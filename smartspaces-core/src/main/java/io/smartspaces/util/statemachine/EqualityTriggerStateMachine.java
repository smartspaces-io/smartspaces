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
import io.smartspaces.SmartSpacesException;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A state machine which uses equality on the trigger class to transition to the
 * next state.
 *
 * @param <S>
 *          The state indicator type
 * @param <T>
 *          The transition indicator type
 * @param <SO>
 *          The state object
 *
 * @author Keith M. Hughes
 */
public class EqualityTriggerStateMachine<S, T, SO extends StateMachineObject<S, T>> implements
    StateMachine<S, T, SO> {

  /**
   * A map from states to the internal state object representation.
   */
  private Map<S, InternalStateMachineState> internalStates = new HashMap<>();

  @Override
  public void initialState(SO stateObject, S initialState) throws SmartSpacesException {
    InternalStateMachineState currentInternalState = internalStates.get(initialState);
    if (currentInternalState != null) {
      stateObject.setState(initialState);
      currentInternalState.enterState(stateObject);
    } else {
      throw new SmartSpacesException(String.format("Cannot set initial state %s",
          initialState));
    }
  }

  @Override
  public void transition(SO stateObject, T nextStateTransition) {
    S currentState = stateObject.getState();

    InternalStateMachineState currentInternalState = internalStates.get(currentState);

    InternalStateMachineState nextInternalState =
        currentInternalState.getTransitionState(nextStateTransition);

    if (nextInternalState == null) {
      throw new SimpleSmartSpacesException(String.format(
          "There is no transition named %s from state %s", nextStateTransition, currentState));
    }

    currentInternalState.exitState(stateObject);

    stateObject.setState(nextInternalState.getState());
    nextInternalState.enterState(stateObject);
  }

  /**
   * Add in a new internal state to the machine.
   *
   * @param state
   *          the new state
   *
   * @return the internal state
   */
  InternalStateMachineState addInternalState(S state) {
    InternalStateMachineState internalState = internalStates.get(state);
    if (internalState != null) {
      if (internalState.getState() == null) {
        internalState.setState(state);
      } else {
        throw new SimpleSmartSpacesException(String.format(
            "State machine already contained state %s", state));
      }
    } else {
      internalState = new InternalStateMachineState(state);
      internalStates.put(state, internalState);
    }

    return internalState;
  }

  /**
   * Get an internal state for a transition.
   *
   * @param state
   *          the state the transition should go to
   *
   * @return the internal state
   */
  InternalStateMachineState getTransitionInternalState(S state) {
    InternalStateMachineState internalState = internalStates.get(state);
    if (internalState == null) {
      // Don't want the state associated yet so we can fail validation if the
      // state was never
      // defined, only transitioned to.
      internalState = new InternalStateMachineState();
      internalStates.put(state, internalState);
    }

    return internalState;
  }

  /**
   * Make sure the state machine is valid.
   *
   * @throws SmartSpacesException
   *           the state machine is invalid
   */
  void validate() throws SmartSpacesException {
    for (Entry<S, InternalStateMachineState> internalState : internalStates.entrySet()) {
      if (internalState.getValue().getState() == null) {
        throw new SimpleSmartSpacesException(String.format(
            "State %s is not explicity defined", internalState.getKey()));
      }
    }

  }

  /**
   * The internal representation of a state.
   *
   * @author Keith M. Hughes
   */
  class InternalStateMachineState {

    /**
     * The state this class represents.
     */
    private S state;

    /**
     * A mapping of transition objects to the state for that transition.
     */
    private final Map<T, InternalStateMachineState> transitionToState = new HashMap<>();

    /**
     * The action to perform when entering the state. Can be {@code null}.
     */
    private StateMachineAction<S, T, SO> enterAction;

    /**
     * The action to perform when exiting the state. Can be {@code null}.
     */
    private StateMachineAction<S, T, SO> exitAction;

    /**
     * Construct an internal state without a state marker.
     */
    public InternalStateMachineState() {
    }

    /**
     * Construct an internal state with a given state marker.
     *
     * @param state
     *          the state marker
     */
    public InternalStateMachineState(S state) {
      this.state = state;
    }

    /**
     * Enter a state.
     *
     * @param stateObject
     *          the state object
     */
    void enterState(SO stateObject) {
      if (enterAction != null) {
        enterAction.performAction(stateObject);
      }
    }

    /**
     * Exit a state.
     *
     * @param stateObject
     *          the state object
     */
    void exitState(SO stateObject) {
      if (exitAction != null) {
        exitAction.performAction(stateObject);
      }
    }

    /**
     * Set the enter action.
     *
     * @param enterAction
     *          the enter action
     */
    void setEnterAction(StateMachineAction<S, T, SO> enterAction) {
      this.enterAction = enterAction;
    }

    /**
     * Set the exit action.
     *
     * @param exitAction
     *          the exit action
     */
    void setExitAction(StateMachineAction<S, T, SO> exitAction) {
      this.exitAction = exitAction;
    }

    /**
     * Get the state represented by this internal state.
     *
     * @return the state represented by this internal state
     */
    S getState() {
      return state;
    }

    /**
     * Set the state represented by this internal state.
     *
     * @param state
     *          the state represented by this internal state
     */
    void setState(S state) {
      this.state = state;
    }

    /**
     * Add in a new transition.
     *
     * @param transition
     *          the transition marker
     * @param newState
     *          the new state the transition should go to
     */
    void addTransition(T transition, S newState) {
      transitionToState.put(transition, getTransitionInternalState(newState));
    }

    /**
     * Get the state associated with a transition.
     *
     * @param transition
     *          the transition
     *
     * @return the state associated with the transition, or {@code null} if none
     */
    InternalStateMachineState getTransitionState(T transition) {
      return transitionToState.get(transition);
    }
  }
}
