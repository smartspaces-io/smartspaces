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

package io.smartspaces.service.action

import io.smartspaces.SimpleSmartSpacesException

/**
 * An action source with a map of actions.
 *
 * @author Keith M. Hughes
 */
abstract class MappedActionSource extends ActionSource {

  /**
   * The map of actions.
   */
  val actionMap: Map[String, Action]

  override def getAction(actionName: String): Action = {
    val action = actionMap.get(actionName)

    if (action.isDefined) {
      action.get
    } else {
      throw new SimpleSmartSpacesException(s"Unknown action ${actionName}")
    }

  }
}