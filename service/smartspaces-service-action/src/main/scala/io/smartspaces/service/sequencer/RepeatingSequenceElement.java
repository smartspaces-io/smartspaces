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

/**
 * A sequence element that will repeat a sequence element a specified number of
 * times.
 *
 * @author Keith M. Hughes
 */
public class RepeatingSequenceElement implements SequenceElement {

  /**
   * The number of times to repeat.
   */
  private final int repeatCount;

  /**
   * The element to be repeated.
   */
  private final SequenceElement element;

  /**
   * Construct a new sequence element.
   *
   * @param repeatCount
   *          the number of times to do the element
   * @param element
   *          the element to be repeated
   *
   */
  public RepeatingSequenceElement(int repeatCount, SequenceElement element) {
    this.repeatCount = repeatCount;
    this.element = element;
  }

  @Override
  public void run(SequenceExecutionContext sequenceExecutionContext) {
    for (int i = 0; i < repeatCount; i++) {
      if (Thread.interrupted()) {
        break;
      }

      element.run(sequenceExecutionContext);
    }
  }
}
