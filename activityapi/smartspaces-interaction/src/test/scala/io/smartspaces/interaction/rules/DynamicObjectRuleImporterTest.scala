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
import io.smartspaces.evaluation.ExecutionContextFactory
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.util.data.mapper.YamlDataMapper
import io.smartspaces.util.data.mapper.StandardYamlDataMapper
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator
import org.mockito.Matchers

/**
 * Tests for the dynamic object rule importer.
 *
 * @author Keith M. Hughes
 */
class DynamicObjectRuleImporterTest extends JUnitSuite {

  @Mock var log: ExtendedLog = _
  @Mock var executionContextFactory: ExecutionContextFactory = _

  var importer: DynamicObjectRuleImporter = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    importer = new DynamicObjectRuleImporter(log)
  }

  /**
   * Test a rule initialized.
   */
  @Test def testRuleInitialize(): Unit = {
    val triggerKind1 = "trigger.1"
    val triggerName1 = "triggerfoo1"
    
    val triggerKind2 = "trigger.2"
    val triggerName2 = "triggerfoo2"

    val guardKind1 = "guard.1"
    val guardKind2 = "guard.2"

    val actionKind1 = "action.1"
    val actionKind2 = "action.2"
     
    val ruleName = "foo"

    val rules = s"""
rules:
  - name: ${ruleName}
    triggers:
      - kind: ${triggerKind1}
        name: triggerfoo1
      - kind: ${triggerKind2}
        name: triggerfoo2
    guards:
      - kind: ${guardKind1}
      - kind: ${guardKind2}
    actions:
      - kind: ${actionKind1}
      - kind: ${actionKind2}
"""

    val source = new StandardDynamicObjectNavigator(StandardYamlDataMapper.INSTANCE.parse(rules))

    val trigger1 = Mockito.mock(classOf[RuleTrigger])
    Mockito.when(trigger1.triggerName).thenReturn(triggerName1)
    val trigger1Factory = Mockito.mock(classOf[RuleTriggerKindImporter])

    Mockito.when(trigger1Factory.importerKind).thenReturn(triggerKind1)
    Mockito.when(trigger1Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(trigger1)

    val trigger2 = Mockito.mock(classOf[RuleTrigger])
    Mockito.when(trigger2.triggerName).thenReturn(triggerName2)
    val trigger2Factory = Mockito.mock(classOf[RuleTriggerKindImporter])

    Mockito.when(trigger2Factory.importerKind).thenReturn(triggerKind2)
    Mockito.when(trigger2Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(trigger2)

    val guard1 = Mockito.mock(classOf[RuleGuard])
    val guard1Factory = Mockito.mock(classOf[RuleGuardKindImporter])

    Mockito.when(guard1Factory.importerKind).thenReturn(guardKind1)
    Mockito.when(guard1Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(guard1)

    val guard2 = Mockito.mock(classOf[RuleGuard])
    val guard2Factory = Mockito.mock(classOf[RuleGuardKindImporter])

    Mockito.when(guard2Factory.importerKind).thenReturn(guardKind2)
    Mockito.when(guard2Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(guard2)

    val action1 = Mockito.mock(classOf[RuleAction])
    val action1Factory = Mockito.mock(classOf[RuleActionKindImporter])

    Mockito.when(action1Factory.importerKind).thenReturn(actionKind1)
    Mockito.when(action1Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(action1)

    val action2 = Mockito.mock(classOf[RuleAction])
    val action2Factory = Mockito.mock(classOf[RuleActionKindImporter])

    Mockito.when(action2Factory.importerKind).thenReturn(actionKind2)
    Mockito.when(action2Factory.importRuleComponent(Matchers.eq(source), Matchers.any(classOf[Rule]))).thenReturn(action2)
    
    importer.addRuleTriggerImporter(trigger1Factory)
    importer.addRuleTriggerImporter(trigger2Factory)
    importer.addRuleGuardImporter(guard1Factory)
    importer.addRuleGuardImporter(guard2Factory)
    importer.addRuleActionImporter(action1Factory)
    importer.addRuleActionImporter(action2Factory)

    val executionContext = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextFactory.newContext()).thenReturn(executionContext)

    val collection = importer.importRules(source, executionContextFactory)

    val rule = collection.getRule(ruleName)

    Assert.assertTrue(rule.isDefined)
    Assert.assertEquals(ruleName, rule.get.ruleName)
    Assert.assertEquals(executionContext, rule.get.rootExecutionContext)
    
    Assert.assertEquals(trigger1, rule.get.getRuleTrigger(triggerName1).get)
    Assert.assertEquals(trigger2, rule.get.getRuleTrigger(triggerName2).get)
    
    Assert.assertEquals(Set(guard1, guard2), rule.get.ruleGuards.toSet)
    Assert.assertEquals(Set(action1, action2), rule.get.ruleActions.toSet)
    
  }
}