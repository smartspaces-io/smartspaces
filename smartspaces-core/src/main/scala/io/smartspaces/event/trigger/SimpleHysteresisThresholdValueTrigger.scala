/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.event.trigger;

/**
 * A simple trigger which watches for the change of a value and will signal
 * rising or falling states.
 *
 * <p>
 * The trigger supports hysteresis. The trigger will only turn on if the updated
 * value is at or above the on threshold. The trigger will then stay on until
 * the update value goes at or below the off threshold. This is useful for noisy
 * data.
 *
 * @author Keith M. Hughes
 */
class SimpleHysteresisThresholdValueTrigger extends ResettableTrigger {

  /**
   * The current value of the trigger.
   */
  private var value: Double = 0.0

  /**
   * The value at which the trigger will switch on.
   */
  private var thresholdOn: Double = 0

  /**
   * The value at which the trigger will switch off.
   */
  private var thresholdOff: Double = 0

  /**
   * The current state of the trigger.
   */
  private var state: TriggerStates.TriggerState = TriggerStates.NOT_TRIGGERED

  /**
   * The previous state of the trigger.
   */
  private var previousState: TriggerStates.TriggerState = TriggerStates.NOT_TRIGGERED

  /**
   * Collection of listeners for trigger point events.
   */
  private var listeners = List[TriggerListener]()

  /**
   * Set the threshold at which the trigger will switch on.
   *
   * @param thresholdOn
   *          the threshold at which the trigger will switch on
   */
  def setThresholdOn(thresholdOn: Double): SimpleHysteresisThresholdValueTrigger = {
    this.thresholdOn = thresholdOn

    this
  }

  /**
   * Get the threshold at which the trigger will switch on.
   *
   * @return the threshold at which the trigger will switch on
   */
  def getThresholdOn(): Double = {
    thresholdOn
  }

  /**
   * Set the value at which the trigger will switch off.
   *
   * @param thresholdOff
   *          the value at which the trigger will switch off
   */
  def setThresholdOff(thresholdOff: Double): SimpleHysteresisThresholdValueTrigger = {
    this.thresholdOff = thresholdOff

    this
  }

  /**
   * Set the value at which the trigger will switch on and off.
   *
   * <p>
   * The on value will be from {@code thresholdOn}. {@code offset} will be
   * subtracted from {@code thresholdOn} to get {@code thresholdOff}.
   *
   * @param thresholdOn
   *          the value at which the trigger will switch on
   * @param offset
   *          the value to subtract from {@code thresholdOn} to get
   *          {@code thresholdOff}
   */
  def setThresholdsWithOffset(
    thresholdOn: Double,
    offset: Double): SimpleHysteresisThresholdValueTrigger = {
    this.thresholdOn = thresholdOn
    this.thresholdOff = thresholdOn - offset

    this
  }

  /**
   * Get the threshold at which the trigger will switch off.
   *
   * @return the threshold at which the trigger will switch off
   */
  def getThresholdOff(): Double = {
    thresholdOn
  }

  override def addListener(listener: TriggerListener): Unit = {
    listeners = listener :: listeners
  }

  override def removeListener(listener: TriggerListener): Unit = {
    listeners = listeners.filter(_ != listener)
  }

  /**
   * Update the value, potentially triggering and notifying listeners.
   *
   * @param newValue
   *          the new value
   */
  def update(newValue: Double): Unit = {
    var newState: TriggerStates.TriggerState = null
    var eventType: TriggerEventTypes.TriggerEventType = null

    var change = false
    if (newValue <= thresholdOff) {
      if (state == TriggerStates.TRIGGERED) {
        newState = TriggerStates.NOT_TRIGGERED
        eventType = TriggerEventTypes.FALLING
        changeState(newValue, newState);
        change = true
      }
    } else if (newValue >= thresholdOn) {
      if (state == TriggerStates.NOT_TRIGGERED) {
        newState = TriggerStates.TRIGGERED
        eventType = TriggerEventTypes.RISING
        changeState(newValue, newState)
        change = true
      }
    }

    if (change) {
      listeners.foreach {
        _.onTrigger(this, newState, eventType)
      }
    }

  }

  /**
   * Get the current value of the trigger.
   *
   * @return the current value of the trigger
   */
  def getValue(): Double = {
    value
  }

  override def getState(): TriggerStates.TriggerState = {
    synchronized {
      state
    }
  }

  override def reset(): Unit = {
    val lastState = state
    changeState(0, TriggerStates.NOT_TRIGGERED)

    if (!lastState.equals(TriggerStates.NOT_TRIGGERED)) {
      listeners.foreach {
        _.onTrigger(this, TriggerStates.NOT_TRIGGERED, TriggerEventTypes.FALLING)
      }
    }
  }

  /**
   * Get the previous state of the trigger.
   *
   * @return the previous state of the trigger
   */
  def getPreviousState(): TriggerStates.TriggerState = {
    synchronized {
      previousState
    }
  }

  /**
   * Change the state of the trigger in a thread safe way.
   *
   * @param newValue
   *          the new value for the trigger
   * @param newState
   *          the new state of the trigger
   */
  private def changeState(newValue: Double, newState: TriggerStates.TriggerState): Unit = {
    synchronized {
      value = newValue;
      previousState = state;
      state = newState;
    }
  }

  override def toString(): String = {
    "SimpleHysteresisThresholdValueTrigger [value=" + value + ", state=" + state +
      ", previousState=" + previousState + ", thresholdOn=" + thresholdOn +
      ", thresholdOff=" + thresholdOff + "]";
  }
}
