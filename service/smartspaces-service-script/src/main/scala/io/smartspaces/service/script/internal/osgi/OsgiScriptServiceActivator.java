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

package io.smartspaces.service.script.internal.osgi;

import io.smartspaces.service.script.ScriptService;
import io.smartspaces.service.script.internal.JavaxScriptScriptService;
import io.smartspaces.system.osgi.SmartSpacesOsgiBundleActivator;

/**
 * An OSGI bundle activator for the script service.
 *
 * @author Keith M. Hughes
 */
public class OsgiScriptServiceActivator extends SmartSpacesOsgiBundleActivator {

  @Override
  protected void allRequiredServicesAvailable() {
    JavaxScriptScriptService scriptService = new JavaxScriptScriptService();

    registerNewSmartSpacesService(scriptService);

    registerOsgiService(ScriptService.class.getName(), scriptService);
  }
}
