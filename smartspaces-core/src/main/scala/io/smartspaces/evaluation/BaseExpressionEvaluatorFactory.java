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

import io.smartspaces.evaluation.function.AllOfFunctionDefinition;
import io.smartspaces.evaluation.function.ConcatStringFunctionDefinition;
import io.smartspaces.evaluation.function.CondFunctionDefinition;
import io.smartspaces.evaluation.function.EqualsFunctionDefinition;
import io.smartspaces.evaluation.function.NoneOfFunctionDefinition;
import io.smartspaces.evaluation.function.ReplaceAllStringFunctionDefinition;
import io.smartspaces.evaluation.function.SomeOfFunctionDefinition;

/**
 * The base class for implementing expression evaluator factories.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseExpressionEvaluatorFactory implements ExpressionEvaluatorFactory {

  /**
   * Create a new evaluation environment.
   * 
   * @return a new evaluation environment
   */
  protected EvaluationEnvironment newEvaluationEnvironment() {
    SimpleEvaluationEnvironment environment = new SimpleEvaluationEnvironment();
    
    environment.addFunctionDefinition(new ConcatStringFunctionDefinition());
    environment.addFunctionDefinition(new ReplaceAllStringFunctionDefinition());
    environment.addFunctionDefinition(new AllOfFunctionDefinition());
    environment.addFunctionDefinition(new SomeOfFunctionDefinition());
    environment.addFunctionDefinition(new NoneOfFunctionDefinition());
    environment.addFunctionDefinition(new CondFunctionDefinition());
    environment.addFunctionDefinition(new EqualsFunctionDefinition());
    
    return environment;
  }
}
