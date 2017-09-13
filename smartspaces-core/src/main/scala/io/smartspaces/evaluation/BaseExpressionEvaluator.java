/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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
 * An {@link ExpressionEvaluator} that does simple evaluations of strings.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseExpressionEvaluator implements ExpressionEvaluator {

  /**
   * The environment for evaluating the expressions in.
   */
  protected EvaluationEnvironment environment;

  @Override
  public void setEvaluationEnvironment(EvaluationEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public EvaluationEnvironment getEvaluationEnvironment() {
    return environment;
  }
}
