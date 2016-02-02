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

package io.smartspaces.service.script.internal.javascript;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.service.script.ActivityScriptWrapper;
import io.smartspaces.service.script.ScriptActivityExecutionContext;
import io.smartspaces.service.script.ScriptSource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * An {@link ActivityScriptWrapper} for Rhino Javascript scripts.
 *
 * @author Keith M. Hughes
 */
public class RhinoActivityScriptWrapper implements ActivityScriptWrapper {

  /**
   * The global scope we'll store the standard JavaScript objects and some of
   * our own global definitions in.
   */
  private Scriptable globalScope;

  /**
   * The script to be run
   */
  private ScriptSource scriptSource;

  /**
   * File system for the activity
   */
  private ActivityFilesystem activityFilesystem;

  /**
   * Configuration for the activity script.
   */
  Configuration configuration;

  public RhinoActivityScriptWrapper(Scriptable globalScope, ScriptSource scriptSource,
      ActivityFilesystem activityFilesystem, Configuration configuration) {
    this.globalScope = globalScope;
    this.scriptSource = scriptSource;
    this.activityFilesystem = activityFilesystem;
    this.configuration = configuration;
  }

  @Override
  public Activity newInstance() {

    String script = scriptSource.getScriptContents();

    int curlyPos = script.indexOf('{');
    String baseClass = script.substring(0, curlyPos);
    String scriptBody = script.substring(curlyPos);

    String buildScript = "new JavaAdapter( Packages." + baseClass + ", " + scriptBody + ")";

    try {
      Context cx = Context.enter();

      Scriptable localScope = cx.newObject(globalScope);
      localScope.setPrototype(globalScope);

      // We want localScope to be a new top-level scope.
      // This means that any variables created by
      // assignments will be properties of localScope.
      localScope.setParentScope(null);

      Object result = cx.evaluateString(localScope, buildScript, "<creation_script>", 0, null);

      NativeJavaObject njo = (NativeJavaObject) result;
      return (Activity) NativeJavaObject.coerceType(Activity.class, result);

    } catch (Exception e) {
      throw new SmartSpacesException(String.format("Could not create activity %s", buildScript), e);
    }
  }

  @Override
  public ActivityExecutionContext newExecutionContext() {
    return new ScriptActivityExecutionContext(RhinoActivityScriptWrapper.class.getClassLoader());
  }
}
