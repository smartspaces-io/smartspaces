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

package io.smartspaces.sensor.model.rules

import org.junit.Test
import org.mockito.Mockito
import org.scalatest.junit.JUnitSuite

import io.smartspaces.interaction.rules.Rule
import io.smartspaces.sensor.model.SensorChannelEntityModel
import io.smartspaces.sensor.model.SimpleNumericContinuousSensedValue
import io.smartspaces.interaction.rules.StandardRule
import io.smartspaces.evaluation.ExecutionContext
import org.junit.Assert

/**
 * Tests for the various components for sensed value rules.
 *
 * @author Keith M. Hughes
 */
class SensedValueRuleComponentsTest extends JUnitSuite {

  /**
   * Test the rule trigger for sensor channel sensed values.
   */
  @Test def testSensorChannelSensedValueRuleTrigger(): Unit = {
    val rootExecutionContext = Mockito.mock(classOf[ExecutionContext])

    val executionContext = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(rootExecutionContext.push()).thenReturn(executionContext)

    val rule = new StandardRule("foo", rootExecutionContext)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])

    val trigger = new SensorChannelSensedValueRuleTrigger("trigger1", rule, sensorChannelModel)

    val value = 100.0

    val sensedValue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, value, None, 1000, 1000)

    trigger.updateValue(sensedValue)

    Mockito.verify(executionContext).setValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE, sensedValue)
  }

  /**
   * Test the rule guard for sensor channel sensed values above the supplied value.
   */
  @Test def testSimpleNumericContinuousSensedValueAboveRuleGuard(): Unit = {
    val rootExecutionContext = Mockito.mock(classOf[ExecutionContext])


    val rule = new StandardRule("foo", rootExecutionContext)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])

    val guard = new SimpleNumericContinuousSensedValueAboveRuleGuard(1000)

    val sensedValueTrue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 1001, None, 1000, 1000)
    val executionContextTrue = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextTrue.getValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)).
      thenReturn(sensedValueTrue)

    Assert.assertTrue(guard.evaluate(rule, executionContextTrue))

    val sensedValueFalse = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 999, None, 1000, 1000)
    val executionContextFalse = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextFalse.getValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)).
      thenReturn(sensedValueFalse)

    Assert.assertFalse(guard.evaluate(rule, executionContextFalse))
  }

  /**
   * Test the rule guard for sensor channel sensed values below the supplied value.
   */
  @Test def testSimpleNumericContinuousSensedValueBelowRuleGuard(): Unit = {
    val rootExecutionContext = Mockito.mock(classOf[ExecutionContext])


    val rule = new StandardRule("foo", rootExecutionContext)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])

    val guard = new SimpleNumericContinuousSensedValueBelowRuleGuard(1000)

    val sensedValueTrue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 999, None, 999, 1000)
    val executionContextTrue = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextTrue.getValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)).
      thenReturn(sensedValueTrue)

    Assert.assertTrue(guard.evaluate(rule, executionContextTrue))

    val sensedValueFalse = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 1000, None, 1000, 1000)
    val executionContextFalse = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextFalse.getValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)).
      thenReturn(sensedValueFalse)

    Assert.assertFalse(guard.evaluate(rule, executionContextFalse))
  }
}
