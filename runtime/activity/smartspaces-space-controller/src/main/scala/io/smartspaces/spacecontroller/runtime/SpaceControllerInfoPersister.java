/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.spacecontroller.runtime;

import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.system.SmartSpacesEnvironment;

/**
 * A persister for space controller info.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerInfoPersister {

  /**
   * Persist the information for the controller.
   *
   * @param controllerInfo
   *          the information about the controller
   *
   * @param spaceEnvironment
   *          the space environment the controller is running under
   */
  void persist(SpaceController controllerInfo, SmartSpacesEnvironment spaceEnvironment);
}
