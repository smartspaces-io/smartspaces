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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A sequence element that groups a collection of sequence elements together.
 *
 * @author Keith M. Hughes
 */
public class GroupSequenceElement implements SequenceElement {

  /**
   * The sequence of ActionElements to execute in order.
   */
  private List<SequenceElement> elements;

  /**
   * Create a SequenceActionElement that runs a given List<ActionElement> in
   * order.
   *
   * @param elements
   *          the elements to place in the sequence
   */
  public GroupSequenceElement(List<SequenceElement> elements) {
    this.elements = elements;
  }

  /**
   * Create a SequenceActionElement that runs a given sequence of ActionElements
   * in order.
   *
   * @param elements
   *          the elements to be executed in order
   */
  public GroupSequenceElement(SequenceElement... elements) {
    this(Lists.newArrayList(elements));
  }

  @Override
  public void run(SequenceExecutionContext sequenceExecutionContext) {
    for (SequenceElement element : elements) {
      if (Thread.interrupted()) {
        break;
      }

      element.run(sequenceExecutionContext);
    }
  }
}
