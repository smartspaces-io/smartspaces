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
 * Various useful external constants for various rule components for sensed values.
 *
 * @author Keith M. Hughes
 */
object SensedValueRuleComponents {

  /**
   * The kind for the sensor channel sensed value rule trigger.
   */
  val KIND_TRIGGER_SENSOR_CHANNEL_SENSED_VALUE =
    "io.smartspaces.rule.trigger.sensor.channel.sensed.value"

  /**
   * The kind for the simple continuous sensed value above rule guard.
   */
  val KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_ABOVE =
    "io.smartspaces.rule.guard.simple.numeric.continuous.sensed.value.above"

  /**
   * The kind for the simple continuous sensed value below rule guard.
   */
  val KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_BELOW =
    "io.smartspaces.rule.guard.simple.numeric.continuous.sensed.value.below"

  /**
   * The kind for the logging of a sensed value rule action.
   */
  val KIND_ACTION_SENSED_VALUE_LOG  =
    "io.smartspaces.rule.action.sensed.value.log"
}

/**
 * A rule trigger for sensed values from a sensor channel.
 *
 * @author Keith M. Hughes
 */
class SensorChannelSensedValueRuleTrigger(
  override val triggerName: String,
  override val rule: Rule,
  val sensorChannelModel: SensorChannelEntityModel,
  val sensedValueValueName: String) extends BaseRuleTrigger {

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
      context.setValue(sensedValueValueName, value)
    }

    rule.triggered(this, ruleInvocationInitialize)
  }
}

/**
 * Various constants and other components useful for the sensor channel sensed value rule trigger.
 *
 * @author Keith M. Hughes
 */
object SensorChannelSensedValueRuleTriggerKindImporter {

  /**
   * The field name in the data section for the sensor ID.
   */
  val DATA_FIELD_NAME_SENSOR_ID = "sensorId"

  /**
   * The field name in the data section for the sensor channel ID.
   */
  val DATA_FIELD_NAME_SENSOR_CHANNEL_ID = "sensorChannelId"

  /**
   * The field name in the data section for the sensed value value name.
   */
  val DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME = "sensedValueValueName"
}

/**
 * Import a kind of rule trigger.
 *
 * @author Keith M. Hughes
 */
class SensorChannelSensedValueRuleTriggerKindImporter(
  sensorModel: CompleteSensedEntityModel) extends RuleTriggerKindImporter {

  override val importerKind: String = {
    SensedValueRuleComponents.KIND_TRIGGER_SENSOR_CHANNEL_SENSED_VALUE
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleTrigger = {

    val triggerName = source.getRequiredString(DynamicObjectRuleImporter.ITEM_NAME)

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val sensorChannelModel = {
      val sensorId = source.getRequiredString(
        SensorChannelSensedValueRuleTriggerKindImporter.DATA_FIELD_NAME_SENSOR_ID)
      val sensorChannelId = source.getRequiredString(
        SensorChannelSensedValueRuleTriggerKindImporter.DATA_FIELD_NAME_SENSOR_CHANNEL_ID)

      sensorModel.getSensorEntityModelByExternalId(sensorId).flatMap(
        _.getSensorChannelEntityModel(sensorChannelId))
    }
    val valueName = source.getRequiredString(
      SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter.DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME)

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
  val sensedValueValueName: String) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(sensedValueValueName)

    value.value > thresholdValue
  }
}

/**
 * Various constants and other components useful for the simple numeric continuous sensed value above rule guard.
 *
 * @author Keith M. Hughes
 */
object SimpleNumericContinuousSensedValueAboveRuleGuardKindImporter {

  /**
   * The field name in the data section for the threshold.
   */
  val DATA_FIELD_NAME_THRESHOLD = "threshold"

  /**
   * The field name in the data section for the sensed value value name.
   */
  val DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME = "sensedValueValueName"
}

/**
 * Import a simple numeric continuous sensed value above rule guard.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueAboveRuleGuardKindImporter extends RuleGuardKindImporter {

  override val importerKind: String = {
    SensedValueRuleComponents.KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_ABOVE
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleGuard = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val thresholdValue = source.getRequiredDouble(
      SimpleNumericContinuousSensedValueAboveRuleGuardKindImporter.DATA_FIELD_NAME_THRESHOLD)
    val valueName = source.getRequiredString(
      SimpleNumericContinuousSensedValueAboveRuleGuardKindImporter.DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME)

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
  sensedValueValueName: String) extends BaseRuleGuard {

  override def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean = {
    val value: SimpleNumericContinuousSensedValue = executionContext.getValue(sensedValueValueName)

    value.value < thresholdValue
  }
}

/**
 * Various constants and other components useful for the simple numeric continuous sensed value below rule guard.
 *
 * @author Keith M. Hughes
 */
object SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter {

  /**
   * The field name in the data section for the threshold.
   */
  val DATA_FIELD_NAME_THRESHOLD = "threshold"

  /**
   * The field name in the data section for the sensed value value name.
   */
  val DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME = "sensedValueValueName"
}

/**
 * Import a simple numeric continuous sensed value below rule guard.
 *
 * @author Keith M. Hughes
 */
class SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter extends RuleGuardKindImporter {

  override val importerKind: String = {
    SensedValueRuleComponents.KIND_GUARD_SIMPLE_NUMERIC_CONTINUOUS_SENSED_VALUE_BELOW
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleGuard = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val thresholdValue = source.getRequiredDouble(
      SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter.DATA_FIELD_NAME_THRESHOLD)
    val valueName = source.getRequiredString(
      SimpleNumericContinuousSensedValueBelowRuleGuardKindImporter.DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME)

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
  val sensedValueValueName: String) extends BaseRuleAction {

  override def evaluate(rule: Rule, trigger: RuleTrigger, executionContext: ExecutionContext): Unit = {
    executionContext.getLog.info(s"Rule action for ${rule.ruleName} ${executionContext.getValue(sensedValueValueName)}")
  }
}

/**
 * Various constants and other components useful for the simple numeric continuous sensed value below rule guard.
 *
 * @author Keith M. Hughes
 */
object SensedValueLoggingRuleActionKindImporter {

  /**
   * The field name in the data section for the sensed value value name.
   */
  val DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME = "sensedValueValueName"
}

/**
 * Import a simple sensed value logging action.
 *
 * @author Keith M. Hughes
 */
class SensedValueLoggingRuleActionKindImporter extends RuleActionKindImporter {

  override val importerKind: String = {
    SensedValueRuleComponents.KIND_ACTION_SENSED_VALUE_LOG 
  }

  override def importRuleComponent(source: DynamicObject, rule: Rule): RuleAction = {

    source.down(DynamicObjectRuleImporter.SECTION_DATA)

    val valueName = source.getRequiredString(
      SensedValueLoggingRuleActionKindImporter.DATA_FIELD_NAME_SENSED_VALUE_VALUE_NAME)

    source.up

    new SensedValueLoggingRuleAction(valueName)
  }
}
