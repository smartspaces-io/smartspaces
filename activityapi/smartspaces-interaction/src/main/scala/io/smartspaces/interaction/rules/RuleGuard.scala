/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.interaction.rules

import io.smartspaces.evaluation.ExecutionContext

/**
 * A guard for a rule.
 * 
 * All guards must allow for a rule's action to trigger.
 * 
 * @author Keith M. Hughes
 */
trait RuleGuard {
  
  /**
   * Evaluate the conditional in the context of a rule.
   * 
   * @param rule
   *        the rule this is being evaluated under
   * @param executionContext
   *        the execution context for the conditional
   *        
   * @return [[true]] if the conditional passes
   */
  def evaluate(rule: Rule, executionContext: ExecutionContext): Boolean
  
  /**
   * Do any initialization required by the guard.
   */
  def initialize(): Unit
}

/**
 * A base rule guard that provides some useful definitions for guards.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseRuleGuard extends RuleGuard {
  
  override def initialize(): Unit = {
    // Do nothing
  }
}
