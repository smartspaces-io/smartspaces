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

    val valueName = "glorp"

    val trigger = new SensorChannelSensedValueRuleTrigger("trigger1", rule, sensorChannelModel, valueName)

    val value = 100.0

    val sensedValue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, value, None, 1000, 1000)

    trigger.updateValue(sensedValue)

    Mockito.verify(executionContext).setValue(valueName, sensedValue)
  }

  /**
   * Test the rule guard for sensor channel sensed values above the supplied value.
   */
  @Test def testSimpleNumericContinuousSensedValueAboveRuleGuard(): Unit = {
    val rootExecutionContext = Mockito.mock(classOf[ExecutionContext])

    val rule = new StandardRule("foo", rootExecutionContext)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])

    val valueName = "glorp"

    val threshold = 1000.0
    val guard = new SimpleNumericContinuousSensedValueAboveRuleGuard(threshold, valueName)

    val sensedValueTrue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 1001, None, 1000, 1000)
    val executionContextTrue = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextTrue.getValue(valueName)).
      thenReturn(sensedValueTrue)

    Assert.assertTrue(guard.evaluate(rule, executionContextTrue))
    Mockito.verify(executionContextTrue, Mockito.times(1)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
      threshold)
    Mockito.verify(executionContextTrue, Mockito.times(1)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
      RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_ABOVE)

    val sensedValueFalse = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 999, None, 1000, 1000)
    val executionContextFalse = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextFalse.getValue(valueName)).
      thenReturn(sensedValueFalse)

    Assert.assertFalse(guard.evaluate(rule, executionContextFalse))
    Mockito.verify(executionContextFalse, Mockito.times(0)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
      threshold)
    Mockito.verify(executionContextFalse, Mockito.times(0)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
      RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_ABOVE)
  }

  /**
   * Test the rule guard for sensor channel sensed values below the supplied value.
   */
  @Test def testSimpleNumericContinuousSensedValueBelowRuleGuard(): Unit = {
    val rootExecutionContext = Mockito.mock(classOf[ExecutionContext])

    val rule = new StandardRule("foo", rootExecutionContext)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])

    val valueName = "glorp"

    val threshold = 1000.0
    val guard = new SimpleNumericContinuousSensedValueBelowRuleGuard(threshold, valueName)

    val sensedValueTrue = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 999, None, 999, 1000)
    val executionContextTrue = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextTrue.getValue(valueName)).
      thenReturn(sensedValueTrue)

    Assert.assertTrue(guard.evaluate(rule, executionContextTrue))
    Mockito.verify(executionContextTrue, Mockito.times(1)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
      threshold)
    Mockito.verify(executionContextTrue, Mockito.times(1)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
      RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_BELOW)

    val sensedValueFalse = new SimpleNumericContinuousSensedValue(
      sensorChannelModel, 1000, None, 1000, 1000)
    val executionContextFalse = Mockito.mock(classOf[ExecutionContext])
    Mockito.when(executionContextFalse.getValue(valueName)).
      thenReturn(sensedValueFalse)

    Assert.assertFalse(guard.evaluate(rule, executionContextFalse))
    Mockito.verify(executionContextFalse, Mockito.times(0)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
      threshold)
    Mockito.verify(executionContextFalse, Mockito.times(0)).setValue(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
      RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_BELOW)
  }
}
