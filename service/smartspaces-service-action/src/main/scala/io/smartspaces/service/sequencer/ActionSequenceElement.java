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

package io.smartspaces.service.sequencer;

import io.smartspaces.service.action.ActionReference;
import io.smartspaces.service.action.ActionService;

/**
 * A sequence element that performs an action.
 *
 * @author Keith M. Hughes
 */
public class ActionSequenceElement implements SequenceElement {

  /**
   * The action reference this element will execute.
   */
  private final ActionReference actionReference;

  /**
   * Construct a sequence element.
   * 
   * @param actionReference
   *          the action reference the element will run
   */
  public ActionSequenceElement(ActionReference actionReference) {
    this.actionReference = actionReference;
  }

  @Override
  public void run(SequenceExecutionContext sequenceExecutionContext) {
    ActionService actionService = sequenceExecutionContext.getSpaceEnvironment().getServiceRegistry()
        .getRequiredService(ActionService.SERVICE_NAME);
    
    actionService.performActionReference(actionReference, sequenceExecutionContext);
  }
}
