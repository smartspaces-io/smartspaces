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

import io.smartspaces.activity.component.web.WebBrowserActivityComponent

/**
 * An activity behavior for web browser support.
 *
 * <p>
 * This behavior uses the registered {@link WebServerActivityComponent.COMPONENT_NAME} activity component.
 *
 * @author Keith M. Hughes
 */
trait StandardActivityWebBrowser extends WebBrowserActivityBehavior {
  
  abstract override def commonActivitySetup(): Unit = {
    super.commonActivitySetup()

    addActivityComponent(WebBrowserActivityComponent.COMPONENT_NAME)
  }

}