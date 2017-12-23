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
import io.smartspaces.SmartSpacesException

import java.util.Map
import java.util.concurrent.ConcurrentSkipListMap

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
class StateMachineObjectCollection[K, S, T, SO <: StateMachineObject[S]](
    private val machine: StateMachine[S, T, SO]) {

  /**
   * The collection of state machine objects.
   */
  private val  machineObjects = new ConcurrentSkipListMap[K, SO]()

  /**
   * Add in a new object into the collection.
   *
   * @param key
   *          the key for the object
   * @param object
   *          the object
   */
  def put(key: K ,  machineObject: SO): Unit = {
    machineObjects.put(key, machineObject)
  }

  /**
   * Add in a new object into the collection.
   *
   * @param key
   *          the key for the object
   * @param object
   *          the object
   */
  def put(key: K, machineObject: SO , initialState: S ): Unit = {
    put(key, machineObject)
    setInitialState(initialState, machineObject)
  }

  /**
   * Get the object with the associated key.
   *
   * @param key
   *          the key for the object
   *
   * @return the object, or {@code null} if none
   */
  def get(key: K): SO = {
    return machineObjects.get(key)
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
  def setInitialState(key: K ,  initialState: S): Unit = {
    val machineObject = machineObjects.get(key)
    if (machineObject != null) {
      setInitialState(initialState, machineObject)
    } else {
      throw new SimpleSmartSpacesException(String.format(
          "Cannot set initial state %s on object with key %s, object not in the collection",
          initialState.toString, key.toString))
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
  def transition(key: K ,  transition: T): Unit = {
    val machineObject = machineObjects.get(key)
    if (machineObject != null) {
      machineObject.synchronized {
        machine.transition(machineObject, transition)
      }
    } else {
      throw SimpleSmartSpacesException.newFormattedException(
          "Cannot transition object with key %s with transition %s, object not in the collection",
          key.toString, transition.toString)
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
  private def setInitialState( initialState: S, machineObject: SO ): Unit = {
    machineObject.synchronized  {
      machine.initialState(machineObject, initialState)
    }
  }
}
