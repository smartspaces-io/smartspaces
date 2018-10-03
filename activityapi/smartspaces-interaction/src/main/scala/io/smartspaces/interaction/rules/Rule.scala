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
 * A rule.
 *
 * A rule has a collection of triggers that can initiate an evaluation of the rule. Once the rule
 * is triggers, a set of guards will be evaluated. If all guards evaluate to true,
 * the rule's actions will be evaluated.
 *
 * @author Keith M. Hughes
 */
trait Rule {

  /**
   * The name of the rule.
   */
  def ruleName: String

  /**
   * Get all rule triggers.
   *
   * @return all rule triggers
   */
  def ruleTriggers: Iterable[RuleTrigger]

  /**
   * Get a rule trigger by name.
   *
   * @return the trigger, if found
   */
  def getRuleTrigger(triggerName: String): Option[RuleTrigger]

  /**
   * Add in a new rule trigger.
   *
   * @param ruleTrigger
   *        the rule trigger to add
   *
   * @return this rule
   */
  def addRuleTrigger(ruleTrigger: RuleTrigger): Rule

  /**
   * Remove a rule trigger.
   *
   * Does nothing if the trigger isn't part of the rule
   *
   * @param ruleTrigger
   *        the rule trigger to add
   *
   * @return this rule
   */
  def removeRuleTrigger(ruleTrigger: RuleTrigger): Rule

  /**
   * Get all rule guards.
   *
   * @return all rule guards
   */
  def ruleGuards: Iterable[RuleGuard]

  /**
   * Add in a new rule guard.
   *
   * @param ruleGuard
   *        the rule guard to add
   *
   * @return this rule
   */
  def addRuleGuard(ruleGuard: RuleGuard): Rule

  /**
   * Remove a rule guard.
   *
   * Does nothing if the guard isn't part of the rule
   *
   * @param ruleGuard
   *        the rule guard to add
   *
   * @return this rule
   */
  def removeRuleGuard(ruleGuard: RuleGuard): Rule

  /**
   * Get all rule actions.
   *
   * @return all rule actions
   */
  def ruleActions: Iterable[RuleAction]

  /**
   * Add in a new rule action.
   *
   * @param ruleAction
   *        the rule action to add
   *
   * @return this rule
   */
  def addRuleAction(ruleAction: RuleAction): Rule

  /**
   * Remove a rule action.
   *
   * Does nothing if the action isn't part of the rule
   *
   * @param ruleAction
   *        the rule action to add
   *
   * @return this rule
   */
  def removeRuleAction(ruleAction: RuleAction): Rule

  /**
   * The root execution context.
   *
   * This context remains stable for the entire lifetime of the rule. Evaluations
   * of rule guards will be given an execution context with this as a parent.
   */
  def rootExecutionContext: ExecutionContext

  /**
   * The rule has been triggered by the supplied trigger.
   *
   * @param trigger
   *        the trigger initiating the rule
   * @param ruleInvocationInitialize
   *        the initializer for the rule invocation, argument is the pushed context
   */
  def triggered(trigger: RuleTrigger, ruleInvocationInitialize: (ExecutionContext) => Unit): Unit
}

/**
 * Create a standard rule.
 *
 * @author Keith M. Hughes
 */
class StandardRule(
  override val ruleName: String,
  override val rootExecutionContext: ExecutionContext) extends Rule {

  /**
   * The triggers for the rule.
   */
  private var _ruleTriggers = Map[String, RuleTrigger]()

  /**
   * The guards for the rule.
   */
  private var _ruleGuards = List[RuleGuard]()

  /**
   * The actions for the rule.
   */
  private var _ruleActions = List[RuleAction]()

  override def ruleTriggers: Iterable[RuleTrigger] = {
    _ruleTriggers.values
  }

  override def getRuleTrigger(triggerName: String): Option[RuleTrigger] = {
    _ruleTriggers.get(triggerName)
  }

  override def addRuleTrigger(ruleTrigger: RuleTrigger): Rule = {
    ruleTrigger.initialize()

    _ruleTriggers = _ruleTriggers + (ruleTrigger.triggerName -> ruleTrigger)

    this
  }

  override def removeRuleTrigger(ruleTrigger: RuleTrigger): Rule = {
    _ruleTriggers = _ruleTriggers - ruleTrigger.triggerName

    this
  }

  override def ruleGuards: Iterable[RuleGuard] = {
    _ruleGuards
  }

  override def addRuleGuard(ruleGuard: RuleGuard): Rule = {
    ruleGuard.initialize()

    _ruleGuards = ruleGuard :: _ruleGuards

    this
  }

  override def removeRuleGuard(ruleGuard: RuleGuard): Rule = {
    _ruleGuards = _ruleGuards.filter(_ != ruleGuard)

    this
  }

  override def ruleActions: Iterable[RuleAction] = {
    _ruleActions
  }

  override def addRuleAction(ruleAction: RuleAction): Rule = {
    ruleAction.initialize()

    _ruleActions = ruleAction :: _ruleActions

    this
  }

  override def removeRuleAction(ruleAction: RuleAction): Rule = {
    _ruleActions = _ruleActions.filter(_ != ruleAction)

    this
  }

  override def triggered(trigger: RuleTrigger, ruleInvocationInitialize: (ExecutionContext) => Unit): Unit = {
    val evaluationContext = rootExecutionContext.push()

    ruleInvocationInitialize(evaluationContext)

    if (_ruleGuards.forall(_.evaluate(this, evaluationContext))) {
      _ruleActions.foreach(_.evaluate(this, trigger, evaluationContext))
    }
  }
}
