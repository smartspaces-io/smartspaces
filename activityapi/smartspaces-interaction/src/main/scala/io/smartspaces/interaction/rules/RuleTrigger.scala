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
 * A trigger for a rule.
 * 
 * @author Keith M. Hughes
 */
trait RuleTrigger {
  
  /**
   * Get the name of the trigger.
   */
  def triggerName: String
  
  /**
   * Get the rule for the trigger.
   */
  def rule: Rule
  
  /**
   * Do any initialization required by the trigger.
   */
  def initialize(): Unit
}

/**
 * A base rule trigger that provides some useful definitions for triggers.
 * 
 * @author Keith M. Hughes
 */
abstract class BaseRuleTrigger extends RuleTrigger {
  
  override def initialize(): Unit = {
    // Do nothing
  }
}
