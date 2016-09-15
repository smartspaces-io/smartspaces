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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * <p>
 * The trigger events carry extra data. This data will only be updated if the
 * trigger changes state.
 *
 * @author Keith M. Hughes
 */

public class SimpleHysteresisThresholdValueTriggerWithData<D>
    implements ResettableTriggerWithData<D> {

  /**
   * The current value of the trigger.
   */
  private double value;

  /**
   * The current data of the trigger.
   */
  private D data;

  /**
   * The value at which the trigger will switch on.
   */
  private double thresholdOn;

  /**
   * The value at which the trigger will switch off.
   */
  private double thresholdOff;

  /**
   * The current state of the trigger.
   */
  private TriggerState state;

  /**
   * The previous state of the trigger.
   */
  private TriggerState previousState;

  /**
   * Collection of listeners for trigger point events.
   */
  private List<TriggerWithDataListener<D>> listeners = new CopyOnWriteArrayList<>();

  /**
   * Construct a new trigger.
   * 
   * @param initialData
   *          the initial data
   */
  public SimpleHysteresisThresholdValueTriggerWithData(D initialData) {
    thresholdOn = 0;
    thresholdOff = 0;
    value = 0;
    state = previousState = TriggerState.NOT_TRIGGERED;
    data = initialData;
  }

  /**
   * Set the threshold at which the trigger will switch on.
   *
   * @param thresholdOn
   *          the threshold at which the trigger will switch on
   * 
   * @return this trigger
   */
  public SimpleHysteresisThresholdValueTriggerWithData<D> setThresholdOn(double thresholdOn) {
    this.thresholdOn = thresholdOn;

    return this;
  }

  /**
   * Get the threshold at which the trigger will switch on.
   *
   * @return the threshold at which the trigger will switch on
   */
  public double getThresholdOn() {
    return thresholdOn;
  }

  /**
   * Set the value at which the trigger will switch off.
   *
   * @param thresholdOff
   *          the value at which the trigger will switch off
   * 
   * @return this trigger
   */
  public SimpleHysteresisThresholdValueTriggerWithData<D> setThresholdOff(double thresholdOff) {
    this.thresholdOff = thresholdOff;

    return this;
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
  public SimpleHysteresisThresholdValueTriggerWithData<D>
      setThresholdsWithOffset(double thresholdOn, double offset) {
    this.thresholdOn = thresholdOn;
    this.thresholdOff = thresholdOn - offset;

    return this;
  }

  /**
   * Get the threshold at which the trigger will switch off.
   *
   * @return the threshold at which the trigger will switch off
   */
  public double getThresholdOff() {
    return thresholdOn;
  }

  @Override
  public void addListener(TriggerWithDataListener<D> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(TriggerWithDataListener<D> listener) {
    listeners.remove(listener);
  }

  /**
   * Update the value, potentially triggering and notifying listeners.
   *
   * @param newValue
   *          the new value
   * @param newData
   *          the new data
   */
  public void update(double newValue, D newData) {
    TriggerState newState = null;
    TriggerEventType type = null;

    boolean change = false;
    if (newValue <= thresholdOff) {
      if (state == TriggerState.TRIGGERED) {
        newState = TriggerState.NOT_TRIGGERED;
        type = TriggerEventType.FALLING;
        changeState(newValue, newState, newData);
        change = true;
      }
    } else if (newValue >= thresholdOn) {
      if (state == TriggerState.NOT_TRIGGERED) {
        newState = TriggerState.TRIGGERED;
        type = TriggerEventType.RISING;
        changeState(newValue, newState, newData);
        change = true;
      }
    }

    if (change) {
      for (TriggerWithDataListener<D> listener : listeners) {
        listener.onTrigger(this, newState, type);
      }
    }

  }

  @Override
  public D getData() {
    return data;
  }

  /**
   * Get the current value of the trigger.
   *
   * @return the current value of the trigger
   */
  public double getValue() {
    return value;
  }

  @Override
  public synchronized TriggerState getState() {
    return state;
  }

  @Override
  public void reset(D data) {
    TriggerState lastState = state;
    changeState(0, TriggerState.NOT_TRIGGERED, data);

    if (!lastState.equals(TriggerState.NOT_TRIGGERED)) {
      for (TriggerWithDataListener<D> listener : listeners) {
        listener.onTrigger(this, TriggerState.NOT_TRIGGERED, TriggerEventType.FALLING);
      }
    }
  }

  /**
   * Get the previous state of the trigger.
   *
   * @return the previous state of the trigger
   */
  public synchronized TriggerState getPreviousState() {
    return previousState;
  }

  /**
   * Change the state of the trigger in a thread safe way.
   *
   * @param newValue
   *          the new value for the trigger
   * @param newState
   *          the new state of the trigger
   */
  private synchronized void changeState(double newValue, TriggerState newState, D newData) {
    value = newValue;
    previousState = state;
    state = newState;
    data = newData;
  }

  @Override
  public String toString() {
    return "SimpleHysteresisThresholdValueTriggerWithData [value=" + value + ", data=" + data
        + ", state=" + state + ", previousState=" + previousState + ", thresholdOn=" + thresholdOn
        + ", thresholdOff=" + thresholdOff + "]";
  }
}
