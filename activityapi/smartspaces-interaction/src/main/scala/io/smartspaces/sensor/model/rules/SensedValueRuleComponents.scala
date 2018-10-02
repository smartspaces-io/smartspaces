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

import io.smartspaces.evaluation.ExecutionContext
import io.smartspaces.interaction.rules.BaseRuleGuard
import io.smartspaces.interaction.rules.BaseRuleTrigger
import io.smartspaces.interaction.rules.Rule
import io.smartspaces.sensor.model.SensedValue
import io.smartspaces.sensor.model.SensorChannelEntityModel
import io.smartspaces.sensor.model.SimpleNumericContinuousSensedValue

object SensedValueRuleTrigger {
  val CONTEXT_NAME_SENSED_VALUE = "sensedValue"
}

/**
 * A rule trigger for sensed values from a sensor channel.
 *
 * @author Keith M. Hughes
 */
class SensorChannelSensedValueRuleTrigger(
  override val triggerName: String,
  override val rule: Rule,
  val sensorChannelModel: SensorChannelEntityModel) extends BaseRuleTrigger {

  /**
   * The value has been updated.
   *
   * @param value
   *        the sensed value
   */
  def updateValue(value: SensedValue[Any]): Unit = {

    def ruleInvocationInitialize = (context: ExecutionContext) => {
      context.setValue(SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE, value)
      context.setValue("value", value.value)
    }

    rule.triggered(this, ruleInvocationInitialize)
  }
}

/**
 * A rule conditional of a numeric continuous value goes above a trigger.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueAboveRuleGuard(val triggerValue: Double) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(
      SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)

    value.value > triggerValue
  }
}

/**
 * A rule conditional of a numeric continuous value goes below a trigger.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueBelowRuleGuard(val triggerValue: Double) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(
      SensedValueRuleTrigger.CONTEXT_NAME_SENSED_VALUE)

    value.value < triggerValue
  }
}
