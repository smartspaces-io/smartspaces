/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.service.sequencer;

import io.smartspaces.time.TimeDelay;

import java.util.List;

/**
 * A collection of methods for creating sequence elements of various types.
 *
 * @author Keith M. Hughes
 */
public class SequenceElements {

  /**
   * Create an ActionElement for the given Runnable.
   *
   * @param action
   *          a Runnable to be executed
   *
   * @return a sequence element that will run the runnable
   */
  public static SequenceElement runnable(Runnable action) {
    return new RunnableSequenceElement(action);
  }

  /**
   * Create an ActionElement that delays for the given number of seconds.
   *
   * @param duration
   *          amount of time to delay
   *
   * @return a sequence element that will delay
   */
  public static SequenceElement delay(TimeDelay duration) {
    return new DelaySequenceElement(duration);
  }

  /**
   * Create a sequence element that will repeat a collection of elements a given
   * number of times.
   *
   * <p>
   * The elements will be performed in the order given.
   *
   * @param count
   *          number of times the elements should be repeated
   * @param elements
   *          the elements to be repeated
   *
   * @return an ActionElement that loops the given element count times
   */
  public static SequenceElement repeat(int count, SequenceElement... elements) {
    return new RepeatingSequenceElement(count, new GroupSequenceElement(elements));
  }

  /**
   * Create an ActionElement that runs the given sequence of ActionElements in
   * order.
   *
   * @param elements
   *          a sequence of elements to be treated as a group
   *
   * @return the group as a single element
   */
  public static SequenceElement group(SequenceElement... elements) {
    return new GroupSequenceElement(elements);
  }

  /**
   * Create an ActionElement that runs the given sequence of ActionElements in
   * order.
   *
   * @param sequence
   *          a sequence of elements to be treated as a group
   *
   * @return the group as a single element
   */
  public static SequenceElement group(List<SequenceElement> sequence) {
    return new GroupSequenceElement(sequence);
  }
}
