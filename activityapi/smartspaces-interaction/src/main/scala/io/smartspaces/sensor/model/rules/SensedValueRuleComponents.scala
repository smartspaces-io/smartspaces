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
import io.smartspaces.interaction.rules.BaseRuleAction
import io.smartspaces.interaction.rules.BaseRuleGuard
import io.smartspaces.interaction.rules.BaseRuleTrigger
import io.smartspaces.interaction.rules.DynamicObjectRuleImporter
import io.smartspaces.interaction.rules.Rule
import io.smartspaces.interaction.rules.RuleAction
import io.smartspaces.interaction.rules.RuleActionKindImporter
import io.smartspaces.interaction.rules.RuleGuard
import io.smartspaces.interaction.rules.RuleGuardKindImporter
import io.smartspaces.interaction.rules.RuleTrigger
import io.smartspaces.interaction.rules.RuleTriggerKindImporter
import io.smartspaces.sensor.model.CompleteSensedEntityModel
import io.smartspaces.sensor.model.SensedValue
import io.smartspaces.sensor.model.SensorChannelEntityModel
import io.smartspaces.sensor.model.SimpleNumericContinuousSensedValue
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * A rule trigger for sensed values from a sensor channel.
 *
 * @author Keith M. Hughes
 */
class SensorChannelSensedValueRuleTrigger(
  override val triggerName: String,
  override val rule: Rule,
  val sensorChannelModel: SensorChannelEntityModel,
  val measurmentValueName: String) extends BaseRuleTrigger {

  override def initialize(): Unit = {
    sensorChannelModel.addSensorChannelSensedValueRuleTrigger(this)
  }

  /**
   * The value has been updated.
   *
   * @param value
   *        the sensed value
   */
  def updateValue(value: SensedValue[Any]): Unit = {

    def ruleInvocationInitialize = (context: ExecutionContext) => {
      context.setValue(measurmentValueName, value)
    }

    rule.triggered(this, ruleInvocationInitialize)
  }
}

/**
 * Import a kind of rule trigger.
 *
 * @author Keith M. Hughes
 */
class SensorChannelSensedValueRuleTriggerKindImporter(
  sensorModel: CompleteSensedEntityModel) extends RuleTriggerKindImporter {

  override val importerKind: String = {
    RuleComponentConstants.KIND_TRIGGER_SENSOR_CHANNEL_SENSED_VALUE
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleTrigger = {

    val triggerName = source.getRequiredString(DynamicObjectRuleImporter.ITEM_NAME)

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val sensorChannelModel = {
      val sensorId = source.getRequiredString(
        RuleComponentConstants.DATA_FIELD_NAME_SENSOR_ID)
      val sensorChannelId = source.getRequiredString(
        RuleComponentConstants.DATA_FIELD_NAME_SENSOR_CHANNEL_ID)

      sensorModel.getSensorEntityModelByExternalId(sensorId).flatMap(
        _.getSensorChannelEntityModel(sensorChannelId))
    }
    val valueName = source.getRequiredString(
      RuleComponentConstants.DATA_FIELD_NAME_MEASUREMENT_VALUE_NAME)

    source.up

    new SensorChannelSensedValueRuleTrigger(triggerName, rule, sensorChannelModel.get, valueName)
  }
}

/**
 * A rule conditional of a numeric continuous value goes above a threshold.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueAboveRuleGuard(
  val thresholdValue: Double,
  val measurmentValueName: String) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(measurmentValueName)

    if (value.value > thresholdValue) {
      executionContext.setValue(
        RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
        thresholdValue)
      executionContext.setValue(
        RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
        RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_ABOVE)

      true
    } else {
      false
    }
  }
}

/**
 * Import a simple numeric continuous sensed value above rule guard.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueAboveRuleGuardKindImporter extends RuleGuardKindImporter {

  override val importerKind: String = {
    RuleComponentConstants.KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_ABOVE
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleGuard = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val thresholdValue = source.getRequiredDouble(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD)
    val valueName = source.getRequiredString(
      RuleComponentConstants.DATA_FIELD_NAME_MEASUREMENT_VALUE_NAME)

    source.up

    new SimpleNumericContinuousSensedValueAboveRuleGuard(thresholdValue, valueName)
  }
}

/**
 * A rule conditional of a numeric continuous value goes below a threshold.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueBelowRuleGuard(
  val thresholdValue: Double,
  val measurementValueName: String) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(measurementValueName)

    if (value.value < thresholdValue) {
      executionContext.setValue(
        RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD,
        thresholdValue)
      executionContext.setValue(
        RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE,
        RuleComponentConstants.DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_BELOW)

      true
    } else {
      false
    }
  }
}

/**
 * Import a simple numeric continuous sensed value below rule guard.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter extends RuleGuardKindImporter {

  override val importerKind: String = {
    RuleComponentConstants.KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_BELOW
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleGuard = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val thresholdValue = source.getRequiredDouble(
      RuleComponentConstants.DATA_FIELD_NAME_THRESHOLD)
    val valueName = source.getRequiredString(
      RuleComponentConstants.DATA_FIELD_NAME_MEASUREMENT_VALUE_NAME)

    source.up

    new SimpleNumericContinuousSensedValueBelowRuleGuard(thresholdValue, valueName)
  }
}

/**
 * A rule conditional of a numeric continuous value goes below a threshold.
 *
 * @author Keith M. Hughes
 */
class SensedValueLoggingRuleAction(
  val measurmentValueName: String) extends BaseRuleAction {

  override def evaluate(rule: Rule, trigger: RuleTrigger, executionContext: ExecutionContext): Unit = {
    executionContext.getLog.info(s"Rule action for ${rule.ruleName} ${executionContext.getValue(measurmentValueName)}")
  }
}

/**
 * Import a simple sensed value logging action.
 *
 * @author Keith M. Hughes
 */
class SensedValueLoggingRuleActionKindImporter extends RuleActionKindImporter {

  override val importerKind: String = {
    RuleComponentConstants.KIND_ACTION_SENSED_VALUE_LOG
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleAction = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val valueName = source.getRequiredString(
      RuleComponentConstants.DATA_FIELD_NAME_MEASUREMENT_VALUE_NAME)

    source.up

    new SensedValueLoggingRuleAction(valueName)
  }
}
