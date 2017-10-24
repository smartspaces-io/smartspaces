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

package io.smartspaces.activity.behavior.web

import io.smartspaces.activity.ActivityBehavior
import io.smartspaces.activity.behavior.general.JsonActivityBehavior
import io.smartspaces.activity.component.web.WebServerActivityComponent
import io.smartspaces.service.web.server.WebServer

import java.io.File
import java.util.Map
import io.smartspaces.service.web.server.CompleteWebServerBehavior

/**
 * Web server behavior for an activity.
 *
 * @author Keith M. Hughes
 */
trait WebServerActivityBehavior extends ActivityBehavior with CompleteWebServerBehavior with JsonActivityBehavior {

  /**
   * Get the web server activity component for the activity.
   *
   * @return the web server activity component
   */
  def getWebServerComponent(): WebServerActivityComponent
}