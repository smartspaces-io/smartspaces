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

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A collection of state machine objects.
 *
 * <p>
 * This class is typesafe.
 *
 * @param <K>
 *          type of the key
 * @param <S>
 *          type to the state indicator
 * @param <T>
 *          type to the transition indicator
 * @param <SO>
 *          type of the state object
 *
 * @author Keith M. Hughes
 */
public class StateMachineObjectCollection<K, S, T, SO extends StateMachineObject<S, T>> {

  /**
   * The machine doing the transitioning.
   */
  private final StateMachine<S, T, SO> machine;

  /**
   * The collection of state machine objects.
   */
  private final Map<K, SO> objects = new ConcurrentSkipListMap<K, SO>();

  /**
   * @param machine
   */
  public StateMachineObjectCollection(StateMachine<S, T, SO> machine) {
    this.machine = machine;
  }

  /**
   * Add in a new object into the collection.
   *
   * @param key
   *          the key for the object
   * @param object
   *          the object
   */
  public void put(K key, SO object) {
    objects.put(key, object);
  }

  /**
   * Add in a new object into the collection.
   *
   * @param key
   *          the key for the object
   * @param object
   *          the object
   */
  public void put(K key, SO object, S initialState) {
    put(key, object);
    setInitialState(initialState, object);
  }

  /**
   * Get the object with the associated key.
   *
   * @param key
   *          the key for the object
   *
   * @return the object, or {@code null} if none
   */
  public SO get(K key) {
    return objects.get(key);
  }

  /**
   * Set the initial state for an object.
   *
   * @param key
   *          key for the object
   * @param initialState
   *          the initial state
   *
   * @throws SmartSpacesException
   *           an object with the given key was not found in the collection
   */
  public void setInitialState(K key, S initialState) throws SmartSpacesException {
    SO object = objects.get(key);
    if (object != null) {
      setInitialState(initialState, object);
    } else {
      throw new SimpleSmartSpacesException(String.format(
          "Cannot set initial state %s on object with key %s, object not in the collection",
          initialState, key));
    }
  }

  /**
   * Transition the state object with the given key.
   *
   * @param key
   *          the key for the object
   * @param transition
   *          the transition
   *
   * @throws SmartSpacesException
   *           either no object was found with the given key or the transition
   *           failed
   */
  public void transition(K key, T transition) throws SmartSpacesException {
    SO object = objects.get(key);
    if (object != null) {
      synchronized (object) {
        machine.transition(object, transition);
      }
    } else {
      throw new SimpleSmartSpacesException(String.format(
          "Cannot transition object with key %s with transition %s, object not in the collection",
          key, transition));
    }
  }

  /**
   * Set the initial state of a state object.
   *
   * @param initialState
   *          the initial state
   * @param object
   *          the object to get the initial state
   */
  private void setInitialState(S initialState, SO object) {
    synchronized (object) {
      machine.initialState(object, initialState);
    }
  }
}
