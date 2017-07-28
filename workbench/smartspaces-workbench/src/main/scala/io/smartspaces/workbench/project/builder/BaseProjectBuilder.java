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

package io.smartspaces.workbench.project.builder;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.activity.ActivityProject;

import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A base builder class for common project types.
 *
 * @param <T>
 *          the type of the project
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public abstract class BaseProjectBuilder implements ProjectBuilder {

  /**
   * Template data key entry for the list of resource sources.
   */
  public static final String TEMPLATE_SRC_LIST_KEY = "srclist";

  /**
   * Template destination entry key for resource entries.
   */
  public static final String TEMPLATE_ENTRY_DST_KEY = "dst";

  /**
   * Template source entry key for resource entries.
   */
  public static final String TEMPLATE_ENTRY_SRC_KEY = "src";

  /**
   * Resource map template.
   */
  public static final String ACTIVITY_RESOURCE_MAP_TEMPLATE = "activity/resource.map.ftl";

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Build has begun. Do any specific parts of the build.
   *
   * @param project
   *          the project
   * @param context
   *          the build context
   * @param stagingDirectory
   *          the staging directory where build artifacts go
   *
   * @throws SmartSpacesException
   *           the build failed
   */
  public void onBuild(Project project, ProjectTaskContext context, File stagingDirectory)
      throws SmartSpacesException {
    // Default is nothing
  }

  /**
   * Get the build destination file.
   *
   * @param project
   *          the project being built
   * @param buildDirectory
   *          where the artifact will be built
   * @param extension
   *          filename extension for generating destination
   *
   * @return the file where the build should be written
   */
  protected File getBuildDestinationFile(Project project, File buildDirectory, String extension) {
    return fileSupport.newFile(buildDirectory, getProjectArtifactFilename(project, extension));
  }

  /**
   * Get the name of the project artifact.
   *
   * @param project
   *          the project
   * @param extension
   *          the extension to be used for the artifact
   *
   * @return the filename for the project artifact
   */
  private String getProjectArtifactFilename(Project project, String extension) {
    return project.getIdentifyingName() + "-" + project.getVersion() + "." + extension;
  }

  /**
   * Write out the resource source map contained in the project build context.
   *
   * @param project
   *          current build project
   * @param stagingDirectory
   *          destination directory for build output
   * @param context
   *          project context containing the resource source map
   */
  protected void
      writeResourceMap(Project project, File stagingDirectory, ProjectTaskContext context) {
    File resourceMapFile =
        fileSupport.newFile(context.getRootBuildDirectory(), ActivityProject.FILENAME_RESOURCE_MAP);

    Map<String, Object> templateData = new HashMap<>();
    List<Map<String, String>> srcList = new ArrayList<>();
    templateData.put(TEMPLATE_SRC_LIST_KEY, srcList);

    String stagingPrefix = stagingDirectory.getAbsolutePath() + File.separatorChar;

    for (Map.Entry<File, File> sourceEntry : context.getResourceFileCollector().entrySet()) {
      Map<String, String> stringMap = Maps.newHashMapWithExpectedSize(1);
      String destPath = sourceEntry.getKey().getAbsolutePath();
      if (destPath.startsWith(stagingPrefix)) {
        destPath = destPath.substring(stagingPrefix.length());
      }
      stringMap.put(TEMPLATE_ENTRY_DST_KEY, destPath);
      stringMap.put(TEMPLATE_ENTRY_SRC_KEY, sourceEntry.getValue().getAbsolutePath());
      srcList.add(stringMap);
    }
    context.getWorkbenchTaskContext().getWorkbench().getTemplater()
        .writeTemplate(templateData, resourceMapFile, ACTIVITY_RESOURCE_MAP_TEMPLATE);
  }
}
