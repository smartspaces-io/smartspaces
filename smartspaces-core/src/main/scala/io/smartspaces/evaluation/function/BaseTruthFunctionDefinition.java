/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.evaluation.function;

import io.smartspaces.evaluation.EvaluationSmartSpacesException;
import io.smartspaces.evaluation.ExpressionEvaluatorUtilities;
import io.smartspaces.evaluation.FunctionCall;
import io.smartspaces.evaluation.FunctionDefinition;

/**
 * A base class for functions that evaluate to a boolean and have a shotcircuit value.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseTruthFunctionDefinition implements FunctionDefinition {

  /**
   * The value that will be tested against for a short circuit out.
   */
  private boolean shortcircuitTest;

  /**
   * The value that will be given if all arguments aren't the short circuit value.
   */
  private boolean allTheWayThroughValue;

  /**
   * Construct a new definition.
   * 
   * @param shortcircuitTest
   *          the value that will be tested against for a shirt circuit out
   * @param allTheWayThroughValue
   *          the value that will be given if all arguments aren't the short
   *          circuit value
   */
  public BaseTruthFunctionDefinition(boolean shortcircuitTest, boolean allTheWayThroughValue) {
    this.shortcircuitTest = shortcircuitTest;
    this.allTheWayThroughValue = allTheWayThroughValue;
  }

  @Override
  public Object evaluateFunctionCall(FunctionCall functionCall)
      throws EvaluationSmartSpacesException {
    
    for (Object arg : functionCall.args()) {
      if (ExpressionEvaluatorUtilities.isTrue(arg) == shortcircuitTest) {
        return !allTheWayThroughValue;
      }
    }
    
    return allTheWayThroughValue;
  }

}
