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

import scala.collection.JavaConverters._
import io.smartspaces.util.data.dynamic.DynamicObject.ArrayDynamicObjectEntry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.evaluation.ExecutionContextFactory

object DynamicObjectRuleImporter {

  /**
   * The item giving the name of a component.
   */
  val ITEM_NAME = "name"

  /**
   * The item giving the kind of a component.
   */
  val ITEM_KIND = "kind"

  /**
   * The section head for the entire collection of rules.
   */
  val SECTION_RULES = "rules"

  /**
   * The section head for the entire collection of triggers for a given rule.
   */
  val SECTION_TRIGGERS = "triggers"

  /**
   * The section head for the entire collection of guards for a given rule.
   */
  val SECTION_GUARDS = "guards"

  /**
   * The section head for the entire collection of actions for a given rule.
   */
  val SECTION_ACTIONS = "actions"
}

/**
 * A rule importer that uses Java maps.
 *
 * @author Keith M. Hughes
 */
class DynamicObjectRuleImporter(log: ExtendedLog) extends RuleImporter {

  /**
   * The trigger kind importers indexed by kind.
   */
  private var triggerKindImporters = Map[String, RuleTriggerKindImporter]()

  /**
   * The guard kind importers indexed by kind.
   */
  private var guardKindImporters = Map[String, RuleGuardKindImporter]()

  /**
   * The action kind importers indexed by kind.
   */
  private var actionKindImporters = Map[String, RuleActionKindImporter]()

  override def addRuleTriggerImporter(kindImporter: RuleTriggerKindImporter): RuleImporter = {
    triggerKindImporters = triggerKindImporters + (kindImporter.importerKind -> kindImporter)

    this
  }

  override def addRuleGuardImporter(kindImporter: RuleGuardKindImporter): RuleImporter = {
    guardKindImporters = guardKindImporters + (kindImporter.importerKind -> kindImporter)

    this
  }

  override def addRuleActionImporter(kindImporter: RuleActionKindImporter): RuleImporter = {
    actionKindImporters = actionKindImporters + (kindImporter.importerKind -> kindImporter)

    this
  }

  override def importRules(
    source: DynamicObject,
    executionContextFactory: ExecutionContextFactory): RulesCollection = {
    val rulesCollection = new StandardRulesCollection()

    if (source.downChecked(DynamicObjectRuleImporter.SECTION_RULES)) {

      source.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        rulesCollection.addRule(importRule(itemData, executionContextFactory))
      })

      source.up
    }

    rulesCollection
  }

  /**
   * Import a rule.
   *
   * @param source
   *        the source for the rule at the location of the current rule
   * @param executionContextFactory
   *        the factory for root execution contexts
   */
  private def importRule(
    source: DynamicObject,
    executionContextFactory: ExecutionContextFactory): Rule = {

    val ruleName = source.getRequiredString(DynamicObjectRuleImporter.ITEM_NAME)

    val rule = new StandardRule(ruleName, executionContextFactory.newContext())

    importTriggers(rule, source)
    importGuards(rule, source)
    importActions(rule, source)

    rule
  }

  /**
   * Import all of the triggers for a rule.
   *
   * @param rule
   *        the rule the triggers should be attached to
   * @param source
   *        the source for the trigger definitions
   */
  private def importTriggers(rule: Rule, source: DynamicObject): Unit = {
    if (source.downChecked(DynamicObjectRuleImporter.SECTION_TRIGGERS)) {
      source.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        importTrigger(source).foreach(rule.addRuleTrigger(_))
      })

      source.up
    }
  }

  /**
   * Import an triggers for a rule.
   *
   * @param source
   *        the source for the trigger definition at the position for the definition
   */
  private def importTrigger(source: DynamicObject): Option[RuleTrigger] = {
    val kind = source.getRequiredString(DynamicObjectRuleImporter.ITEM_KIND)
    val kindImporter = triggerKindImporters.get(kind)
    if (kindImporter.isDefined) {
      Some(kindImporter.get.importRuleComponent(source))
    } else {
      log.warn(s"Rule trigger had unknown kind ${kind}")

      None
    }
  }

  /**
   * Import all of the guards for a rule.
   *
   * @param rule
   *        the rule the guards should be attached to
   * @param source
   *        the source for the guard definitions
   */
  private def importGuards(rule: Rule, source: DynamicObject): Unit = {
    if (source.downChecked(DynamicObjectRuleImporter.SECTION_GUARDS)) {

      source.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        importGuard(source).foreach(rule.addRuleGuard(_))
      })

      source.up
    }
  }

  /**
   * Import an guards for a rule.
   *
   * @param source
   *        the source for the guard definition at the position for the definition
   */
  private def importGuard(source: DynamicObject): Option[RuleGuard] = {
    val kind = source.getRequiredString(DynamicObjectRuleImporter.ITEM_KIND)
    val kindImporter = guardKindImporters.get(kind)
    if (kindImporter.isDefined) {
      Some(kindImporter.get.importRuleComponent(source))
    } else {
      log.warn(s"Rule guard had unknown kind ${kind}")

      None
    }
  }

  /**
   * Import all of the actions for a rule.
   *
   * @param rule
   *        the rule the actions should be attached to
   * @param source
   *        the source for the action definitions
   */
  private def importActions(rule: Rule, source: DynamicObject): Unit = {
    if (source.downChecked(DynamicObjectRuleImporter.SECTION_ACTIONS)) {
      source.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        importAction(source).foreach(rule.addRuleAction(_))
      })

      source.up
    }
  }

  /**
   * Import an actions for a rule.
   *
   * @param source
   *        the source for the action definition at the position for the definition
   */
  private def importAction(source: DynamicObject): Option[RuleAction] = {
    val kind = source.getRequiredString(DynamicObjectRuleImporter.ITEM_KIND)
    val kindImporter = actionKindImporters.get(kind)
    if (kindImporter.isDefined) {
      Some(kindImporter.get.importRuleComponent(source))
    } else {
      log.warn(s"Rule action had unknown kind ${kind}")

      None
    }
  }
}
