/*
 * Copyright (C) 2016 Keith M. Hughes
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Test the {@link SimpleHysteresisThresholdValueTrigger} class.
 * 
 * @author Keith M. Hughes
 */
public class SimpleHysteresisThresholdValueTriggerWithDataTest {

  private SimpleHysteresisThresholdValueTriggerWithData<Integer> trigger;

  private TriggerWithDataListener<Integer> triggerListener;

  @Before
  public void setup() {
    trigger = new SimpleHysteresisThresholdValueTriggerWithData<Integer>(0);

    triggerListener = Mockito.mock(TriggerWithDataListener.class);
    trigger.addListener(triggerListener);
  }

  /**
   * Check no trigger on updates below the on threshold.
   */
  @Test
  public void testNoTrigger() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);

    trigger.update(9, 1);
    trigger.update(8,1);
    trigger.update(7,1);
    Mockito.verify(triggerListener, Mockito.times(0)).onTrigger(Mockito.eq(trigger),
        Mockito.any(TriggerState.class), Mockito.any(TriggerEventType.class));
  }

  /**
   * Check for a trigger above threshold.
   */
  @Test
  public void testTriggerAboveThreshold() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);

    trigger.update(11, 1);
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.TRIGGERED, TriggerEventType.RISING);
  }

  /**
   * Check for a trigger at threshold.
   */
  @Test
  public void testTriggerAtThreshold() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);

    trigger.update(10, 1);
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.TRIGGERED, TriggerEventType.RISING);
  }

  /**
   * Check for multiple above threshold, but only 1 trigger.
   */
  @Test
  public void testTriggerMultipleTrigger() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);

    trigger.update(11, 1);
    trigger.update(10, 1);
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.TRIGGERED, TriggerEventType.RISING);
  }

  /**
   * Check for trigger then trigger off.
   */
  @Test
  public void testTriggerOnThenOff() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);
    
    InOrder inorder = Mockito.inOrder(triggerListener);

    trigger.update(11, 1);
    trigger.update(8, 1);
    
    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.TRIGGERED, TriggerEventType.RISING);
    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.NOT_TRIGGERED, TriggerEventType.FALLING);
  }

  /**
   * Check for trigger then no trigger off because not below lower threshold.
   */
  @Test
  public void testTriggerOnThenNotOff() {
    trigger.setThresholdOn(10);
    trigger.setThresholdOff(8);
    
    InOrder inorder = Mockito.inOrder(triggerListener);

    trigger.update(11, 1);
    trigger.update(9, 2);
    
    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(trigger,
        TriggerState.TRIGGERED, TriggerEventType.RISING);
    inorder.verify(triggerListener, Mockito.times(0)).onTrigger(trigger,
        TriggerState.NOT_TRIGGERED, TriggerEventType.FALLING);
  }
}
