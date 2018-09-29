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

package io.smartspaces.interaction.event.trigger

import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import org.scalatest.junit.JUnitSuite

/**
 * Test the {@link SimpleHysteresisThresholdValueTrigger} class.
 *
 * @author Keith M. Hughes
 */
class SimpleHysteresisThresholdValueTriggerTest extends JUnitSuite {

  var trigger: SimpleHysteresisThresholdValueTrigger = _

  var triggerListener: TriggerListener = _

  @Before def setup(): Unit = {
    trigger = new SimpleHysteresisThresholdValueTrigger()

    triggerListener = Mockito.mock(classOf[TriggerListener])
    trigger.addListener(triggerListener)
  }

  /**
   * Check no trigger on updates below the on threshold.
   */
  @Test def testNoTrigger(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    trigger.update(9)
    trigger.update(8)
    trigger.update(7)
    Mockito.verify(triggerListener, Mockito.times(0)).onTrigger(
      Matchers.eq(trigger),
      Matchers.any(classOf[TriggerStates.TriggerState]),
      Matchers.any(classOf[TriggerEventTypes.TriggerEventType]))
  }

  /**
   * Check for a trigger above threshold.
   */
  @Test def testTriggerAboveThreshold(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    trigger.update(11)
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.TRIGGERED, TriggerEventTypes.RISING)
  }

  /**
   * Check for a trigger at threshold.
   */
  @Test def testTriggerAtThreshold(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    trigger.update(10)
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.TRIGGERED, TriggerEventTypes.RISING)
  }

  /**
   * Check for multiple above threshold, but only 1 trigger.
   */
  @Test def testTriggerMultipleTrigger(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    trigger.update(11)
    trigger.update(10)
    Mockito.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.TRIGGERED, TriggerEventTypes.RISING)
  }

  /**
   * Check for trigger then trigger off.
   */
  @Test def testTriggerOnThenOff(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    val inorder = Mockito.inOrder(triggerListener)

    trigger.update(11)
    trigger.update(8)

    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.TRIGGERED, TriggerEventTypes.RISING)
    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.NOT_TRIGGERED, TriggerEventTypes.FALLING)
  }

  /**
   * Check for trigger then no trigger off because not below lower threshold.
   */
  @Test def testTriggerOnThenNotOff(): Unit = {
    trigger.setThresholdOn(10)
    trigger.setThresholdOff(8)

    val inorder = Mockito.inOrder(triggerListener)

    trigger.update(11)
    trigger.update(9)

    inorder.verify(triggerListener, Mockito.times(1)).onTrigger(
      trigger,
      TriggerStates.TRIGGERED, TriggerEventTypes.RISING)
    inorder.verify(triggerListener, Mockito.times(0)).onTrigger(
      trigger,
      TriggerStates.NOT_TRIGGERED, TriggerEventTypes.FALLING)
  }
}
