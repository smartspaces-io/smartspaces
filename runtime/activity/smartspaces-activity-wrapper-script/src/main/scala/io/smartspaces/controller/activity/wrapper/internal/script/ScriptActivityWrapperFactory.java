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

package io.smartspaces.controller.activity.wrapper.internal.script;

import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.ActivityRuntime;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import io.smartspaces.liveactivity.runtime.activity.wrapper.ActivityWrapperFactory;
import io.smartspaces.liveactivity.runtime.activity.wrapper.BaseActivityWrapperFactory;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.service.script.ScriptService;

/**
 * An {@link ActivityWrapperFactory} for scripted activities.
 *
 * @author Keith M. Hughes
 */
public class ScriptActivityWrapperFactory extends BaseActivityWrapperFactory {

  /**
   * The name of the activity type.
   */
  public static final String ACTIVITY_TYPE_NAME = "script";

  /**
   * Configuration property giving the scripting language.
   */
  public static final String CONFIGURATION_NAME_ACTIVITY_SCRIPT_LANGUAGE =
      "space.activity.script.language";

  /**
   * The script engine to be used by this factory.
   */
  private ScriptService scriptService;

  /**
   * Construct the factory.
   *
   * @param scriptService
   *          the script service
   */
  public ScriptActivityWrapperFactory(ScriptService scriptService) {
    this.scriptService = scriptService;
  }

  @Override
  public String getActivityType() {
    return ACTIVITY_TYPE_NAME;
  }

  @Override
  public ActivityWrapper newActivityWrapper(InstalledLiveActivity liapp,
      ActivityFilesystem activityFilesystem, Configuration configuration,
      ActivityRuntime activityRuntime) {

    return new ScriptActivityWrapper(
        getActivityExecutable(liapp, activityFilesystem, configuration), scriptService,
        activityFilesystem, configuration);
  }
}
