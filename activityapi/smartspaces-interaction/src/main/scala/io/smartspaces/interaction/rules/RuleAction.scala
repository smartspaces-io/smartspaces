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
 * An action to take place when a rule passes all conditionals.
 * 
 * @author Keith M. Hughes
 */
trait RuleAction {
  
  /**
   * Evaluate the action in the context of a rule.
   * 
   * @param rule
   *        the rule this is being evaluated under
   * @param trigger
   *        the trigger that cased the rule to take action
   * @param executionContext
   *        the execution context for the conditional
   */
  def evaluate(rule: Rule, trigger: RuleTrigger, executionContext: ExecutionContext): Unit
  
  /**
   * Do any initialization required by the guard.
   */
  def initialize(): Unit
}

/**
 * A base rule action that provides some useful definitions for actions.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseRuleAction extends RuleAction{
  
  override def initialize(): Unit = {
    // Do nothing
  }
}

/**
 * A rule action that will evaluate all child actions.
 * 
 * @author Keith M. Hughes
 */
class CompositeRuleAction(actions: Iterable[RuleAction]) extends RuleAction {
  
  override def evaluate(rule: Rule, trigger: RuleTrigger, executionContext: ExecutionContext): Unit = {
    actions.foreach(_.evaluate(rule, trigger, executionContext))
  }
  
  override def initialize(): Unit = {
    actions.foreach(_.initialize())
  }
}
