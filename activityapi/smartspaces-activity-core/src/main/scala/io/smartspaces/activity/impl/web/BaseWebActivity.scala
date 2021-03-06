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

package io.smartspaces.activity.impl.web;

import io.smartspaces.activity.Activity;
import io.smartspaces.activity.component.web.WebBrowserActivityComponent;
import io.smartspaces.activity.behavior.web.StandardActivityWebServer
import io.smartspaces.activity.impl.BaseActivity
import io.smartspaces.activity.behavior.web.StandardActivityWebBrowser

/**
 * An {@link Activity} which starts up a web server with websocket handler and a
 * web browser. The activity can then easily respond to web socket messages.
 *
 * <p>
 * Only 1 web socket client is currently supported.
 *
 * <p>
 * This activity is easily scriptable.
 *
 * @author Keith M. Hughes
 */
class BaseWebActivity extends BaseActivity with StandardActivityWebServer with StandardActivityWebBrowser {
}
