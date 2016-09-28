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

import java.io.File;
import java.util.Map;

import io.smartspaces.activity.component.web.WebServerActivityComponent;
import io.smartspaces.activity.execution.ActivityMethodInvocation;
import io.smartspaces.activity.impl.BaseActivity;
import io.smartspaces.service.web.server.BasicMultipleConnectionWebServerWebSocketHandlerFactory;
import io.smartspaces.service.web.server.HttpFileUpload;
import io.smartspaces.service.web.server.HttpFileUploadListener;
import io.smartspaces.service.web.server.MultipleConnectionWebSocketHandler;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.activity.behavior.web.StandardActivityWebServer

/**
 * An activity which has a web server only.
 *
 * <p>
 * This web server can also handle file uploads.
 *
 * @author Keith M. Hughes
 */
class BaseWebServerActivity extends BaseActivity with StandardActivityWebServer {
}
