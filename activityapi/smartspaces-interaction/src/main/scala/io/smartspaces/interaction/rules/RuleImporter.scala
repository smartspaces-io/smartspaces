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

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.evaluation.ExecutionContextFactory

/**
 * Import a kind of rule trigger.
 *
 * @author Keith M. Hughes
 */
trait RuleTriggerKindImporter {

  /**
   * The kind of the rules importer
   */
  def importerKind: String

  /**
   * Import a rule trigger.
   * 
   * @param source
   *        the source for the import
   * @param rule
   *        the rule the trigger will be part of
   *        
   * @return the imported trigger
   */
  def importRuleComponent(source: DynamicObject, rule: Rule): RuleTrigger
}

/**
 * Import a kind of rule guard.
 *
 * @author Keith M. Hughes
 */
trait RuleGuardKindImporter {

  /**
   * The kind of the rules importer
   */
  def importerKind: String

  /**
   * Import a rule guard.
   * 
   * @param source
   *        the source for the import
   * @param rule
   *        the rule the guard will be part of
   *        
   * @return the imported guard
   */
  def importRuleComponent(source: DynamicObject, rule: Rule): RuleGuard
}

/**
 * Import a kind of rule action.
 *
 * @author Keith M. Hughes
 */
trait RuleActionKindImporter {

  /**
   * The kind of the rules importer
   */
  def importerKind: String

  /**
   * Import a rule guardaction.
   * 
   * @param source
   *        the source for the import
   * @param rule
   *        the rule the action will be part of
   *        
   * @return the imported action
   */
  def importRuleComponent(source: DynamicObject, rule: Rule): RuleAction
}

/**
 * Import a collection of rules.
 *
 * @author Keith M. Hughes
 */
trait RuleImporter {
  
  /**
   * Add in a new rule trigger importer.
   * 
   * @param kindImporter
   *        the kind importer
   *        
   * @ this collection
   */
  def addRuleTriggerImporter(kindImporter: RuleTriggerKindImporter): RuleImporter
  
  /**
   * Add in a new rule guard importer.
   * 
   * @param kindImporter
   *        the kind importer
   *        
   * @ this collection
   */
  def addRuleGuardImporter(kindImporter: RuleGuardKindImporter): RuleImporter
  
  /**
   * Add in a new rule action importer.
   * 
   * @param kindImporter
   *        the kind importer
   *        
   * @ this collection
   */
  def addRuleActionImporter(kindImporter: RuleActionKindImporter): RuleImporter

  /**
   * Import a set of rules.
   *
   * @param source
   *        the source for the rules
   * @param executionContextFactory
   *        the factory for root execution contexts for rules
   *
   * @return the collection of rules from the source
   */
  def importRules(source: DynamicObject, 
      executionContextFactory: ExecutionContextFactory): RulesCollection
}