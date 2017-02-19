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

package io.smartspaces.master.server.services.internal;

import io.smartspaces.domain.system.NamedScript;
import io.smartspaces.master.api.master.MasterApiActivityManager;
import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.api.master.MasterApiResourceManager;
import io.smartspaces.master.api.master.MasterApiSpaceControllerManager;
import io.smartspaces.master.api.messages.MasterApiMessageSupport;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.AutomationManager;
import io.smartspaces.master.server.services.ScriptingNames;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.service.scheduler.SchedulerService;
import io.smartspaces.service.script.ScriptService;
import io.smartspaces.service.script.StringScriptSource;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.io.directorywatcher.BaseDirectoryWatcherListener;
import io.smartspaces.util.io.directorywatcher.DirectoryWatcher;
import io.smartspaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A standard implementation of the {@link AutomationManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardAutomationManager implements AutomationManager {

  /**
   * How often the watched directories should be scanned. In seconds.
   */
  public static final int DIRECTORY_SCAN_TIME = 10;

  /**
   * Watched subfolder for importing new activities.
   */
  public static final String ACTIVITY_IMPORT_DIRECTORY = "master/autoimport/activity/import";

  /**
   * Watched subfolder for importing and deploying new activities.
   */
  public static final String ACTIVITY_DEPLOY_DIRECTORY = "master/autoimport/activity/deploy";

  /**
   * Watched subfolder for importing new resources.
   */
  public static final String RESOURCE_IMPORT_DIRECTORY = "master/autoimport/resource/import";

  /**
   * The script service to use for the automation master.
   */
  private ScriptService scriptService;

  /**
   * The scheduling service to use for the automation master.
   */
  private SchedulerService schedulerService;

  /**
   * The controller repository to use for the automation master.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The activity repository to use for the automation master.
   */
  private ActivityRepository activityRepository;

  /**
   * The activity controller manager to use for the automation master.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * The Master API activity manager to use for the automation master.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API controller manager to use for the automation master.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API resource manager to use for the automation master.
   */
  private MasterApiResourceManager masterApiResourceManager;

  /**
   * The Master API master support manager to use for the automation master.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * Smart Spaces environment being run in.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * A directory watcher for imports.
   */
  private DirectoryWatcher importDirectoryWatcher;

  /**
   * The map of bindings that every automation invocation will receive.
   */
  private Map<String, Object> automationBindings;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void startup() {
    prepareImportDirectoryWatcher();
    prepareAutomationBindings();
    schedulerService.addSchedulingEntities(automationBindings);
  }

  @Override
  public void shutdown() {
    if (importDirectoryWatcher != null) {
      importDirectoryWatcher.shutdown();
      importDirectoryWatcher = null;
    }
  }

  @Override
  public Set<String> getScriptingLanguages() {
    return scriptService.getLanguageNames();
  }

  @Override
  public Map<String, Object> getAutomationBindings() {
    return automationBindings;
  }

  @Override
  public void runScript(NamedScript script) {

    try {
      scriptService.executeScriptByName(script.getLanguage(),
          new StringScriptSource(script.getContent()), automationBindings);
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Error while running script", e);
    }
  }

  /**
   * Prepare the directory watcher for automatic import of activities.
   */
  private void prepareImportDirectoryWatcher() {
    importDirectoryWatcher = new SimpleDirectoryWatcher();
    importDirectoryWatcher.addDirectory(fileSupport.newFile(
        spaceEnvironment.getFilesystem().getInstallDirectory(), ACTIVITY_IMPORT_DIRECTORY));
    importDirectoryWatcher.addDirectory(fileSupport.newFile(
        spaceEnvironment.getFilesystem().getInstallDirectory(), ACTIVITY_DEPLOY_DIRECTORY));
    importDirectoryWatcher.addDirectory(fileSupport.newFile(
        spaceEnvironment.getFilesystem().getInstallDirectory(), RESOURCE_IMPORT_DIRECTORY));
    importDirectoryWatcher.addDirectoryWatcherListener(new BaseDirectoryWatcherListener() {
      @Override
      public void onFileAdded(File file) {
        handleImportFileAdded(file);
      }
    });
    importDirectoryWatcher.startup(spaceEnvironment, DIRECTORY_SCAN_TIME, TimeUnit.SECONDS);
  }

  /**
   * Prepare the bindings for automation.
   */
  private void prepareAutomationBindings() {
    automationBindings = new HashMap<>();
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVITY_REPOSITORY, activityRepository);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_CONTROLLER_REPOSITORY,
        spaceControllerRepository);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCRIPT_SERVICE, scriptService);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCHEDULER_SERVICE, schedulerService);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVE_SPACE_CONTROLLER_MANAGER,
        activeSpaceControllerManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_ACTIVITY_MANAGER,
        masterApiActivityManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_RESOURCE_MANAGER,
        masterApiResourceManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_SPACE_CONTROLLER_MANAGER,
        masterApiSpaceControllerManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_MASTER_SUPPORT_MANAGER,
        masterApiMasterSupportManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_ENVIRONMENT, spaceEnvironment);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_AUTOMATION_MANAGER, this);
  }

  /**
   * An activity file has been added to the scanned folders.
   *
   * @param file
   *          the folder which has been added
   */
  private void handleImportFileAdded(File file) {
    spaceEnvironment.getLog().formatInfo("Import file  %s found in autoinput folders", file);

    String watchedFolder = file.getParent();

    FileInputStream importStream = null;
    try {
      importStream = new FileInputStream(file);
      if (watchedFolder.endsWith(RESOURCE_IMPORT_DIRECTORY)) {
        Map<String, Object> resourceResponse =
            masterApiResourceManager.saveResource(null, importStream);

      } else {
        Map<String, Object> activityResponse =
            masterApiActivityManager.saveActivity(null, importStream);

        if (MasterApiMessageSupport.isSuccessResponse(activityResponse)) {
          if (watchedFolder.endsWith(ACTIVITY_DEPLOY_DIRECTORY)) {
            masterApiSpaceControllerManager.deployAllLiveActivityInstances(
                (String) MasterApiMessageSupport.getResponseDataMap(activityResponse)
                    .get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID));
          }
        }
      }
    } catch (Throwable e) {
      spaceEnvironment.getLog().formatError(e, "Could not read imported file %s", file.getAbsolutePath());
    } finally {
      Closeables.closeQuietly(importStream);

      file.delete();
    }

  }

  /**
   * @param scriptService
   *          the scriptService to set
   */
  public void setScriptService(ScriptService scriptService) {
    this.scriptService = scriptService;
  }

  /**
   * @param schedulerService
   *          the schedulerService to set
   */
  public void setSchedulerService(SchedulerService schedulerService) {
    this.schedulerService = schedulerService;
  }

  /**
   * @param controllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository controllerRepository) {
    this.spaceControllerRepository = controllerRepository;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void
      setActiveSpaceControllerManager(ActiveSpaceControllerManager activeControllerManager) {
    this.activeSpaceControllerManager = activeControllerManager;
  }

  /**
   * @param masterApiActivityManager
   *          the uiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * @param masterApiControllerManager
   *          the uiControllerManager to set
   */
  public void setMasterApiSpaceControllerManager(
      MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }

  /**
   * @param masterApiResourceManager
   *          the masterApiResourceManager to set
   */
  public void setMasterApiResourceManager(MasterApiResourceManager masterApiResourceManager) {
    this.masterApiResourceManager = masterApiResourceManager;
  }

  /**
   * @param masterApiMasterSupportManager
   *          the uiMasterSupportManager to set
   */
  public void setMasterApiMasterSupportManager(
      MasterApiMasterSupportManager masterApiMasterSupportManager) {
    this.masterApiMasterSupportManager = masterApiMasterSupportManager;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
