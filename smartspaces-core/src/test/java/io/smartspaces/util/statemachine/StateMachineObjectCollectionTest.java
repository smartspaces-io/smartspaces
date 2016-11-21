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

import java.util.List;

/**
 * Tests for the {@class StateMachineObjectCollection}.
 *
 * @author Keith M. Hughes
 */
public class StateMachineObjectCollectionTest {

  /**
   * Test one object going through a set of transitions while leaving the other
   * alone.
   */
  @Test
  public void testSimpleMachine() {
    EqualityTriggerStateMachine<TestStates, TestTransitions, MyStateMachineObject> machine =
        newMachine();

    MyStateMachineObject object1 = new MyStateMachineObject("object1");
    MyStateMachineObject object2 = new MyStateMachineObject("object2");

    StateMachineObjectCollection<String, TestStates, TestTransitions, MyStateMachineObject> collection =
        new StateMachineObjectCollection<String, StateMachineObjectCollectionTest.TestStates, StateMachineObjectCollectionTest.TestTransitions, StateMachineObjectCollectionTest.MyStateMachineObject>(
            machine);
    collection.put("object1", object1);
    collection.put("object2", object2);

    collection.setInitialState("object1", TestStates.A);

    Assert.assertEquals(Lists.newArrayList("object1.A.enter"), object1.path);

    collection.transition("object1", TestTransitions.T1);

    Assert.assertEquals(Lists.newArrayList("object1.A.enter", "object1.A.exit", "object1.B.enter"),
        object1.path);

    collection.transition("object1", TestTransitions.T2);

    Assert.assertEquals(Lists.newArrayList("object1.A.enter", "object1.A.exit", "object1.B.enter",
        "object1.B.exit", "object1.C.enter"), object1.path);

    Assert.assertTrue(object2.path.isEmpty());
  }

  private EqualityTriggerStateMachine<TestStates, TestTransitions, MyStateMachineObject>
      newMachine() {
    EqualityTriggerStateMachine<TestStates, TestTransitions, MyStateMachineObject> machine =
        new EqualityTriggerStateMachineBuilder<TestStates, TestTransitions, MyStateMachineObject>()
            .addState(TestStates.A)
            .onEntry(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("A", "enter");
              }
            }).onExit(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("A", "exit");
              }
            }).transition(TestTransitions.T1, TestStates.B).addState(TestStates.B)
            .onEntry(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("B", "enter");
              }
            }).onExit(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("B", "exit");
              }
            }).transition(TestTransitions.T2, TestStates.C).addState(TestStates.C)
            .onEntry(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("C", "enter");
              }
            }).onExit(new StateMachineAction<TestStates, TestTransitions, MyStateMachineObject>() {
              @Override
              public void performAction(MyStateMachineObject object) {
                object.action("C", "exit");
              }
            }).build();
    return machine;
  }

  /**
   * Transition for an object which does not exist.
   */
  @Test(expected = SimpleSmartSpacesException.class)
  public void testFailBuild() {
    EqualityTriggerStateMachine<TestStates, TestTransitions, MyStateMachineObject> machine =
        newMachine();

    MyStateMachineObject object1 = new MyStateMachineObject("object1");
    StateMachineObjectCollection<String, TestStates, TestTransitions, MyStateMachineObject> collection =
        new StateMachineObjectCollection<String, StateMachineObjectCollectionTest.TestStates, StateMachineObjectCollectionTest.TestTransitions, StateMachineObjectCollectionTest.MyStateMachineObject>(
            machine);
    collection.put("object1", object1);

    collection.setInitialState("object2", TestStates.A);
  }

  public enum TestStates {
    A, B, C, D
  }

  public enum TestTransitions {
    T1, T2, T3, T4
  }

  private static class MyStateMachineObject extends
      BaseStateMachineObject<TestStates, TestTransitions> {
    private String name;

    private List<String> path = Lists.newArrayList();

    public MyStateMachineObject(String name) {
      this.name = name;
    }

    private void action(String state, String action) {
      path.add(name + "." + getState().get() + "." + action);
    }
  }
}
