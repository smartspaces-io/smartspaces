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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Keith M. Hughes
 */
public class EqualityTriggerStateMachineTest {

  /**
   * Test a simple set of transitions.
   */
  @Test
  public void testSimpleMachine() {
    final ArrayList<String> path = Lists.newArrayList();

    EqualityTriggerStateMachine<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>> machine =
        new EqualityTriggerStateMachineBuilder<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>()
            .addState(TestStates.A)
            .onEntry(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("A.enter");
                  }
                })
            .onExit(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("A.exit");
                  }
                })
            .transition(TestTransitions.T1, TestStates.B)
            .addState(TestStates.B)
            .onEntry(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("B.enter");
                  }
                })
            .onExit(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("B.exit");
                  }
                })
            .transition(TestTransitions.T2, TestStates.C)
            .addState(TestStates.C)
            .onEntry(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("C.enter");
                  }
                })
            .onExit(
                new StateMachineAction<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>() {
                  @Override
                  public void performAction(StateMachineObject<TestStates, TestTransitions> object) {
                    path.add("C.exit");
                  }
                }).build();

    BaseStateMachineObject<TestStates, TestTransitions> stateObject =
        new BaseStateMachineObject<TestStates, TestTransitions>();
    machine.initialState(stateObject, TestStates.A);

    Assert.assertEquals(Lists.newArrayList("A.enter"), path);

    machine.transition(stateObject, TestTransitions.T1);

    Assert.assertEquals(Lists.newArrayList("A.enter", "A.exit", "B.enter"), path);

    machine.transition(stateObject, TestTransitions.T2);

    Assert.assertEquals(Lists.newArrayList("A.enter", "A.exit", "B.enter", "B.exit", "C.enter"),
        path);
  }

  /**
   * Have an invalid state machine fail to build. State C not explicitly added.
   */
  @Test(expected = SimpleSmartSpacesException.class)
  public void testFailBuild() {
    EqualityTriggerStateMachine<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>> machine =
        new EqualityTriggerStateMachineBuilder<TestStates, TestTransitions, StateMachineObject<TestStates, TestTransitions>>()
            .addState(TestStates.A).transition(TestTransitions.T1, TestStates.B)
            .addState(TestStates.B).transition(TestTransitions.T2, TestStates.C).build();
  }

  public enum TestStates {
    A, B, C, D
  }

  public enum TestTransitions {
    T1, T2, T3, T4
  }

}
