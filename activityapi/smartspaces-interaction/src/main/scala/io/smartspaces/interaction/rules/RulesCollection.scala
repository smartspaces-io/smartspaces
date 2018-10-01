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

/**
 * A collection of rules.
 * 
 * @author Keith M. Hughes
 */
trait RulesCollection {
  
  /**
   * Add a rule to the collection.
   * 
   * @param rule
   *        the rule to add
   */
  def addRule(rule: Rule): RulesCollection
  
  /**
   * Get a rule from the collection.
   * 
   * @param ruleName
   *        name of the rule
   *        
   * @return the rule, if found
   */
  def getRule(ruleName: String): Option[Rule]
}

/**
 * A collection of rules.
 * 
 * @author Keith M. Hughes
 */
class StandardRulesCollection extends RulesCollection {
  
  /**
   * The rules indexed by name
   */
  private var rules = Map[String, Rule]()
  
  override def addRule(rule: Rule): RulesCollection = {
    rules = rules + (rule.ruleName -> rule)
    
    this
  }
  
  override def getRule(ruleName: String): Option[Rule] = {
    rules.get(ruleName)
  }
}
