/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.interaction.rules

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite
import io.smartspaces.evaluation.ExecutionContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tests for the standard rule.
 *
 * @author Keith M. Hughes
 */
class StandardRuleTest extends JUnitSuite {
  @Mock var rootExecutionContext: ExecutionContext = _

  var rule: StandardRule = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    rule = new StandardRule("foo", rootExecutionContext)
  }

  /**
   * Test a rule initialized.
   */
  @Test def testRuleInitialize(): Unit = {
 
    val trigger = Mockito.mock(classOf[RuleTrigger])

    val guard1 = Mockito.mock(classOf[RuleGuard])

    val action1 = Mockito.mock(classOf[RuleAction])
 
    rule.addRuleTrigger(trigger)
    rule.addRuleGuard(guard1)
    rule.addRuleAction(action1)

    Mockito.verify(trigger, Mockito.times(1)).initialize()
    Mockito.verify(guard1, Mockito.times(1)).initialize()

    Mockito.verify(action1, Mockito.times(1)).initialize()
  }

  /**
   * Test a rule firing.
   */
  @Test def testRuleFire(): Unit = {
    val executionContext = Mockito.mock(classOf[ExecutionContext])

    Mockito.when(rootExecutionContext.push()).thenReturn(executionContext)

    val trigger = Mockito.mock(classOf[RuleTrigger])

    val guard1 = Mockito.mock(classOf[RuleGuard])
    val guard2 = Mockito.mock(classOf[RuleGuard])

    Mockito.when(guard1.evaluate(rule, executionContext)).thenReturn(true)
    Mockito.when(guard2.evaluate(rule, executionContext)).thenReturn(true)

    val action1 = Mockito.mock(classOf[RuleAction])
    val action2 = Mockito.mock(classOf[RuleAction])

    rule.addRuleTrigger(trigger)
    rule.addRuleGuard(guard1)
    rule.addRuleGuard(guard2)
    rule.addRuleAction(action1)
    rule.addRuleAction(action2)

    val called = new AtomicBoolean(false)
    def ruleInvocationInitialize = (context: ExecutionContext) => {
      Assert.assertEquals(executionContext, context)

      called.set(true)
    }

    rule.triggered(trigger, ruleInvocationInitialize)

    Assert.assertTrue(called.get)

    Mockito.verify(guard1, Mockito.times(1)).evaluate(rule, executionContext)
    Mockito.verify(guard2, Mockito.times(1)).evaluate(rule, executionContext)

    Mockito.verify(action1, Mockito.times(1)).evaluate(rule, trigger, executionContext)
    Mockito.verify(action2, Mockito.times(1)).evaluate(rule, trigger, executionContext)
  }

  /**
   * Test a rule not firing because the first guard fails.
   */
  @Test def testRuleNoFireGuard1Fail(): Unit = {
    val executionContext = Mockito.mock(classOf[ExecutionContext])

    Mockito.when(rootExecutionContext.push()).thenReturn(executionContext)

    val trigger = Mockito.mock(classOf[RuleTrigger])

    val guard1 = Mockito.mock(classOf[RuleGuard])
    val guard2 = Mockito.mock(classOf[RuleGuard])

    Mockito.when(guard1.evaluate(rule, executionContext)).thenReturn(false)
    Mockito.when(guard2.evaluate(rule, executionContext)).thenReturn(true)

    val action1 = Mockito.mock(classOf[RuleAction])
    val action2 = Mockito.mock(classOf[RuleAction])

    rule.addRuleTrigger(trigger)
    rule.addRuleGuard(guard1)
    rule.addRuleGuard(guard2)
    rule.addRuleAction(action1)
    rule.addRuleAction(action2)

    val called = new AtomicBoolean(false)
    def ruleInvocationInitialize = (context: ExecutionContext) => {
      Assert.assertEquals(executionContext, context)

      called.set(true)
    }

    rule.triggered(trigger, ruleInvocationInitialize)

    Assert.assertTrue(called.get)

    Mockito.verify(guard1, Mockito.times(1)).evaluate(rule, executionContext)
    Mockito.verify(guard2, Mockito.times(1)).evaluate(rule, executionContext)

    Mockito.verify(action1, Mockito.times(0)).evaluate(rule, trigger, executionContext)
    Mockito.verify(action2, Mockito.times(0)).evaluate(rule, trigger, executionContext)
  }

  /**
   * Test a rule not firing because the second guard fails.
   */
  @Test def testRuleNoFireGuard2Fail(): Unit = {
    val executionContext = Mockito.mock(classOf[ExecutionContext])

    Mockito.when(rootExecutionContext.push()).thenReturn(executionContext)

    val trigger = Mockito.mock(classOf[RuleTrigger])

    val guard1 = Mockito.mock(classOf[RuleGuard])
    val guard2 = Mockito.mock(classOf[RuleGuard])

    Mockito.when(guard1.evaluate(rule, executionContext)).thenReturn(true)
    Mockito.when(guard2.evaluate(rule, executionContext)).thenReturn(false)

    val action1 = Mockito.mock(classOf[RuleAction])
    val action2 = Mockito.mock(classOf[RuleAction])

    rule.addRuleTrigger(trigger)
    rule.addRuleGuard(guard1)
    rule.addRuleGuard(guard2)
    rule.addRuleAction(action1)
    rule.addRuleAction(action2)

    val called = new AtomicBoolean(false)
    def ruleInvocationInitialize = (context: ExecutionContext) => {
      Assert.assertEquals(executionContext, context)

      called.set(true)
    }

    rule.triggered(trigger, ruleInvocationInitialize)

    Assert.assertTrue(called.get)

    Mockito.verify(guard1, Mockito.times(0)).evaluate(rule, executionContext)
    Mockito.verify(guard2, Mockito.times(1)).evaluate(rule, executionContext)

    Mockito.verify(action1, Mockito.times(0)).evaluate(rule, trigger, executionContext)
    Mockito.verify(action2, Mockito.times(0)).evaluate(rule, trigger, executionContext)
  }
}
