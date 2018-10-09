/*
 * Copyright (C) 2018 Inhabitech
 *
 * This code is Inhabitech Confidential and is not to be shared without explicit written permission.
 */

package io.smartspaces.sensor.model.rules

/**
 * Constants for rule components.
 * 
 * @author Keith M. Hughes
 */
object RuleComponentConstants {

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

  /**
   * The field name in the data section for the measurement value name.
   */
  val DATA_FIELD_NAME_MEASUREMENT_VALUE_NAME = "measurementValueName"

  /**
   * The field name in the data section for the sensor ID.
   */
  val DATA_FIELD_NAME_SENSOR_ID = "sensorId"

  /**
   * The field name in the data section for the sensor channel ID.
   */
  val DATA_FIELD_NAME_SENSOR_CHANNEL_ID = "sensorChannelId"  

  /**
   * The field name in the data section for the threshold.
   */
  val DATA_FIELD_NAME_THRESHOLD = "threshold"

  /**
   * The field name in the data section for the threshold commprison_type.
   */
  val DATA_FIELD_NAME_THRESHOLD_COMPARISON_TYPE = "thresholdComparisonType"

  /**
   * The field type for the above threshold commprison_type.
   */
  val DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_ABOVE = "threshold.comparison.type.above"

  /**
   * The field type for the below threshold commprison_type.
   */
  val DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_BELOW = "threshold.comparison.type.below"

  /**
   * The field type for the between threshold commprison_type.
   */
  val DATA_FIELD_VALUE_THRESHOLD_COMPARISON_TYPE_BETWEEN = "threshold.comparison.type.between"
}
