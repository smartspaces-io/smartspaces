/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.liveactivity.runtime.development.lightweight

import io.smartspaces.activity.Activity
import io.smartspaces.activity.BaseActivityRuntime
import io.smartspaces.activity.execution.BaseActivityExecutionContext
import io.smartspaces.configuration.SimpleConfiguration
import io.smartspaces.liveactivity.runtime.SimpleLiveActivityFilesystem
import io.smartspaces.liveactivity.runtime.StandardLiveActivityRuntimeComponentFactory
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl

import java.io.File
import io.smartspaces.activity.ActivityListener
import io.smartspaces.activity.ActivityStatus

/**
 * The standard lightweight activity runtime.
 *
 * @author Keith M. Hughes
 */
class StandardLightweightActivityRuntime(val spaceEnvironment: SmartSpacesEnvironment) extends LightweightActivityRuntime {

  /**
   * The component factory for activity runtimes.
   */
  private var activityRuntimeComponentFactory: StandardLiveActivityRuntimeComponentFactory = null

  /**
   * The activity runtime to inject.
   */
  private var activityRuntime: BaseActivityRuntime = null

  /**
   * The directory file root for all activity filesystems.
   */
  private var baseRuntimeDir: File = null

  /**
   * The file path for the directory file root for all activity filesystems.
   */
  var baseRuntimeDirPath = "/var/tmp/smartspaces/run"

  /**
   * The file support to use.
   */
  private val fileSupport: FileSupport = FileSupportImpl.INSTANCE

  /**
   * The activity listener to inject into all activities.
   */
  val activityListener: ActivityListener = new ActivityListener() {
    override def onActivityStatusChange(activity: Activity, oldStatus: ActivityStatus, newStatus: ActivityStatus): Unit = {
      handleActivityStatusChange(activity, oldStatus, newStatus)
    }
  }

  override def startup(): Unit = {
    activityRuntimeComponentFactory =
      new StandardLiveActivityRuntimeComponentFactory(spaceEnvironment, null)
    activityRuntimeComponentFactory
      .registerCoreServices(spaceEnvironment.getServiceRegistry())
    activityRuntime = new BaseActivityRuntime(
      activityRuntimeComponentFactory.newNativeActivityRunnerFactory(),
      activityRuntimeComponentFactory.newActivityComponentFactory(), spaceEnvironment)

    baseRuntimeDir = new File("/var/tmp/smartspaces/run")
    fileSupport.directoryExists(baseRuntimeDir)

  }

  override def shutdown(): Unit = {

  }

  override def injectActivity(activity: Activity): Unit = {
    val activityUuid = "test"

    val activityExecutionContext = new BaseActivityExecutionContext()

    val activityFileBase = fileSupport.newFile(baseRuntimeDir, activityUuid)
    val activityFilesystem = new SimpleLiveActivityFilesystem(activityFileBase)
    activityFilesystem.ensureDirectories()

    val conf = new SimpleConfiguration(spaceEnvironment.getSystemConfiguration().getExpressionEvaluator())
    conf.setParent(spaceEnvironment.getSystemConfiguration())

    activity.setLog(spaceEnvironment.getLog())
    activity.setSpaceEnvironment(spaceEnvironment)
    activity.setActivityRuntime(activityRuntime)
    activity.setExecutionContext(activityExecutionContext)
    activity.setConfiguration(conf)
    activity.setActivityFilesystem(activityFilesystem)
    activity.setUuid(activityUuid)
    activity.setName("org.test.foo")

    activity.addActivityListener(activityListener)
  }

  /**
   * Handle all activity state changes.
   */
  private def handleActivityStatusChange(activity: Activity, oldStatus: ActivityStatus, newStatus: ActivityStatus): Unit = {
    spaceEnvironment.getLog.formatInfo("Activity %s has transitioned from state %s to state %s", activity.getUuid(), oldStatus, newStatus)
  }

}