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
import io.smartspaces.evaluation.FunctionCall;
import io.smartspaces.evaluation.FunctionDefinition;

import java.util.List;

/**
 * Compare 2 items to see if they are equal to each other.
 * 
 * @author Keith M. Hughes
 */
public class EqualsFunctionDefinition implements FunctionDefinition {

  /**
   * The name of the function.
   */
  public static final String FUNCTION_NAME = "equals";

  @Override
  public String functionName() {
    return FUNCTION_NAME;
  }

  @Override
  public boolean canEvaluateCall(FunctionCall functionCall) {
    return FUNCTION_NAME.equals(functionCall.functionName());
  }

  @Override
  public Object evaluateFunctionCall(FunctionCall functionCall)
      throws EvaluationSmartSpacesException {
    
    List<Object> args = functionCall.args();
    if (args.size() < 2) {
      throw new EvaluationSmartSpacesException("equals function does not have enough arguments");
    }
    
    Object arg1 = args.get(0);
    Object arg2 = args.get(1);
    
    boolean equals = (arg1 != null) ? arg1.equals(arg2) : arg2 == null;
    
    return Boolean.valueOf(equals);
  }
}
