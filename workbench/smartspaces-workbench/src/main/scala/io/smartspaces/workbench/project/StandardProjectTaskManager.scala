/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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
import io.smartspaces.workbench.FreemarkerTemplater;
import io.smartspaces.workbench.programming.javalang.ExternalJavadocGenerator;
import io.smartspaces.workbench.programming.javalang.JavadocGenerator;
import io.smartspaces.workbench.project.activity.ActivityProject;
import io.smartspaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import io.smartspaces.workbench.project.activity.packager.ActivityProjectPackager;
import io.smartspaces.workbench.project.activity.packager.StandardActivityProjectPackager;
import io.smartspaces.workbench.project.builder.ProjectBuilder;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreator;
import io.smartspaces.workbench.project.ide.EclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.project.ide.SimpleEclipseIdeProjectCreatorSpecification;
import io.smartspaces.workbench.tasks.WorkbenchTaskContext;

import _root_.java.io.File;
import _root_.java.io.FileFilter;
import _root_.java.util.HashMap;
import _root_.java.util.Map;

import _root_.scala.collection.JavaConverters._

/**
 * Standard implementation of the project tasks manager.
 *
 * @author Keith M. Hughes
 */
 class StandardProjectTaskManager(projectTypeRegistry: ProjectTypeRegistry ,
      templater: FreemarkerTemplater ) extends ProjectTaskManager {

  /**
   * A packager for activities.
   */
  private val  activityProjectPackager: ActivityProjectPackager = new StandardActivityProjectPackager()

  /**
   * The IDE project creator.
   */
  private val  ideProjectCreator: EclipseIdeProjectCreator =new EclipseIdeProjectCreator(templater)

  /**
   * The file support to use.
   */
  private val fileSupport = FileSupportImpl.INSTANCE;

 
  override def  newProjectTaskContext(project: Project ,
      workbenchTaskContext: WorkbenchTaskContext ): ProjectTaskContext = {
    val ptype = projectTypeRegistry.getProjectType(project);

    val projectTaskContext =
        new ProjectTaskContext(ptype, project, workbenchTaskContext);

    // Always add a pre-task
    addPreTasks(project, projectTaskContext, workbenchTaskContext);

    workbenchTaskContext.addProjectTaskContext(projectTaskContext);

    return projectTaskContext;
  }

  override def addPreTasks(project: Project , projectTaskContext: ProjectTaskContext ,
       workbenchTaskContext: WorkbenchTaskContext): Unit = {
    workbenchTaskContext.addTasks(new ProjectPreTask(project, projectTaskContext));
  }

  override def addCleanTasks( project: Project,  projectTaskContext: ProjectTaskContext,
       workbenchTaskContext: WorkbenchTaskContext): Unit = {
    workbenchTaskContext.addTasks(new ProjectCleanTask(project, projectTaskContext));
  }

  override def addBuildTasks(project: Project ,  projectTaskContext: ProjectTaskContext,
      workbenchTaskContext: WorkbenchTaskContext ): Unit = {
    // TODO(keith): Consider putting this map into the workbench task context
    // with tasks having a classification, e.g.
    // build, clean, etc. If so, workbench task context objects should be used
    // once or have a reset functionality.
    val buildTasks: Map[Project, ProjectBuildTask] = new HashMap();
    addProjectBuildTask(project, projectTaskContext, workbenchTaskContext, buildTasks);
  }

  /**
   * Add a build task for a given project to the growing list of tasks.
   *
   * <p>
   * All dependent tasks are added.
   *
   * @param project
   *          the project a build task is needed for
   * @param projectTaskContext
   *          the task context for the project to be built
   * @param workbenchTaskContext
   *          the workbench task context the build is to be done under
   * @param existingBuildTasks
   *          all project build tasks
   *
   * @return the new build task
   */
  private def addProjectBuildTask( project: Project,
       projectTaskContext: ProjectTaskContext,  workbenchTaskContext: WorkbenchTaskContext,
       existingBuildTasks: Map[Project, ProjectBuildTask]): ProjectBuildTask = {
    val newBuildTask = new ProjectBuildTask(project, projectTaskContext);
    projectTaskContext.addProjectTask(newBuildTask);

    existingBuildTasks.put(project, newBuildTask);

    // TODO(keith): Should this go in the expanding build tasks? Probably if
    // knew general task category.
    val newPackageTask = new ProjectPackageTask(project, projectTaskContext);
    newPackageTask.addTaskDependency(newBuildTask);
    workbenchTaskContext.addTasks(newPackageTask);

    project.getDependencies().asScala.foreach { dependency =>
      if (dependency.isDynamic()) {
        val dependencyProject =
            workbenchTaskContext.getDynamicProjectFromProjectPath(dependency);
        if (dependencyProject != null) {
          var dependencyBuildTask = existingBuildTasks.get(dependencyProject);
          if (dependencyBuildTask == null) {
            // No build task currently for this dependency, so create it.
            dependencyBuildTask =
                addProjectBuildTask(dependencyProject,
                    newProjectTaskContext(dependencyProject, workbenchTaskContext),
                    workbenchTaskContext, existingBuildTasks);

            // Set the provider for the dependency.
            dependency.setProvider(new DynamicProjectProjectDependencyProvider(dependencyBuildTask
                .getProjectTaskContext()));
          }

          // Add the dependency context from the build context so that the
          // current task can get all artifacts from the
          // dependency.
          val dependencyProjectTaskContext = dependencyBuildTask.getProjectTaskContext();
          projectTaskContext.addDynamicProjectDependencyContext(dependencyProjectTaskContext)
              .addDynamicProjectDependencyContexts(
                  dependencyProjectTaskContext.getDynamicProjectDependencyContexts());

          newBuildTask.addTaskDependency(dependencyBuildTask);
        }
      }
    }

    return newBuildTask;
  }

  override def addDocsTasks(project: Project ,
      projectTaskContext: ProjectTaskContext ,  workbenchTaskContext: WorkbenchTaskContext): Unit = {
    workbenchTaskContext.addTasks(new ProjectDocTask(project, projectTaskContext));
  }

  override def addDeploymentTasks(deploymentType: String , project: Project ,
      projectTaskContext: ProjectTaskContext ,  workbenchTaskContext: WorkbenchTaskContext): Unit = {
    workbenchTaskContext.addTasks(new ProjectDeploymentTask(deploymentType, project,
        projectTaskContext));
  }

  override def addIdeProjectTasks(project: Project , ide: String ,
      projectTaskContext: ProjectTaskContext ,  workbenchTaskContext: WorkbenchTaskContext): Unit = {
    workbenchTaskContext.addTasks(new ProjectIdeTask(ide, project, projectTaskContext));
  }

  /**
   * The pre task for tasks to be done to a chain of project commands.
   *
   * @author Keith M. Hughes
   */
  class ProjectPreTask(project: Project,  projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_PRE, project, projectTaskContext) {

    override def onPerform(): Unit = {
      getProjectTaskContext().processExtraConstituents();
    }
  }

  /**
   * The task for cleaning projects.
   *
   * @author Keith M. Hughes
   */
  class ProjectCleanTask(project: Project ,  projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_CLEAN, project, projectTaskContext) {

    override def onPerform(): Unit = {
      val projectTaskContext = getProjectTaskContext();

      projectTaskContext.getLog().info(
          String.format("Cleaning project %s", getProject().getBaseDirectory().getAbsolutePath()));

      val buildDirectory = projectTaskContext.getRootBuildDirectory();

      if (buildDirectory.exists()) {
        fileSupport.deleteDirectoryContents(buildDirectory);
      }
    }
  }

  /**
   * The task for building projects.
   *
   * @author Keith M. Hughes
   */
  class ProjectBuildTask(project: Project , projectTaskContext: ProjectTaskContext ) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_BUILD, project, projectTaskContext) {

    override def onPerform(): Unit = {
      val projectTaskContext = getProjectTaskContext();

      // If no type, there is nothing special to do for building.
      val ptype: ProjectType = projectTaskContext.getProjectType();
      val builder: ProjectBuilder = if (ptype != null) {
        ptype.newBuilder(projectTaskContext)
      } else {
        new BaseActivityProjectBuilder()
      }

      projectTaskContext.getLog().info(s"Building project ${getProject().getBaseDirectory().getAbsolutePath()}");
      projectTaskContext.getLog().info(s"Using Smart Spaces Space Controller ${projectTaskContext
              .getWorkbenchTaskContext().getControllerDirectory().getAbsolutePath()}");

      builder.build(getProject(), projectTaskContext);
    }
  }

  /**
   * The task for packaging projects.
   *
   * @author Keith M. Hughes
   */
  class ProjectPackageTask(project: Project,  projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_PACKAGE, project, projectTaskContext) {

    override def onPerform(): Unit = {

      try {
        getProjectTaskContext().getLog().info(
            String
                .format("Packaging project %s", getProject().getBaseDirectory().getAbsolutePath()));

        if (ActivityProject.PROJECT_TYPE_NAME.equals(getProject().getType())) {
          activityProjectPackager.packageActivityProject(getProject(), getProjectTaskContext());
        }
      } catch  {
        case e: Throwable =>
        getProjectTaskContext().getWorkbenchTaskContext().handleError(
            "Error while packaging project", e);
      }
    }
  }

  /**
   * The task for deploying projects.
   *
   * @author Keith M. Hughes
   */
   class ProjectDeploymentTask(deploymentType: String ,  project: Project,
         projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_DEPLOY, project, projectTaskContext) {

    override def onPerform(): Unit = {
      val projectTaskContext = getProjectTaskContext();

      try {
        val project = getProject();
        projectTaskContext.getLog().info(
            String.format("Deploying project %s", project.getBaseDirectory().getAbsolutePath()));

        var deploymentMatch = false;
        project.getDeployments().asScala.foreach { deployment =>
          if (deploymentType.equals(deployment.getType())) {
            val project = projectTaskContext.getProject
            val deploymentLocation =
                projectTaskContext.getProjectTargetFile(project.getBaseDirectory(), deployment.getLocation());
            copyBuildArtifacts(deploymentLocation);
            deploymentMatch = true;
          }
        }
        if (!deploymentMatch) {
          projectTaskContext.getLog().warn(
              "No deployment target match for deployment type " + deploymentType);
        }
      } catch  {
        case e: Throwable =>
        projectTaskContext.getWorkbenchTaskContext()
            .handleError("Error while deploying project", e);
      }
    }

    /**
     * Copy the necessary build artifacts for the project.
     *
     * @param destination
     *          the destination directory for the deployment
     */
    private def copyBuildArtifacts(destination: File ): Unit = {
      if (!fileSupport.isDirectory(destination)) {
        throw SimpleSmartSpacesException.newFormattedException(
            "Deploy directory not found or not a directory: '%s'", destination.getAbsolutePath());
      }
      val projectTaskContext = getProjectTaskContext();
      val artifacts = projectTaskContext.getRootBuildDirectory().listFiles(new FileFilter() {
        override def accept( pathname: File): Boolean = {
          return pathname.isFile();
        }
      });
      if (artifacts != null && artifacts.length > 0) {
        artifacts.foreach { artifact =>
          projectTaskContext.getLog().info(
              String.format("Deploying build artifact to %s/%s", destination.getAbsolutePath(),
                  artifact.getName()));
          fileSupport.copyFile(artifact, fileSupport.newFile(destination, artifact.getName()));
        }
      } else {
        projectTaskContext.getLog().warn("No build artifacts found for project");
      }
    }
  }

  /**
   * The task for creating docs for projects.
   *
   * @author Keith M. Hughes
   */
  class ProjectDocTask(project: Project ,  projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_DOC, project, projectTaskContext) {

    override def onPerform(): Unit = {
      val projectTaskContext = getProjectTaskContext();

      val project = getProject();

      projectTaskContext.getLog().info(
          String.format("Building Docs for project %s", project.getBaseDirectory()
              .getAbsolutePath()));

      // TODO(keith): Make work for other project types
      if ("library".equals(project.getType()) || "java".equals(project.getLanguage())) {
        val generator = new ExternalJavadocGenerator();

        generator.generate(getProjectTaskContext());
      } else {
        projectTaskContext.getLog().warn(
            String.format("Project located at %s is not a java project\n", project
                .getBaseDirectory().getAbsolutePath()));
      }
    }
  }

  /**
   * The task for creating IDe projects for projects.
   *
   * @author Keith M. Hughes
   */
  class ProjectIdeTask(ide: String, project: Project ,  projectTaskContext: ProjectTaskContext) extends ProjectWorkbenchTask(StandardProjectTaskNames.TASK_NAME_IDE, project, projectTaskContext) {

    /**
     * The ide parameter for eclipse projects.
     */
    private val IDE_ECLIPSE = "eclipse";

    override def onPerform(): Unit = {
      val projectTaskContext = getProjectTaskContext();

      val project = getProject();

      projectTaskContext.getLog().info(
          String.format("Building project IDE project %s", project.getBaseDirectory()
              .getAbsolutePath()));

      val ptype: ProjectType = projectTaskContext.getProjectType();
      if (!ide.equals(IDE_ECLIPSE)) {
        throw SimpleSmartSpacesException.newFormattedException(
            "Attempt to create a project of non-supported type: '%s'. Only '%s' is supported.",
            ide, IDE_ECLIPSE);
      }
      val spec = if (ptype != null) {
        ptype.getEclipseIdeProjectCreatorSpecification(projectTaskContext);
      } else {
        new SimpleEclipseIdeProjectCreatorSpecification();
      }

      ideProjectCreator.createProject(project, projectTaskContext, spec);
    }
  }
}
