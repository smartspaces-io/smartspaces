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

package io.smartspaces.workbench.project;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.SmartSpacesWorkbench;
import io.smartspaces.workbench.project.jdom.JdomProjectReader;
import io.smartspaces.workbench.project.source.SimpleSource;
import io.smartspaces.workbench.project.source.Source;

import java.io.File;

import org.apache.commons.logging.Log;

/**
 * A basic {@link ProjectManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardProjectManager implements ProjectManager {

  /**
   * Name of a project file.
   */
  public static final String FILE_NAME_PROJECT = "project.xml";

  /**
   * Name of an activity file.
   */
  public static final String FILE_NAME_ACTIVITY = "activity.xml";

  /**
   * The workbench being used.
   */
  private final SmartSpacesWorkbench workbench;

  /**
   * File support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct the manager.
   *
   * @param workbench
   *          the workbench to use
   */
  public StandardProjectManager(SmartSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  @Override
  public boolean isProjectFolder(File baseDir) {
    return getProjectFile(baseDir).exists();
  }

  @Override
  public Project readProject(File baseProjectDir, Log log) {
    File projectFile = getProjectFile(baseProjectDir);
    if (projectFile.exists()) {
      return readProjectFile(projectFile, log);
    }

    throw new SimpleSmartSpacesException(String.format(
        "The folder %s does not contain any legal Smart Spaces project files",
        baseProjectDir.getAbsolutePath()));
  }

  /**
   * Read a project file.
   *
   * @param projectFile
   *          the project file
   * @param log
   *          logger for reading the file
   *
   * @return the project
   */
  public Project readProjectFile(File projectFile, Log log) {
    JdomProjectReader jdomProjectReader = new JdomProjectReader(workbench);
    Project project = jdomProjectReader.readProject(projectFile);
    postProcessProject(project);

    return project;
  }

  /**
   * Do any post processing on the project.
   *
   * @param project
   *          the project to be processed
   */
  private void postProcessProject(Project project) {
    project.getConfiguration().setParent(workbench.getWorkbenchConfig());
  }

  @Override
  public Source getProjectXmlSource(Project project) {
    Source source = new SimpleSource();
    File sourceFile = getProjectFile(project.getBaseDirectory());
    source.setPath(sourceFile.getAbsolutePath());
    source.setProject(project);
    source.setContent(fileSupport.readFile(sourceFile));

    return source;
  }

  @Override
  public void saveSource(Source source) {
    fileSupport.writeFile(fileSupport.newFile(source.getPath()), source.getContent());
  }

  /**
   * Get the project file.
   *
   * @param baseDir
   *          the project base directory
   *
   * @return the file for the project file
   */
  private File getProjectFile(File baseDir) {
    return fileSupport.newFile(baseDir, FILE_NAME_PROJECT);
  }
}
