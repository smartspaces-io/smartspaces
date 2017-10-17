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

import java.util.List;

/**
 * A function that evaluates booleans and returns the value associated with the boolean.
 * 
 * <p>
 * The number of arguments must be odd. The even numbered arguments are evaluated starting from argument
 * 0 increasing. The value of the expression immediately following the first argument that evaluates to true
 * is returned. If no boolean expressions are true, then the very last value is returned.
 * 
 * @author Keith M. Hughes
 */
public class CondFunctionDefinition  implements FunctionDefinition {

  /**
   * The name of the function.
   */
  public static final String FUNCTION_NAME = "cond";

  @Override
  public String functionName() {
    return FUNCTION_NAME;
  }

  @Override
  public boolean canEvaluateCall(FunctionCall functionCall) {
    return FUNCTION_NAME.equals(functionCall.functionName()) && functionCall.args().size() >= 3;
  }

  @Override
  public Object evaluateFunctionCall(FunctionCall functionCall)
      throws EvaluationSmartSpacesException {
    
    List<Object> args = functionCall.args();
    int numArgs = args.size();
    if (numArgs % 2 == 0) {
      throw new EvaluationSmartSpacesException("The cond expression does not have a odd number of arguments.");
    }
   
    int lastConditional = numArgs - 3;
    for (int i = 0; i <= lastConditional; i += 2) {
      if (ExpressionEvaluatorUtilities.isTrue(args.get(i))) {
        return args.get(i+1);
      }
    }

    // The final value
    return args.get(numArgs - 1);
  }

}

