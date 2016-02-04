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
 * A base object for storing state machine state.
 *
 * @param <S>
 *          type of the state indicator
 * @param <T>
 *          type of the transition indicator
 *
 * @author Keith M. Hughes
 */
public class BaseStateMachineObject<S, T> implements StateMachineObject<S, T> {

  /**
   * The current state of the object.
   */
  private S state;

  @Override
  public S getState() {
    return state;
  }

  @Override
  public void setState(S state) {
    this.state = state;
  }
}
