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

import io.smartspaces.evaluation.FunctionCall;

/**
 * A function that requires all of its arguments to be false.
 * 
 * @author Keith M. Hughes
 */
public class NoneOfFunctionDefinition extends BaseTruthFunctionDefinition {

  /**
   * The name of the function.
   */
  public static final String FUNCTION_NAME = "noneOf";

  /**
   * Construct a new definition.
   */
  public NoneOfFunctionDefinition() {
    super(true, true);
  }
  
  @Override
  public String functionName() {
    return FUNCTION_NAME;
  }

  @Override
  public boolean canEvaluateCall(FunctionCall functionCall) {
    return FUNCTION_NAME.equals(functionCall.functionName());
  }
}
