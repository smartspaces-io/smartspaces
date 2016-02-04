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
public interface StateMachineAction<S, T, SO extends StateMachineObject<S, T>> {

  /**
   * Perform the action.
   *
   * @param object
   *          the state machine state object
   */
  void performAction(SO object);
}
