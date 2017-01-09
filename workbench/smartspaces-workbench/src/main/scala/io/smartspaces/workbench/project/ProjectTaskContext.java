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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.util.io.CanonicalFileCollector;
import io.smartspaces.util.io.FileCollector;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.constituent.ContentProjectConstituent;
import io.smartspaces.workbench.project.constituent.ProjectConstituent;
import io.smartspaces.workbench.tasks.DependencyWorkbenchTask;
import io.smartspaces.workbench.tasks.WorkbenchTaskContext;
import io.smartspaces.workbench.tasks.WorkbenchTaskModifiers;

/**
 * A context for building and packaging activities.
 *
 * @author Keith M. Hughes
 */
public class ProjectTaskContext implements ProjectContext {

  /**
   * Where things are being built.
   */
  public static final String BUILD_DIRECTORY = "build";

  /**
   * Subdirectory of build folder which contains the staged components of the
   * build.
   */
  public static final String BUILD_STAGING_DIRECTORY = "staging";

  /**
   * Extension for creating temp build directories.
   */
  public static final String TEMP_DIRECTORY_EXTENSION = ".dir";

  /**
   * Sub-directory in which to put temp build files.
   */
  public static final String BUILD_TEMP_DIRECTORY = "tmp";

  /**
   * The project being built.
   */
  private final Project project;

  /**
   * The task context for building.
   */
  private final WorkbenchTaskContext workbenchTaskContext;

  /**
   * Files to include in the project.
   */
  private final List<File> artifactsToInclude = new ArrayList<>();

  /**
   * Files generated by the project.
   */
  private final List<File> generatedArtifacts = new ArrayList<>();

  /**
   * T source directories for the project.
   */
  private final List<File> sourceDirectories = new ArrayList<>();

  /**
   * The directory where the project will be built.
   */
  private final File buildDirectory;

  /**
   * The staging directory for artifacts.
   */
  private final File stagingDirectory;

  /**
   * The project type of the project.
   */
  private final ProjectType projectType;

  /**
   * The collection of tasks for the project indexed by task name.
   *
   * TODO(keith): Should this be a multimap?
   */
  private final Map<String, DependencyWorkbenchTask> tasksForProject = new HashMap<>();

  /**
   * Project task contexts for projects which this project is dependent on.
   */
  private final Set<ProjectTaskContext> dynamicProjectDependencyContexts = new HashSet<>();

  /**
   * The name of the current task being run, can be {@code null}.
   */
  private String currentTaskName;

  /**
   * Collection of dest to src file mappings, constructed during the build
   * process.
   */
  private final CanonicalFileCollector sourceMap = new CanonicalFileCollector();

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new build context.
   *
   * @param projectType
   *          the type of the project
   * @param project
   *          project object
   * @param taskContext
   *          workbench instance
   */
  public ProjectTaskContext(ProjectType projectType, Project project,
      WorkbenchTaskContext taskContext) {
    this.projectType = projectType;
    this.project = project;
    this.workbenchTaskContext = taskContext;

    buildDirectory = fileSupport.newFile(project.getBaseDirectory(), BUILD_DIRECTORY);
    stagingDirectory = fileSupport.newFile(buildDirectory, BUILD_STAGING_DIRECTORY);

    prepareProjectConfiguration();
  }

  @Override
  public <T extends Project> T getProject() {
    return (T) project;
  }

  @Override
  public WorkbenchTaskContext getWorkbenchTaskContext() {
    return workbenchTaskContext;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ProjectType> T getProjectType() {
    return (T) projectType;
  }

  /**
   * Add anything needed to the project configuration.
   */
  private void prepareProjectConfiguration() {
    Configuration configuration = project.getConfiguration();
    configuration.setProperty(ProjectBuilder.CONFIGURATION_NAME_PROJECT_HOME,
        project.getBaseDirectory().getAbsolutePath());
    configuration.setProperty(ProjectBuilder.CONFIGURATION_NAME_PROJECT_GENERATED_RESOURCE,
        fileSupport.newFile(buildDirectory, ProjectFileLayout.GENERATED_SOURCE_ROOT)
            .getAbsolutePath());
  }

  @Override
  public File getProjectTargetFile(File rootDirectory, String target) {
    String targetPath = project.getConfiguration().evaluate(target);

    return fileSupport.resolveFile(rootDirectory, targetPath);
  }

  @Override
  public FileCollector getResourceFileCollector() {
    return sourceMap;
  }

  /**
   * Get the root build directory.
   *
   * @return the root of the build directory
   */
  public File getBuildDirectory() {
    return buildDirectory;
  }

  /**
   * Get a sub directory from the build directory.
   *
   * @param subpath
   *          relative path for the sub directory
   *
   * @return the subdirectory
   */
  public File getBuildDirectory(String subpath) {
    File subBuild = fileSupport.newFile(getBuildDirectory(), subpath);
    fileSupport.directoryExists(subBuild);
    return subBuild;
  }

  /**
   * Get the root staging directory.
   *
   * @return the root of the staging directory
   */
  public File getStagingDirectory() {
    return stagingDirectory;
  }

  /**
   * Get a unique temporary build directory.
   *
   * @return a temporary build directory
   */
  public File getTempBuildDirectory() {
    File tempDirectory = fileSupport.newFile(buildDirectory, BUILD_TEMP_DIRECTORY);
    fileSupport.mkdir(tempDirectory);
    File tempFile = fileSupport.createTempFile(tempDirectory);
    return fileSupport.newFile(tempDirectory, tempFile.getName() + TEMP_DIRECTORY_EXTENSION);
  }

  /**
   * Add a new artifact to go in the file.
   *
   * @param artifact
   *          artifact to add to context
   */
  public void addArtifactToInclude(File artifact) {
    artifactsToInclude.add(artifact);
  }

  /**
   * Get the list of artifacts to add to the project.
   *
   * @return the list of artifacts to add to the project
   */
  public List<File> getArtifactsToInclude() {
    return artifactsToInclude;
  }

  /**
   * Add a new generated artifact.
   *
   * @param artifact
   *          the generated artifact
   */
  public void addGeneratedArtifact(File artifact) {
    generatedArtifacts.add(artifact);
  }

  /**
   * Get the list of artifacts generated.
   *
   * <p>
   * Only valid after a build.
   *
   * @return the list of generated artifacts
   */
  public List<File> getGeneratedArtifacts() {
    return generatedArtifacts;
  }

  /**
   * Add in source directories for this project.
   *
   * @param sourceDirectories
   *          the directories to add
   *
   * @return this context
   */
  public ProjectTaskContext addSourceDirectories(File... sourceDirectories) {
    if (sourceDirectories != null) {
      Collections.addAll(this.sourceDirectories, sourceDirectories);
    }

    return this;
  }

  /**
   * Get the source directories for the project.
   * 
   * @return the source directories
   */
  public List<File> getSourceDirectories() {
    return sourceDirectories;
  }

  /**
   * Add in tasks that has been created for this project.
   *
   * <p>
   * The tasks will also be added to the workbench task context.
   *
   * @param tasks
   *          the tasks to add
   *
   * @return this context
   */
  public ProjectTaskContext addProjectTasks(DependencyWorkbenchTask... tasks) {
    if (tasks != null) {
      for (DependencyWorkbenchTask task : tasks) {
        if (tasksForProject.put(task.getName(), task) != null) {
          getLog().warn(String.format(
              "There was a previous task with name %s which has been dropped", task.getName()));
        }
      }

      workbenchTaskContext.addTasks(tasks);
    }

    return this;
  }

  /**
   * Modify the project tasks associated with this context with the provided
   * modifiers.
   *
   * @param modifiersCollection
   *          the modifiers to be applied
   *
   * @return this context
   */
  public ProjectTaskContext
      modifyProjectTasks(Collection<WorkbenchTaskModifiers> modifiersCollection) {
    for (WorkbenchTaskModifiers modifiers : modifiersCollection) {
      DependencyWorkbenchTask task = tasksForProject.get(modifiers.getTaskName());
      if (task != null) {
        task.applyTaskModifiers(modifiers);
      } else {
        getLog().warn(
            String.format("Task modifier found for missing task %s", modifiers.getTaskName()));
      }
    }

    return this;
  }

  /**
   * Add a task context for a dynamic project dependency.
   *
   * @param dependencyTaskContext
   *          the dependency task context to add
   *
   * @return this context
   */
  public ProjectTaskContext
      addDynamicProjectDependencyContext(ProjectTaskContext dependencyTaskContext) {
    dynamicProjectDependencyContexts.add(dependencyTaskContext);

    return this;
  }

  /**
   * Add a task context for a dynamic project dependency.
   *
   * @param dependencyTaskContexts
   *          the dependency task contexts to add
   *
   * @return this context
   */
  public ProjectTaskContext
      addDynamicProjectDependencyContexts(Collection<ProjectTaskContext> dependencyTaskContexts) {
    dynamicProjectDependencyContexts.addAll(dependencyTaskContexts);

    return this;
  }

  /**
   * Get the project task contexts for all dynamic project dependencies.
   *
   * @return the dependency project task contexts
   */
  public Set<ProjectTaskContext> getDynamicProjectDependencyContexts() {
    return dynamicProjectDependencyContexts;
  }

  /**
   * Process the extra constituents for the project.
   */
  public void processExtraConstituents() {
    List<ProjectConstituent> constituents = project.getExtraConstituents();
    if (constituents != null) {
      for (ProjectConstituent constituent : constituents) {
        constituent.processConstituent(project, this);
      }
    }
  }

  /**
   * Process any generated resources for the project.
   *
   * @param stagingDirectory
   *          the directory where the processed content should go
   */
  public void processGeneratedResources(File stagingDirectory) {
    File generatedResources =
        fileSupport.newFile(getBuildDirectory(), ProjectFileLayout.SOURCE_GENERATED_MAIN_RESOURCES);
    if (fileSupport.isDirectory(generatedResources)) {
      fileSupport.copyDirectory(generatedResources, stagingDirectory, true);
    }
  }

  /**
   * Process the needed resources for the project.
   *
   * @param stagingDirectory
   *          the directory where the processed content should go
   */
  public void processResources(File stagingDirectory) {
    processContentConstituents(project.getResources(), stagingDirectory);
  }

  /**
   * Process the needed sources for the project.
   *
   * @param stagingDirectory
   *          the directory where the processed content should go
   */
  public void processSources(File stagingDirectory) {
    processContentConstituents(project.getSources(), stagingDirectory);
  }

  /**
   * Process the list of constituents for the project.
   *
   * @param constituents
   *          constituents to process
   * @param stagingDirectory
   *          the directory where the processed content should go
   */
  private void processContentConstituents(List<ContentProjectConstituent> constituents,
      File stagingDirectory) {
    if (constituents == null) {
      return;
    }

    for (ContentProjectConstituent constituent : constituents) {
      constituent.processConstituent(project, stagingDirectory, this);
    }
  }

  /**
   * Get the current task name for this context.
   *
   * <p>
   * It will be {@code null} when tasks are not running against the context.
   *
   * @return the current task name
   */
  public String getCurrentTaskName() {
    return currentTaskName;
  }

  /**
   * Set the current task name for this context.
   *
   * <p>
   * It should be {@code null} when tasks are not running against the context.
   *
   * @param currentTaskName
   *          the current task name
   */
  public void setCurrentTaskName(String currentTaskName) {
    this.currentTaskName = currentTaskName;
  }

  @Override
  public ExtendedLog getLog() {
    return getWorkbenchTaskContext().getLog();
  }
}
