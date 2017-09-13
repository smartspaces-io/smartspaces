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

package io.smartspaces.evaluation;

/**
 * The definition of a function.
 * 
 * @author Keith M. Hughes
 */
public interface FunctionDefinition {
  
  /**
   * Get the name of the function.
   * 
   * @return the name of the function
   */
  String functionName();
  
  /**
   * Can this function evaluate this call?
   * 
   * @param functionCall
   *       the function call
   *       
   * @return {@code true} if can evaluate the call
   */
  boolean canEvaluateCall(FunctionCall functionCall);

  /**
   * Evaluate a function call.
   * 
   * @param functionCall
   *          the function call to evaluate
   *          
   * @return the value of the function call
   * 
   * @throws EvaluationSmartSpacesException
   *          something bad happened during evaluation
   */
  Object evaluateFunctionCall(FunctionCall functionCall) throws EvaluationSmartSpacesException;
}
