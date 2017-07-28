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

package io.smartspaces.workbench.tasks

import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.io.FilenameFilter
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map

import com.google.common.io.Closeables

import io.smartspaces.SimpleSmartSpacesException
import io.smartspaces.SmartSpacesException
import io.smartspaces.configuration.Configuration
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.NamedVersionedResourceCollection
import io.smartspaces.resource.NamedVersionedResourceWithData
import io.smartspaces.resource.Version
import io.smartspaces.resource.VersionRange
import io.smartspaces.system.core.configuration.CoreConfiguration
import io.smartspaces.system.core.container.ContainerFilesystemLayout
import io.smartspaces.util.graph.DependencyResolver
import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.util.process.NativeApplicationRunnerCollection
import io.smartspaces.util.process.StandardNativeApplicationRunnerCollection
import io.smartspaces.workbench.SmartSpacesContainer
import io.smartspaces.workbench.SmartSpacesWorkbench
import io.smartspaces.workbench.project.Project
import io.smartspaces.workbench.project.ProjectDependency
import io.smartspaces.workbench.project.ProjectManager
import io.smartspaces.workbench.project.ProjectTaskContext

import scala.collection.JavaConverters._

object WorkbenchTaskContext {

  /**
   * The base folder for extras in the workbench.
   */
  val EXTRAS_BASE_FOLDER = "extras"

  /**
   * File extension for a Java jar file.
   */
  val FILENAME_JAR_EXTENSION = ".jar"

  /**
   * The file extension used for files which give container extensions.
   */
  val EXTENSION_FILE_EXTENSION = ".ext"

  /**
   * The keyword header for a package line on an extensions file.
   */
  val EXTENSION_FILE_PATH_KEYWORD = "path:"

  /**
   * The length of the keyword header for a package line on an extensions file.
   */
  val EXTENSION_FILE_PATH_KEYWORD_LENGTH = EXTENSION_FILE_PATH_KEYWORD.length()

  /**
   * Configuration property giving the project path for the workbench.
   */
  val CONFIGURATION_NAME_SMARTSPACES_WORKBENCH_PROJECT_PATH =
    "smartspaces.workbench.project.path"

  /**
   * Configuration property giving the location of the controller the workbench
   * is using.
   */
  val CONFIGURATION_NAME_CONTROLLER_BASEDIR =
    "smartspaces.controller.basedir"

  /**
   * Configuration property giving the location of the master the workbench is
   * using.
   */
  val CONFIGURATION_NAME_MASTER_BASEDIR = "smartspaces.master.basedir"

  /**
   * A file filter for detecting directories.
   */
  val DIRECTORY_FILE_FILTER = new FileFilter() {
    override def accept(pathname: File): Boolean = {
      return pathname.isDirectory()
    }
  }

  /**
   * A file filter for detecting jar files.
   */
  val JAR_FILE_FILTER = new FileFilter() {
    override def accept(pathname: File): Boolean = {
      return pathname.getName().endsWith(".jar")
    }
  }

}
/**
 * The main context for a workbench task run.
 *
 * @author Keith M. Hughes
 */
class WorkbenchTaskContext(workbench: SmartSpacesWorkbench, workbenchConfig: Configuration) {

  /**
   * The list of tasks.
   */
  private val tasks: List[DependencyWorkbenchTask] = new ArrayList()

  /**
   * The collection of projects scanned from the project path.
   */
  private val projectsPath: NamedVersionedResourceCollection[NamedVersionedResourceWithData[Project]] = NamedVersionedResourceCollection.newNamedVersionedResourceCollection()

  /**
   * {@code true} if the project path has been scanned.
   */
  private var projectPathScanned = false

  /**
   * File support for file operations.
   */
  private val fileSupport = FileSupportImpl.INSTANCE

  /**
   * Some task had an error during the task processing process.
   */
  private var errors = false

  /**
   * The native application runners collection for this context.
   */
  private var nativeApplicationRunners: StandardNativeApplicationRunnerCollection = _

  /**
   * Map of project task contextx for the current run.
   */
  private val projectTaskContexts: Map[Project, ProjectTaskContext] = new HashMap()

  /**
   * Get the workbench for the context.
   *
   * @return the workbench
   */
  def getWorkbench(): SmartSpacesWorkbench = {
    return workbench
  }

  /**
   * An error has happened, handle it properly.
   *
   * @param message
   *          message for the error
   * @param e
   *          any exception that may have happened, can be {@code null}
   */
  def handleError(message: String, e: Throwable): Unit = {
    errors = true

    logException(e)

    throw new SmartspacesWorkbenchTaskInterruptionException()
  }

  /**
   * Log an exception.
   *
   * @param e
   *          the exception to log
   */
  private def logException(e: Throwable): Unit = {
    if (e.isInstanceOf[SimpleSmartSpacesException]) {
      workbench.getLog().error((e.asInstanceOf[SimpleSmartSpacesException]).getCompoundMessage())
    } else {
      workbench.getLog().error("Error executing workbench commands", e)
    }
  }

  /**
   * An error has happened, handle it properly.
   *
   * @param message
   *          message for the error
   */
  def handleError(message: String): Unit = {
    handleError(message, null)
  }

  /**
   * Add a new collection of tasks to the context.
   *
   * @param tasks
   *          the tasks to add
   *
   * @return this task context
   */
  def addTasks(tasks: DependencyWorkbenchTask*): WorkbenchTaskContext = {
    if (tasks != null) {
      tasks.foreach(task => this.tasks.add(task))
    }

    return this
  }

  /**
   * Do all tasks in the task list.
   */
  def doTasks(): Unit = {
    try {
      prepareForTaskPerformance()

      getTasksInDependencyOrder().asScala.foreach { task =>

        performTask(task)

        if (errors) {
          return 
        }
      }
    } finally {
      endTaskPerformance()
    }
  }

  /**
   * Prepare for performing all tasks.
   */
  private def prepareForTaskPerformance(): Unit = {
    nativeApplicationRunners =
      new StandardNativeApplicationRunnerCollection(workbench.getSpaceEnvironment(),
        workbench.getLog())
    nativeApplicationRunners.startup()
  }

  /**
   * End performing all tasks.
   */
  private def endTaskPerformance(): Unit = {
    nativeApplicationRunners.shutdown()
    nativeApplicationRunners = null
  }

  /**
   * Perform a task, including any before and after tasks.
   *
   * <p>
   * Performance is interrupted if any tasks fail.
   *
   * @param task
   *          the task to perform
   */
  private def performTask(task: WorkbenchTask): Unit = {
    performTaskList(task.getBeforeTasks())

    if (errors) {
      return 
    }

    performMainTask(task)

    if (errors) {
      return 
    }

    performTaskList(task.getAfterTasks())
  }

  /**
   * Perform all tasks in the list in proper order. Return immediately if any
   * errors.
   *
   * @param tasks
   *          the tasks to perform
   */
  private def performTaskList(tasks: List[WorkbenchTask]): Unit = {
    tasks.asScala.foreach { task =>
      performTask(task)

      if (errors) {
        return 
      }
    }
  }

  /**
   * Perform an individual task.
   *
   * @param task
   *          the task to perform
   */
  private def performMainTask(task: WorkbenchTask): Unit = {
    try {
      task.perform(this)
    } catch {
      case e: Throwable =>
        // last change catching of any exceptions not explicitly covered.
        if (!(e.isInstanceOf[SmartspacesWorkbenchTaskInterruptionException])) {
          errors = true
          logException(e)
        }
    }
  }

  /**
   * Order the tasks in dependency order.
   *
   * @return the tasks in dependency order
   */
  private def getTasksInDependencyOrder(): List[DependencyWorkbenchTask] = {
    val resolver =
      new DependencyResolver[DependencyWorkbenchTask, DependencyWorkbenchTask]()

    tasks.asScala.foreach { task =>
      resolver.addNode(task, task)
      resolver.addNodeDependencies(task, task.getTaskDependencies())
    }

    resolver.resolve()

    return resolver.getOrdering
  }

  /**
   * Has the project tasks had an error?
   *
   * @return {@code true} if errors
   */
  def hasErrors(): Boolean = {
    return errors
  }

  /**
   * Get all files in the workbench bootstrap folders, both system and user.
   *
   * @return all files in bootstrap folder.
   */
  def getAllWorkbenchBootstrapFiles(): List[File] = {
    val files: List[File] = new ArrayList()

    addJarFiles(workbench.getWorkbenchFileSystem().getSystemBootstrapDirectory(), files)
    val userBootstrap = workbench.getWorkbenchFileSystem().getUserBootstrapDirectory()
    if (userBootstrap.exists() && userBootstrap.isDirectory()) {
      addJarFiles(userBootstrap, files)
    }

    return files
  }

  /**
   * Get a list of all files on the controller's system bootstrap classpath.
   *
   * @return all files on the classpath
   */
  def getControllerSystemBootstrapClasspath(): List[File] = {
    val classpath: List[File] = new ArrayList()

    addJarFiles(fileSupport.newFile(getControllerDirectory(),
      ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP), classpath)

    val controllerDirectory = getControllerDirectory()
    val javaSystemDirectory =
      fileSupport.newFile(controllerDirectory,
        SmartSpacesContainer.SMARTSPACES_CONTAINER_FOLDER_LIB_SYSTEM_JVM)
    if (!javaSystemDirectory.isDirectory()) {
      throw new SimpleSmartSpacesException(String.format(
        "Controller directory %s configured by %s does not appear to be valid.",
        controllerDirectory, WorkbenchTaskContext.CONFIGURATION_NAME_CONTROLLER_BASEDIR))
    }

    classpath.addAll(fileSupport.collectFiles(javaSystemDirectory, WorkbenchTaskContext.JAR_FILE_FILTER, false))

    addControllerExtensionsClasspath(classpath)

    return classpath
  }

  /**
   * Add all JAR files from the given directory to the list of files.
   *
   * @param directory
   *          the directory to get the jar files from
   * @param fileList
   *          the list to add the files to
   */
  def addJarFiles(directory: File, fileList: List[File]): Unit = {
    val files = directory.listFiles(new FileFilter() {
      override def accept(pathname: File): Boolean = {
        pathname.getName().endsWith(WorkbenchTaskContext.FILENAME_JAR_EXTENSION)
      }
    })
    if (files != null) {
      files.foreach { file => fileList.add(file) }
    }
  }

  /**
   * Get the controller directory which is supporting this workbench.
   *
   * @return the controller directory
   */
  def getControllerDirectory(): File = {
    val controllerPath = workbenchConfig.getPropertyString(WorkbenchTaskContext.CONFIGURATION_NAME_CONTROLLER_BASEDIR)
    val controllerDirectory = fileSupport.newFile(controllerPath)
    if (controllerDirectory.isAbsolute()) {
      return controllerDirectory
    }
    val homeDir =
      fileSupport.newFile(workbenchConfig
        .getPropertyString(CoreConfiguration.CONFIGURATION_NAME_SMARTSPACES_HOME))
    return fileSupport.newFile(homeDir, controllerPath)
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param files
   *          the list of files to add to.
   */
  private def addControllerExtensionsClasspath(files: List[File]): Unit = {
    val extensionFiles =
      fileSupport.newFile(
        fileSupport.newFile(getControllerDirectory(),
          ContainerFilesystemLayout.FOLDER_DEFAULT_CONFIG),
        ContainerFilesystemLayout.FOLDER_CONFIG_ENVIRONMENT).listFiles(new FilenameFilter() {
          override def accept(dir: File, name: String): Boolean = {
            return name.endsWith(WorkbenchTaskContext.EXTENSION_FILE_EXTENSION)
          }
        })

    if (extensionFiles != null) {
      extensionFiles.foreach { extensionFile =>
        processExtensionFile(files, extensionFile, getControllerDirectory())
      }
    }
  }

  /**
   * Add all extension classpath entries that the controller specifies.
   *
   * @param classpath
   *          the list of files to add to
   * @param extraComponent
   *          the extra component to add
   */
  def addExtrasControllerExtensionsClasspath(classpath: List[File], extraComponent: String): Unit = {
    val extraComponentFiles =
      fileSupport.newFile(
        fileSupport.newFile(workbench.getWorkbenchFileSystem().getInstallDirectory(),
          WorkbenchTaskContext.EXTRAS_BASE_FOLDER), extraComponent).listFiles(new FilenameFilter() {
          override def accept(dir: File, name: String): Boolean = {
            return name.endsWith(WorkbenchTaskContext.FILENAME_JAR_EXTENSION)
          }
        })

    if (extraComponentFiles != null) {
      extraComponentFiles.foreach { component => classpath.add(component) }
    }
  }

  /**
   * process an extension file.
   *
   * @param files
   *          the collection of jars described in the extension files
   * @param extensionFile
   *          the extension file to process
   * @param controllerBaseDir
   *          base directory of the controller
   */
  private def processExtensionFile(files: List[File], extensionFile: File, controllerBaseDir: File): Unit = {
    var reader: BufferedReader = null
    try {
      reader = new BufferedReader(new FileReader(extensionFile))

      var line: String = reader.readLine()
      while (line != null) {
        line = line.trim()
        if (!line.isEmpty()) {
          val pos = line.indexOf(WorkbenchTaskContext.EXTENSION_FILE_PATH_KEYWORD)
          if (pos == 0 && line.length() > WorkbenchTaskContext.EXTENSION_FILE_PATH_KEYWORD_LENGTH) {
            val classpathAddition = line.substring(WorkbenchTaskContext.EXTENSION_FILE_PATH_KEYWORD_LENGTH).trim()

            // Want to be able to have files relative to the controller
            var classpathFile = fileSupport.newFile(classpathAddition)
            if (!classpathFile.isAbsolute()) {
              classpathFile = fileSupport.newFile(controllerBaseDir, classpathAddition)
            }
            files.add(classpathFile)
          }
        }
        line = reader.readLine()
      }
    } catch {
      case e: Throwable =>
        handleError("Error while creating project", e)
    } finally {
      Closeables.closeQuietly(reader)
    }
  }

  /**
   * Scan the project path.
   */
  def scanProjectPath(): Unit = {
    if (!projectPathScanned) {
      val projectManager = workbench.getProjectManager()
      val projectPaths =
        workbenchConfig.getPropertyStringList(
          WorkbenchTaskContext.CONFIGURATION_NAME_SMARTSPACES_WORKBENCH_PROJECT_PATH, File.pathSeparator)
      if (projectPaths != null) {
        projectPaths.asScala.foreach { projectPath =>
          val projectPathBaseDir = fileSupport.newFile(projectPath)
          processProjectPathDirectory(projectPathBaseDir, projectManager)
        }
      }

      projectPathScanned = true
    }
  }

  /**
   * Process a directory as part of the project path.
   *
   * @param projectPathBaseDir
   *          the base directory to be scanned
   * @param projectManager
   *          the project manager
   */
  private def processProjectPathDirectory(projectPathBaseDir: File, projectManager: ProjectManager): Unit = {
    if (projectManager.isProjectFolder(projectPathBaseDir)) {
      processProjectPathProjectDirectory(projectPathBaseDir, projectManager)
    } else {
      processProjectPathSubDirectories(projectPathBaseDir, projectManager)
    }
  }

  /**
   * Process a project directory from the project path.
   *
   * @param projectDir
   *          the project directory
   * @param projectManager
   *          the project manager
   */
  private def processProjectPathProjectDirectory(projectDir: File, projectManager: ProjectManager): Unit = {
    val project = projectManager.readProject(projectDir, workbench.getLog())
    addProjectToProjectPath(project)
  }

  /**
   * Add a project to the project path.
   *
   * @param project
   *          the project to add to the project path
   */
  def addProjectToProjectPath(project: Project): Unit = {
    val identifyingName = project.getIdentifyingName()
    val version = project.getVersion()
    projectsPath.addResource(identifyingName, version, new NamedVersionedResourceWithData[Project](
      identifyingName, version, project))
  }

  /**
   * Recursively process a directory for any project directories contained
   * within.
   *
   * @param projectPathBaseDir
   *          the base directory to be scanned for subdirectories
   * @param projectManager
   *          the project manager to use
   */
  private def processProjectPathSubDirectories(projectPathBaseDir: File,
    projectManager: ProjectManager): Unit = {
    val subdirectories = projectPathBaseDir.listFiles(WorkbenchTaskContext.DIRECTORY_FILE_FILTER)
    if (subdirectories != null) {
      subdirectories.foreach { subdirectory =>
        processProjectPathDirectory(subdirectory, projectManager)
      }
    }
  }

  /**
   * Get a project dependency from the project path.
   *
   * @param projectDependency
   *          the project dependency
   *
   * @return the project, or {@code null} if no projects satisfies the request
   */
  def getDynamicProjectFromProjectPath(projectDependency: ProjectDependency): Project = {
    return getDynamicProjectFromProjectPath(projectDependency.getIdentifyingName(),
      projectDependency.getVersion())
  }

  /**
   * Get a project from the project path.
   *
   * @param identifyingName
   *          identifying name of the project
   * @param versionRange
   *          version range of the project
   *
   * @return the project, or {@code null} if no projects satisfies the request
   */
  def getDynamicProjectFromProjectPath(identifyingName: String, versionRange: VersionRange): Project = {
    // TODO(keith): Consider moving all project repository scanning
    // functionality into its own object that can be shared
    // between tasks.
    scanProjectPath()

    val project = projectsPath.getResource(identifyingName, versionRange)
    if (project != null) {
      return project.getData()
    } else {
      return null
    }
  }

  /**
   * Add in a new project task context to the context.
   *
   * @param projectTaskContext
   *          the new project task context
   */
  def addProjectTaskContext(projectTaskContext: ProjectTaskContext): Unit = {
    projectTaskContexts.put(projectTaskContext.getProject(), projectTaskContext)
  }

  /**
   * Get project task context for the given project.
   *
   * @param project
   *          the given project
   *
   * @return the context for the given project, or {@code null} if none
   */
  def getProjectTaskContext(project: Project): ProjectTaskContext = {
    return projectTaskContexts.get(project)
  }

  /**
   * Get the native application runners for the context.
   *
   * @return the native application runners
   */
  def getNativeApplicationRunners(): NativeApplicationRunnerCollection = {
    return nativeApplicationRunners
  }

  /**
   * Get the context log.
   *
   * @return the context log
   */
  def getLog(): ExtendedLog = {
    return getWorkbench().getLog()
  }

  /**
   * An interruption of workbench tasks.
   *
   * @author Keith M. Hughes
   */
  class SmartspacesWorkbenchTaskInterruptionException extends SmartSpacesException("Task execution interruption") {
  }
}
