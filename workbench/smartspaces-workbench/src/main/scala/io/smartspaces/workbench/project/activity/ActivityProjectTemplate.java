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

package io.smartspaces.workbench.project.activity;

import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.BaseProjectTemplate;
import io.smartspaces.workbench.project.creator.ProjectCreationContext;
import io.smartspaces.workbench.project.java.JvmProjectType;

import java.io.File;

/**
 * A base implementation of a project template for activities.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectTemplate extends BaseProjectTemplate {

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  protected void onTemplateSetup(ProjectCreationContext context) {
    context.addTemplateDataEntry("activity", context.getProject());
  }

  /**
   * Get the activity source directory.
   *
   * @param context
   *          specification for the build
   *
   * @return the source directory for activity sources
   */
  public File getActivitySourceDirectory(ProjectCreationContext context) {
    return fileSupport.newFile(context.getProject().getBaseDirectory(),
        JvmProjectType.SOURCE_MAIN_JAVA);
  }

  /**
   * Get the activity resource directory.
   *
   * @param context
   *          specification for the build
   *
   * @return the resource directory for activity components
   */
  public File getActivityResourceDirectory(ProjectCreationContext context) {
    return fileSupport.newFile(context.getProject().getBaseDirectory(),
        ActivityProject.SRC_MAIN_RESOURCES_ACTIVITY);
  }
}
