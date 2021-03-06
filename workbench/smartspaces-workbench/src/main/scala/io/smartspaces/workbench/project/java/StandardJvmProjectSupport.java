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

package io.smartspaces.workbench.project.java;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.resource.NamedVersionedResourceCollection;
import io.smartspaces.resource.NamedVersionedResourceWithData;
import io.smartspaces.resource.analysis.OsgiResourceAnalyzer;
import io.smartspaces.system.core.container.ContainerFilesystemLayout;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.FileProjectDependencyProvider;
import io.smartspaces.workbench.project.ProjectDependency;
import io.smartspaces.workbench.project.ProjectDependency.ProjectDependencyLinking;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.tasks.WorkbenchTaskContext;

import com.google.common.base.Joiner;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Useful constants and methods for working with Java projects.
 *
 * @author Keith M. Hughes
 */
public class StandardJvmProjectSupport implements JvmProjectSupport  {

  /**
   * A joiner for creating classpaths.
   */
  private static final Joiner CLASSPATH_JOINER = Joiner.on(":");

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void getRuntimeClasspath(boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, List<File> classpath, JvmProjectExtension extension,
      WorkbenchTaskContext workbenchTaskContext) {
    List<File> bootstrapClasspath = workbenchTaskContext.getControllerSystemBootstrapClasspath();

    if (extension != null) {
      extension.addToClasspath(bootstrapClasspath, projectTaskContext);
    }

    classpath.addAll(bootstrapClasspath);

    addClasspathConfiguration(bootstrapClasspath, CONFIGURATION_NAME_PROJECT_CLASSPATH_BOOTSTRAP,
        projectTaskContext);

    Set<File> classpathAdditions = new HashSet<>();
    Set<File> dynamicProjectDependencies =
        addDependenciesFromDynamicProjectTaskContexts(projectTaskContext);
    classpath.addAll(dynamicProjectDependencies);
    classpathAdditions.addAll(dynamicProjectDependencies);

    // The compiletime class path will be everything needed to compile the
    // project. This includes resources that will be
    // statically linked. The runtime class path will be bundles that will
    // be
    // available at runtime and won't be
    // statically linked.
    Set<File> compiletimeClasspathFromUserBootstrap = new HashSet<>();
    Set<File> runtimeClasspathFromUserBootstrap = new HashSet<>();
    addDependenciesFromUserBootstrap(compiletimeClasspathFromUserBootstrap,
        runtimeClasspathFromUserBootstrap, needsDynamicArtifacts, projectTaskContext,
        workbenchTaskContext);
    classpath.addAll(compiletimeClasspathFromUserBootstrap);
    classpathAdditions.addAll(runtimeClasspathFromUserBootstrap);

    addClasspathConfiguration(classpathAdditions, CONFIGURATION_NAME_PROJECT_CLASSPATH_ADDITIONS,
        projectTaskContext);
  }

  @Override
  public void getProjectClasspath(boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, List<File> classpath, JvmProjectExtension extension,
      WorkbenchTaskContext wokbenchTaskContext) {
    getRuntimeClasspath(needsDynamicArtifacts, projectTaskContext, classpath, extension,
        wokbenchTaskContext);

    projectTaskContext.getWorkbenchTaskContext().addExtrasControllerExtensionsClasspath(classpath,
        TESTING_EXTRAS_COMPONENT);
  }

  /**
   * Add in a classpath configuration parameter.
   *
   * @param classpathPiece
   *          the piece of the classpath to create the configuration parameter
   *          from
   * @param configurationParameter
   *          the name of the configuration parameter
   * @param projectTaskContext
   *          the project task context
   */
  private void addClasspathConfiguration(Collection<File> classpathPiece,
      String configurationParameter, ProjectTaskContext projectTaskContext) {
    List<String> path = new ArrayList<>();
    for (File file : classpathPiece) {
      path.add(file.getAbsolutePath());
    }

    String configurationValue = CLASSPATH_JOINER.join(path);
    projectTaskContext.getProject().getConfiguration().setProperty(configurationParameter,
        configurationValue);
  }

  /**
   * Add all generated artifacts from all dynamic dependencies to the classpath.
   *
   * @param projectTaskContext
   *          context for the project the classpath is needed for
   *
   * @return the files being added
   */
  private Set<File>
      addDependenciesFromDynamicProjectTaskContexts(ProjectTaskContext projectTaskContext) {
    Set<File> filesToAdd = new HashSet<>();
    for (ProjectTaskContext dynamicProjectTaskContext : projectTaskContext
        .getDynamicProjectDependencyContexts()) {
      filesToAdd.addAll(dynamicProjectTaskContext.getGeneratedArtifacts());
    }

    return filesToAdd;
  }

  /**
   * Get dependencies for the compile time and runtime classpaths if they are
   * found in the user bootstrap folder of the controller.
   *
   * <p>
   * In essence, this method returns two values, the classpaths in the first 2
   * arguments.
   *
   * @param compiletimeClasspathFromUserBootstrap
   *          the classpath items needed during compile time
   * @param runtimeClasspathFromUserBootstrap
   *          the classpath items that will be available at runtime
   * @param needsDynamicArtifacts
   *          {@code true} if needs artifacts from the dynamic projects
   * @param projectTaskContext
   *          the project build context
   * @param workbenchTaskContext
   *          the workbench task context
   */
  private void addDependenciesFromUserBootstrap(Set<File> compiletimeClasspathFromUserBootstrap,
      Set<File> runtimeClasspathFromUserBootstrap, boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, WorkbenchTaskContext workbenchTaskContext) {

    NamedVersionedResourceCollection<NamedVersionedResourceWithData<URI>> startupResources =
        new OsgiResourceAnalyzer(workbenchTaskContext.getWorkbench().getLog())
            .getResourceCollection(fileSupport.newFile(workbenchTaskContext.getControllerDirectory(),
                ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP));
    for (ProjectDependency dependency : projectTaskContext.getProject().getDependencies()) {
      // Skip the dependency if a dynamic project that exists on the
      // workbench
      // project path.
      if (dependency.isDynamic()
          && workbenchTaskContext.getDynamicProjectFromProjectPath(dependency) != null) {
        continue;
      }

      NamedVersionedResourceWithData<URI> dependencyProvider =
          startupResources.getResource(dependency.getIdentifyingName(), dependency.getVersion());
      if (dependencyProvider != null) {
        File dependencyFile = fileSupport.newFile(dependencyProvider.getData());

        projectTaskContext.getLog().formatInfo("Project Dependency %s:%s is being satisfied by %s",
            dependency.getIdentifyingName(), dependency.getVersion(),
            dependencyFile.getAbsolutePath());

        dependency.setProvider(new FileProjectDependencyProvider(dependencyFile));

        compiletimeClasspathFromUserBootstrap.add(dependencyFile);
        if (ProjectDependencyLinking.RUNTIME == dependency.getLinking()) {
          runtimeClasspathFromUserBootstrap.add(dependencyFile);
        }
      } else {
        // TODO(keith): Collect all missing and put into a single
        // exception.
        throw SimpleSmartSpacesException.newFormattedException(
            "Project has listed dependency that isn't available %s:%s",
            dependency.getIdentifyingName(), dependency.getVersion());
      }
    }
  }
}
