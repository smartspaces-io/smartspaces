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
import io.smartspaces.workbench.project.activity.type.ProjectType;
import io.smartspaces.workbench.tasks.WorkbenchTaskContext;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Useful constants and methods for working with Java projects.
 *
 * @author Keith M. Hughes
 */
public abstract class JavaProjectType implements ProjectType {

  /**
   * The configuration name for the project Java bootstrap classpath.
   */
  public static final String CONFIGURATION_NAME_PROJECT_JAVA_CLASSPATH_BOOTSTRAP =
      "project.java.classpath.bootstrap";

  /**
   * The configuration name for the project Java additional (non-bootstrap)
   * classpath.
   */
  public static final String CONFIGURATION_NAME_PROJECT_JAVA_CLASSPATH_ADDITIONS =
      "project.java.classpath.additions";

  /**
   * A joiner for creating classpaths.
   */
  private static final Joiner CLASSPATH_JOINER = Joiner.on(":");

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Source location for the Java source files.
   */
  public static final String SOURCE_MAIN_JAVA = "src/main/java";

  /**
   * Source location for tests.
   */
  public static final String SOURCE_MAIN_TESTS = "src/test/java";

  /**
   * Source location for test resources.
   */
  public static final String SOURCE_MAIN_TEST_RESOURCES = "src/test/resources";

  /**
   * Source location for the generated Java source files.
   */
  public static final String SOURCE_GENERATED_MAIN_JAVA = ProjectType.GENERATED_SOURCE_ROOT
      + "/main/java";

  /**
   * Source location for generated tests.
   */
  public static final String SOURCE_GENERATED_MAIN_TESTS = ProjectType.GENERATED_SOURCE_ROOT
      + "/test/java";

  /**
   * The extras component for testing support.
   */
  public static final String TESTING_EXTRAS_COMPONENT = "testing";

  /**
   * Get a classpath that would be used at runtime for the project.
   *
   * @param needsDynamicArtifacts
   *          {@code true} if needs artifacts from the dynamic projects
   * @param projectTaskContext
   *          the project build context
   * @param classpath
   *          the classpath list to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbenchTaskContext
   *          the workbench task context
   */
  public void getRuntimeClasspath(boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, List<File> classpath, JavaProjectExtension extension,
      WorkbenchTaskContext workbenchTaskContext) {
    List<File> bootstrapClasspath = workbenchTaskContext.getControllerSystemBootstrapClasspath();

    if (extension != null) {
      extension.addToClasspath(bootstrapClasspath, projectTaskContext);
    }

    classpath.addAll(bootstrapClasspath);

    addClasspathConfiguration(bootstrapClasspath,
        CONFIGURATION_NAME_PROJECT_JAVA_CLASSPATH_BOOTSTRAP, projectTaskContext);

    Set<File> classpathAdditions = Sets.newHashSet();
    Set<File> dynamicProjectDependencies =
        addDependenciesFromDynamicProjectTaskContexts(projectTaskContext);
    classpath.addAll(dynamicProjectDependencies);
    classpathAdditions.addAll(dynamicProjectDependencies);

    // The compiletime class path will be everything needed to compile the
    // project. This includes resources that will be
    // statically linked. The runtime class path will be bundles that will be
    // available at runtime and won't be
    // statically linked.
    Set<File> compiletimeClasspathFromUserBootstrap = Sets.newHashSet();
    Set<File> runtimeClasspathFromUserBootstrap = Sets.newHashSet();
    addDependenciesFromUserBootstrap(compiletimeClasspathFromUserBootstrap,
        runtimeClasspathFromUserBootstrap, needsDynamicArtifacts, projectTaskContext,
        workbenchTaskContext);
    classpath.addAll(compiletimeClasspathFromUserBootstrap);
    classpathAdditions.addAll(runtimeClasspathFromUserBootstrap);

    addClasspathConfiguration(classpathAdditions,
        CONFIGURATION_NAME_PROJECT_JAVA_CLASSPATH_ADDITIONS, projectTaskContext);
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
    List<String> path = Lists.newArrayList();
    for (File file : classpathPiece) {
      path.add(file.getAbsolutePath());
    }

    String configurationValue = CLASSPATH_JOINER.join(path);
    projectTaskContext.getProject().getConfiguration()
        .setValue(configurationParameter, configurationValue);
  }

  /**
   * Add all generated artifacts from all dynamic dependencies to the classpath.
   *
   * @param projectTaskContext
   *          context for the project the classpath is needed for
   *
   * @return the files being added
   */
  private Set<File> addDependenciesFromDynamicProjectTaskContexts(
      ProjectTaskContext projectTaskContext) {
    Set<File> filesToAdd = Sets.newHashSet();
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
   * In essense, this method returns two values, the classpaths in the first 2
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
   * @param wokbenchTaskContext
   *          the workbench task context
   */
  private void addDependenciesFromUserBootstrap(Set<File> compiletimeClasspathFromUserBootstrap,
      Set<File> runtimeClasspathFromUserBootstrap, boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, WorkbenchTaskContext wokbenchTaskContext) {

    NamedVersionedResourceCollection<NamedVersionedResourceWithData<URI>> startupResources =
        new OsgiResourceAnalyzer(wokbenchTaskContext.getWorkbench().getLog())
            .getResourceCollection(fileSupport.newFile(
                wokbenchTaskContext.getControllerDirectory(),
                ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP));
    for (ProjectDependency dependency : projectTaskContext.getProject().getDependencies()) {
      // Skip the dependency if a dynamic project that exists on the workbench
      // project path.
      if (dependency.isDynamic()
          && wokbenchTaskContext.getDynamicProjectFromProjectPath(dependency) != null) {
        continue;
      }

      NamedVersionedResourceWithData<URI> dependencyProvider =
          startupResources.getResource(dependency.getIdentifyingName(), dependency.getVersion());
      if (dependencyProvider != null) {
        File dependencyFile = fileSupport.newFile(dependencyProvider.getData());

        projectTaskContext.getLog().info(
            String.format("Project Dependency %s:%s is being satisfied by %s",
                dependency.getIdentifyingName(), dependency.getVersion(),
                dependencyFile.getAbsolutePath()));

        dependency.setProvider(new FileProjectDependencyProvider(dependencyFile));

        compiletimeClasspathFromUserBootstrap.add(dependencyFile);
        if (ProjectDependencyLinking.RUNTIME == dependency.getLinking()) {
          runtimeClasspathFromUserBootstrap.add(dependencyFile);
        }
      } else {
        // TODO(keith): Collect all missing and put into a single exception.
        throw SimpleSmartSpacesException.newFormattedException(
            "Project has listed dependency that isn't available %s:%s",
            dependency.getIdentifyingName(), dependency.getVersion());
      }
    }
  }

  /**
   * Get a classpath that would be used as part of the project for the project.
   *
   * <p>
   * This includes runtime classes.
   *
   * @param needsDynamicArtifacts
   *          {@code true} if needs artifacts from the dynamic projects
   * @param projectTaskContext
   *          the project build context
   * @param classpath
   *          the classpath to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param wokbenchTaskContext
   *          the workbench task context
   */
  public void getProjectClasspath(boolean needsDynamicArtifacts,
      ProjectTaskContext projectTaskContext, List<File> classpath, JavaProjectExtension extension,
      WorkbenchTaskContext wokbenchTaskContext) {
    getRuntimeClasspath(needsDynamicArtifacts, projectTaskContext, classpath, extension,
        wokbenchTaskContext);

    projectTaskContext.getWorkbenchTaskContext().addExtrasControllerExtensionsClasspath(classpath,
        TESTING_EXTRAS_COMPONENT);
  }
}
