/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime.configuration;

import io.smartspaces.evaluation.ExpressionEvaluatorFactory;
import io.smartspaces.liveactivity.runtime.InternalLiveActivityFilesystem;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.io.File;

/**
 * The live activity configuration manager for a production live activity
 * runtime.
 *
 * @author Keith M. Hughes
 */
public class ProductionPropertyFileLiveActivityConfigurationManager extends
    BasePropertyFileLiveActivityConfigurationManager {

  /**
   * The activity type for the live activity.
   */
  public static final String CONFIG_TYPE_LIVE_ACTIVITY = "activity";

  /**
   * Construct a new configuration manager.
   *
   * @param expressionEvaluatorFactory
   *          the expression evaluator factory to use
   * @param spaceEnvironment
   *          the space environment to use
   */
  public ProductionPropertyFileLiveActivityConfigurationManager(
      ExpressionEvaluatorFactory expressionEvaluatorFactory, SmartSpacesEnvironment spaceEnvironment) {
    super(expressionEvaluatorFactory, spaceEnvironment);
  }

  @Override
  protected File getBaseActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem) {
    return activityFilesystem.getInstallFile(getConfigFileName(CONFIG_TYPE_BASE_ACTIVITY));
  }

  @Override
  protected File getInstalledActivityConfiguration(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem) {
    return activityFilesystem.getInternalFile(getConfigFileName(CONFIG_TYPE_LIVE_ACTIVITY));
  }
}
