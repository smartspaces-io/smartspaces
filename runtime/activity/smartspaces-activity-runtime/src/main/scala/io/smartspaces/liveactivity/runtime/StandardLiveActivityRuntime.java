/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityControl;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.ActivityListener;
import io.smartspaces.activity.ActivityState;
import io.smartspaces.activity.ActivityStateTransition;
import io.smartspaces.activity.ActivityStatus;
import io.smartspaces.activity.BaseActivityRuntime;
import io.smartspaces.activity.configuration.ActivityConfiguration;
import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.alert.AlertStatusManager;
import io.smartspaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import io.smartspaces.liveactivity.runtime.configuration.LiveActivityConfigurationManager;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationListener;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationManager;
import io.smartspaces.liveactivity.runtime.installation.ActivityInstallationManager.RemoveActivityResult;
import io.smartspaces.liveactivity.runtime.logging.LiveActivityLogFactory;
import io.smartspaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import io.smartspaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesFilesystem;
import io.smartspaces.tasks.SequentialTaskQueue;
import io.smartspaces.util.statemachine.simplegoal.SimpleGoalStateTransition;
import io.smartspaces.util.statemachine.simplegoal.SimpleGoalStateTransition.TransitionResult;
import io.smartspaces.util.statemachine.simplegoal.SimpleGoalStateTransitioner;
import io.smartspaces.util.statemachine.simplegoal.SimpleGoalStateTransitionerCollection;

/**
 * The standard implementation of a {@link LiveActivityRuntime}.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRuntime extends BaseActivityRuntime
    implements LiveActivityRuntime, LiveActivityRunnerListener {

  /**
   * The factory for live activity runners.
   */
  private LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * Local repository of live activity information.
   */
  private LocalLiveActivityRepository liveActivityRepository;

  /**
   * Receives activities deployed to the runtime.
   */
  private ActivityInstallationManager activityInstallationManager;

  /**
   * All live activity runners in this controller, indexed by UUID.
   */
  private final Map<String, LiveActivityRunner> liveActivityRunners = new HashMap<>();

  /**
   * Sampler for live activity runners for this controller.
   */
  private LiveActivityRunnerSampler liveActivityRunnerSampler;

  /**
   * Log factory for activities.
   */
  private LiveActivityLogFactory activityLogFactory;

  /**
   * The configuration manager for activities.
   */
  private LiveActivityConfigurationManager configurationManager;

  /**
   * The publisher for live activity statuses.
   */
  private LiveActivityStatusPublisher liveActivityStatusPublisher;

  /**
   * The storage manager for activities.
   */
  private LiveActivityStorageManager liveActivityStorageManager;

  /**
   * All activity state transitioners.
   */
  private SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl> activityStateTransitioners;

  /**
   * The sequential task queue to be used for controller tasks.
   */
  private SequentialTaskQueue taskQueue;

  /**
   * For important alerts worthy of paging, etc.
   */
  private AlertStatusManager alertStatusManager;

  /**
   * The component factory for the runtime.
   */
  private LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory;

  /**
   * A listener for activity events.
   */
  private final ActivityListener activityListener = new ActivityListener() {
    @Override
    public void onActivityStatusChange(Activity activity, ActivityStatus oldStatus,
        ActivityStatus newStatus) {
      handleActivityListenerOnActivityStatusChange(activity, oldStatus, newStatus);
    }
  };

  /**
   * A listener for installation events.
   */
  private final ActivityInstallationListener activityInstallationListener =
      new ActivityInstallationListener() {
        @Override
        public void onActivityInstall(String uuid) {
          handleActivityInstall(uuid);
        }

        @Override
        public void onActivityRemove(String uuid, RemoveActivityResult result) {
          handleActivityRemove(uuid, result);
        }
      };

  /**
   * The runtime listeners for this runtime.
   */
  private List<LiveActivityRuntimeListener> runtimeListeners = Lists.newCopyOnWriteArrayList();

  /**
   * The debug service.
   */
  private RemoteLiveActivityRuntimeMonitorService runtimeDebugService;

  /**
   * Construct a new runtime.
   *
   * @param liveActivityRuntimeComponentFactory
   *          the component factory for live activity runtimes
   * @param liveActivityRepository
   *          the repository for live activities
   * @param activityInstallationManager
   *          the installation manager for new live activities
   * @param activityLogFactory
   *          the log factory for live activities
   * @param configurationManager
   *          the configuration manager for live activities
   * @param activityStorageManager
   *          the storage manager for live activities
   * @param alertStatusManager
   *          the alerting manager for live activities
   * @param taskQueue
   *          the task queue to use
   * @param runtimeDebugService
   *          the remote debug service to use
   * @param spaceEnvironment
   *          the space environment to run under
   */
  public StandardLiveActivityRuntime(
      LiveActivityRuntimeComponentFactory liveActivityRuntimeComponentFactory,
      LocalLiveActivityRepository liveActivityRepository,
      ActivityInstallationManager activityInstallationManager,
      LiveActivityLogFactory activityLogFactory,
      LiveActivityConfigurationManager configurationManager,
      LiveActivityStorageManager activityStorageManager, AlertStatusManager alertStatusManager,
      SequentialTaskQueue taskQueue, RemoteLiveActivityRuntimeMonitorService runtimeDebugService,
      SmartSpacesEnvironment spaceEnvironment) {
    super(liveActivityRuntimeComponentFactory.newNativeActivityRunnerFactory(),
        liveActivityRuntimeComponentFactory.newActivityComponentFactory(), spaceEnvironment);
    this.liveActivityRuntimeComponentFactory = liveActivityRuntimeComponentFactory;
    this.liveActivityRunnerFactory =
        liveActivityRuntimeComponentFactory.newLiveActivityRunnerFactory();
    this.liveActivityRepository = liveActivityRepository;
    this.activityInstallationManager = activityInstallationManager;
    this.activityLogFactory = activityLogFactory;
    this.configurationManager = configurationManager;
    this.liveActivityStorageManager = activityStorageManager;
    this.alertStatusManager = alertStatusManager;
    this.taskQueue = taskQueue;

    liveActivityRunnerSampler =
        new SimpleLiveActivityRunnerSampler(spaceEnvironment, spaceEnvironment.getLog());
    this.runtimeDebugService = runtimeDebugService;
    runtimeDebugService.setLiveActivityRuntime(this);
  }

  @Override
  public void addRuntimeListener(LiveActivityRuntimeListener listener) {
    runtimeListeners.add(listener);
  }

  @Override
  public void removeRuntimeListener(LiveActivityRuntimeListener listener) {
    runtimeListeners.remove(listener);
  }

  @Override
  public void startup() {
    activityStateTransitioners =
        new SimpleGoalStateTransitionerCollection<ActivityState, ActivityControl>();

    activityInstallationManager.addActivityInstallationListener(activityInstallationListener);

    liveActivityRunnerSampler.startup();
    liveActivityRunnerFactory.startup();
    runtimeDebugService.startup();

    liveActivityRuntimeComponentFactory
        .registerCoreServices(getSpaceEnvironment().getServiceRegistry());

    for (LiveActivityRuntimeListener listener : runtimeListeners) {
      try {
        listener.onLiveActivityRuntimeStartup(this);
      } catch (Throwable e) {
        getSpaceEnvironment().getExtendedLog().formatError(e,
            "Error on live activity runtime onStartup call");
      }
    }
  }

  @Override
  public void shutdown() {
    try {
      if (liveActivityRunnerSampler != null) {
        liveActivityRunnerSampler.shutdown();
        liveActivityRunnerSampler = null;
      }

      liveActivityRunnerFactory.shutdown();

      runtimeDebugService.shutdown();

      shutdownAllActivities();

      activityStateTransitioners.clear();

      liveActivityRuntimeComponentFactory
          .unregisterCoreServices(getSpaceEnvironment().getServiceRegistry());
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error while shutting down live activity runtime");
    }

    // Should signal shutdown regardless of whether the shutdown failed.
    for (LiveActivityRuntimeListener listener : runtimeListeners) {
      try {
        listener.onLiveActivityRuntimeStartup(this);
      } catch (Throwable e) {
        getSpaceEnvironment().getExtendedLog()
            .formatError("Error on live activity runtime onStartup call");
      }
    }
  }

  @Override
  public void startupAllActivities() {
    for (LiveActivityRunner app : getAllActiveActivities()) {
      attemptActivityStartup(app);
    }
  }

  @Override
  public void shutdownAllActivities() {
    getSpaceEnvironment().getExtendedLog().info("Shutting down all activities");

    for (LiveActivityRunner app : getAllActiveActivities()) {
      attemptActivityShutdown(app);
    }
  }

  @Override
  public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
    return liveActivityRepository.getAllInstalledLiveActivities();
  }

  @Override
  public void startupLiveActivity(String uuid) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, true);
      if (liveActivityRunner != null) {
        getSpaceEnvironment().getExtendedLog().info(
            String.format("Starting up live activity: %s", liveActivityRunner.getDisplayName()));
        ActivityStatus status = liveActivityRunner.getCachedActivityStatus();
        if (!status.getState().isRunning()) {
          attemptActivityStartup(liveActivityRunner);
        } else {
          // The activity is running so just report what it is doing
          publishActivityStatus(uuid, status);
        }
      } else {
        getSpaceEnvironment().getExtendedLog().warn(String
            .format("Startup of live activity failed, does not exist on controller: %s", uuid));

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during startup of live activity: %s", uuid);
      ActivityStatus status = new ActivityStatus(ActivityState.STARTUP_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void shutdownLiveActivity(final String uuid) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, false);
      if (liveActivityRunner != null) {
        getSpaceEnvironment().getExtendedLog().formatInfo("Shutting down activity: %s",
            liveActivityRunner.getDisplayName());
        attemptActivityShutdown(liveActivityRunner);
      } else {
        // The activity hasn't been active. Make sure it really exists
        // then
        // send that it is ready.
        InstalledLiveActivity liveActivity =
            liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
        if (liveActivity != null) {
          getSpaceEnvironment().getExtendedLog().formatInfo(
              "Shutting down activity (wasn't running): %s", liveActivity.getDisplayName());
          publishActivityStatus(uuid, LiveActivityRunner.INITIAL_ACTIVITY_STATUS);
        } else {
          // TODO(keith): Tell master the controller doesn't exist.
          getSpaceEnvironment().getExtendedLog().formatError(
              "Startup of live activity failed, does not exist on controller: %s", uuid);

          ActivityStatus status =
              new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
          publishActivityStatus(uuid, status);
        }
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during shutdown of live activity: %s", uuid);

      ActivityStatus status = new ActivityStatus(ActivityState.SHUTDOWN_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void statusLiveActivity(String uuid) {
    LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, false);
    if (liveActivityRunner != null) {
      getSpaceEnvironment().getExtendedLog().formatInfo("Getting status of live activity: %s",
          liveActivityRunner.getDisplayName());
      final ActivityStatus activityStatus = liveActivityRunner.sampleActivityStatus();
      getSpaceEnvironment().getExtendedLog().formatInfo("Reporting live activity status %s: %s",
          activityStatus, liveActivityRunner.getDisplayName());
      publishActivityStatus(liveActivityRunner.getUuid(), activityStatus);
    } else {
      InstalledLiveActivity liveActivity =
          liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
      if (liveActivity != null) {
        getSpaceEnvironment().getExtendedLog().formatInfo("Reporting live activity status %s: %s",
            LiveActivityRunner.INITIAL_ACTIVITY_STATUS, liveActivity.getDisplayName());
        publishActivityStatus(uuid, LiveActivityRunner.INITIAL_ACTIVITY_STATUS);
      } else {
        getSpaceEnvironment().getExtendedLog()
            .formatError("Status of live activity failed, does not exist on controller: %s", uuid);

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    }
  }

  @Override
  public void activateLiveActivity(String uuid) {
    try {
      // Can create since can immediately request activate
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, true);
      if (liveActivityRunner != null) {
        getSpaceEnvironment().getExtendedLog().formatInfo("Activating live activity: %s",
            liveActivityRunner.getDisplayName());
        attemptActivityActivate(liveActivityRunner);
      } else {
        getSpaceEnvironment().getExtendedLog().formatError(
            "Activation of live activity failed, does not exist on controller: %s", uuid);

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during activation of live activity: %s", uuid);

      ActivityStatus status = new ActivityStatus(ActivityState.ACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void deactivateLiveActivity(String uuid) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, false);
      if (liveActivityRunner != null) {
        getSpaceEnvironment().getExtendedLog().formatInfo("Deactivating live activity: %s",
            liveActivityRunner.getDisplayName());

        attemptActivityDeactivate(liveActivityRunner);
      } else {
        getSpaceEnvironment().getExtendedLog().formatError(
            "Deactivation of live activity failed, does not exist on controller: %s", uuid);

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during deactivation of live activity: %s", uuid);

      ActivityStatus status = new ActivityStatus(ActivityState.DEACTIVATE_FAILURE, e.getMessage());
      publishActivityStatus(uuid, status);
    }
  }

  @Override
  public void configureLiveActivity(String uuid, Map<String, String> configuration) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid, true);
      if (liveActivityRunner != null) {
        getSpaceEnvironment().getExtendedLog().formatInfo("Configuring live activity: %s",
            liveActivityRunner.getDisplayName());

        liveActivityRunner.updateConfiguration(configuration);
      } else {
        getSpaceEnvironment().getExtendedLog().formatError(
            "Configuration of live activity failed, does not exist on controller: %s", uuid);

        ActivityStatus status =
            new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
        publishActivityStatus(uuid, status);
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during configuration of live activity: %s", uuid);

      // TODO(keith): An appropriate status to send back?
    }
  }

  @Override
  public void cleanLiveActivityTmpData(String uuid) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid);
      if (liveActivityRunner != null) {
        if (!liveActivityRunner.getCachedActivityStatus().getState().isRunning()) {
          getSpaceEnvironment().getExtendedLog().formatInfo(
              "Cleaning live activity tmp directory for live activity: %s",
              liveActivityRunner.getDisplayName());

          liveActivityStorageManager.cleanTmpActivityDataDirectory(uuid);
        } else {
          getSpaceEnvironment().getExtendedLog().formatWarn(
              "Ignoring attempt to clean activity tmp directory for a running activity: %s",
              liveActivityRunner.getDisplayName());
        }
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during cleaning live activity tmp data directory: %s", uuid);

      // TODO(keith): An appropriate status to send back?
    }
  }

  @Override
  public void cleanLiveActivityPermanentData(String uuid) {
    try {
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(uuid);
      if (liveActivityRunner != null) {
        if (!liveActivityRunner.getCachedActivityStatus().getState().isRunning()) {
          getSpaceEnvironment().getExtendedLog().formatInfo(
              "Cleaning activity permanent directory for live activity: %s",
              liveActivityRunner.getDisplayName());

          liveActivityStorageManager.cleanPermanentActivityDataDirectory(uuid);
        } else {
          getSpaceEnvironment().getExtendedLog().formatWarn(
              "Ignoring attempt to clean activity permanent data directory for a running live activity: %s",
              liveActivityRunner.getDisplayName());
        }
      }
    } catch (Throwable e) {
      getSpaceEnvironment().getExtendedLog().formatError(e,
          "Error during cleaning live activity permanent data directory: %s", uuid);

      // TODO(keith): An appropriate status to send back?
    }
  }

  @Override
  public void initializeActivityInstance(InstalledLiveActivity installedActivity,
      ActivityFilesystem activityFilesystem, Activity instance, Configuration configuration,
      Log activityLog, ActivityExecutionContext executionContext) {
    // Set log first to enable logging of any configuration/startup errors.
    instance.setLog(activityLog);

    String uuid = installedActivity.getUuid();
    instance.setActivityRuntime(this);
    instance.setUuid(uuid);

    instance.setConfiguration(configuration);
    instance.setActivityFilesystem(activityFilesystem);
    instance.setSpaceEnvironment(getSpaceEnvironment());
    instance.setExecutionContext(executionContext);

    initializeActivityConfiguration(configuration, activityFilesystem);

    onActivityInitialization(instance);
  }

  /**
   * Perform any needed initialization from the runtime on an activity instance.
   *
   * @param instance
   *          the activity instance
   */
  private void onActivityInitialization(Activity instance) {
    instance.addActivityListener(activityListener);
  }

  @Override
  public void onNoInstanceActivityStatusEvent(LiveActivityRunner liveActivityRunner) {
    // The only thing that should come in here are errors
    ActivityStatus status = liveActivityRunner.getCachedActivityStatus();
    if (status.getState().isError()) {
      publishActivityStatus(liveActivityRunner.getUuid(), status);
      alertStatusManager.announceLiveActivityStatus(liveActivityRunner);
    } else {
      getSpaceEnvironment().getExtendedLog().formatWarn(
          "A live activity liveActivityRunner for live activity %s has a no instance status event that isn't an error: %s",
          liveActivityRunner.getUuid(), status);
    }
  }

  @Override
  public Log getActivityLog(InstalledLiveActivity installedActivity, Configuration configuration,
      ActivityFilesystem activityFilesystem) {
    return activityLogFactory.createLogger(installedActivity,
        configuration.getPropertyString(ActivityConfiguration.CONFIGURATION_PROPERTY_LOG_LEVEL,
            SmartSpacesEnvironment.LOG_LEVEL_ERROR),
        activityFilesystem);
  }

  @Override
  public void releaseActivityLog(Log activityLog) {
    activityLogFactory.releaseLog(activityLog);
  }

  /**
   * Initialize the configuration with any special values needed for running.
   *
   * @param configuration
   *          the configuration to be modified
   * @param activityFilesystem
   *          the activities file system
   */
  private void initializeActivityConfiguration(Configuration configuration,
      ActivityFilesystem activityFilesystem) {
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_INSTALL,
        activityFilesystem.getInstallDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_LOG,
        activityFilesystem.getLogDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_DATA,
        activityFilesystem.getPermanentDataDirectory().getAbsolutePath());
    configuration.setValue(ActivityConfiguration.CONFIGURATION_ACTIVITY_FILESYSTEM_DIR_TMP,
        activityFilesystem.getTempDataDirectory().getAbsolutePath());

    // TODO(keith): Move to smartspaces-system during bootstrap
    SmartSpacesFilesystem filesystem = getSpaceEnvironment().getFilesystem();
    configuration.setValue(SmartSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_DATA,
        filesystem.getDataDirectory().getAbsolutePath());
    configuration.setValue(SmartSpacesEnvironment.CONFIGURATION_SYSTEM_FILESYSTEM_DIR_TMP,
        filesystem.getTempDirectory().getAbsolutePath());
  }

  /**
   * Got a status change on an activity from the activity.
   *
   * @param activity
   *          the activity whose status changed
   * @param oldStatus
   *          the old status
   * @param newStatus
   *          the new status
   */
  private void handleActivityListenerOnActivityStatusChange(final Activity activity,
      final ActivityStatus oldStatus, final ActivityStatus newStatus) {
    // TODO(keith): Android hates garbage collection. This may need an
    // object
    // pool.
    taskQueue.addTask(new Runnable() {
      @Override
      public void run() {
        handleActivityStateChange(activity, oldStatus, newStatus);
      }
    });
  }

  @Override
  public LiveActivityRunner getLiveActivityRunnerByUuid(String uuid) {
    return getLiveActivityRunnerByUuid(uuid, false);
  }

  @Override
  public boolean hasLiveActivitiesRunning() {
    synchronized (liveActivityRunners) {
      for (LiveActivityRunner liveActivityRunner : liveActivityRunners.values()) {
        if (isLiveActivityRunning(liveActivityRunner)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean isLiveActivityRunning(String uuid) {
    synchronized (liveActivityRunners) {
      LiveActivityRunner liveActivityRunner = liveActivityRunners.get(uuid);
      return liveActivityRunner != null && isLiveActivityRunning(liveActivityRunner);
    }
  }

  /**
   * Is the given live activity runner running?
   *
   * @param liveActivityRunner
   *          the runner to check
   *
   * @return {@code true} if the runner is officially running
   */
  private boolean isLiveActivityRunning(LiveActivityRunner liveActivityRunner) {
    ActivityState state = liveActivityRunner.getCachedActivityStatus().getState();
    return state.isRunning() || state.isTransitional();
  }

  /**
   * Get an activity runner by UUID.
   *
   * @param uuid
   *          the UUID of the activity
   * @param create
   *          {@code true} if should create the activity entry from the
   *          controller repository if none found, {@code false} otherwise.
   *
   * @return the runner for the activity with the given UUID, {@code null} if no
   *         such activity
   */
  @VisibleForTesting
      LiveActivityRunner getLiveActivityRunnerByUuid(String uuid, boolean create) {
    LiveActivityRunner liveActivityRunner = null;
    synchronized (liveActivityRunners) {
      liveActivityRunner = liveActivityRunners.get(uuid);
      if (liveActivityRunner == null && create) {
        liveActivityRunner = newLiveActivityRunner(uuid);

        if (liveActivityRunner != null) {
          addLiveActivityRunner(uuid, liveActivityRunner);
        }
      }
    }

    if (liveActivityRunner == null) {
      getSpaceEnvironment().getExtendedLog()
          .formatWarn("Could not find live activity runner with uuid %s", uuid);
    }

    return liveActivityRunner;
  }

  /**
   * Handle the status change from an activity.
   *
   * @param activity
   *          the activity whose status changed
   * @param oldStatus
   *          the old status
   * @param newStatus
   *          the new status
   */
  private void handleActivityStateChange(Activity activity, ActivityStatus oldStatus,
      ActivityStatus newStatus) {
    ActivityState newState = newStatus.getState();
    boolean error = newState.isError();

    // Want the log before anything else is tried.
    if (error) {
      getSpaceEnvironment().getExtendedLog().formatError(newStatus.getException(),
          "Error for live activity %s, state is now %s", activity.getUuid(), newState);
    } else {
      getSpaceEnvironment().getExtendedLog().formatInfo("Live activity %s, state is now %s",
          activity.getUuid(), newState);
    }

    // If went from not running to running we need to watch the activity
    if (!oldStatus.getState().isRunning() && newState.isRunning()) {
      liveActivityRunnerSampler
          .startSamplingRunner(getLiveActivityRunnerByUuid(activity.getUuid()));
    } else if (!newState.isRunning() && !newState.isTransitional()) {
      // If the activity went from a not running state and tried to
      // transition
      // to a not running state
      LiveActivityRunner liveActivityRunner = getLiveActivityRunnerByUuid(activity.getUuid());

      // Paranoia check to see if was a runner.
      if (liveActivityRunner != null) {
        liveActivityRunner.getActivityWrapper().done();
      }
    }

    publishActivityStatus(activity.getUuid(), newStatus);

    if (error) {
      alertStatusManager
          .announceLiveActivityStatus(getLiveActivityRunnerByUuid(activity.getUuid()));
    } else {
      activityStateTransitioners.transition(activity.getUuid(), newState);
    }
  }

  /**
   * Add in a new active activity.
   *
   * @param uuid
   *          uuid of the activity
   * @param liveActivityRunner
   *          the live activity runner
   */
  @VisibleForTesting
      void addLiveActivityRunner(String uuid, LiveActivityRunner liveActivityRunner) {
    liveActivityRunners.put(uuid, liveActivityRunner);
  }

  /**
   * Create anew live activity runner from the repository.
   *
   * @param uuid
   *          UUID of the activity to create a runner for
   *
   * @return the new live activity runner
   */
  private LiveActivityRunner newLiveActivityRunner(String uuid) {
    InstalledLiveActivity liveActivity =
        liveActivityRepository.getInstalledLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      InternalLiveActivityFilesystem activityFilesystem =
          liveActivityStorageManager.getActivityFilesystem(uuid);

      LiveActivityConfiguration activityConfiguration =
          configurationManager.newLiveActivityConfiguration(liveActivity, activityFilesystem);
      activityConfiguration.load();

      LiveActivityRunner liveActivityRunner = liveActivityRunnerFactory.newLiveActivityRunner(
          liveActivity, activityFilesystem, activityConfiguration, this, this);

      return liveActivityRunner;
    } else {
      return null;
    }
  }

  /**
   * Make sure that the runner is not stale. if it is, create a new one.
   *
   * @param liveActivityRunner
   *          the runner to check
   *
   * @return a runner for the activity that is not stale
   */
  private LiveActivityRunner ensureRunnerNotStale(LiveActivityRunner liveActivityRunner) {
    synchronized (liveActivityRunners) {
      if (liveActivityRunner.isStale()) {
        String uuid = liveActivityRunner.getUuid();
        liveActivityRunner = newLiveActivityRunner(uuid);
        liveActivityRunners.put(uuid, liveActivityRunner);
      }

      return liveActivityRunner;
    }
  }

  /**
   * Start up an activity.
   *
   * @param liveActivityRunner
   *          the runner for the live activity to start up
   */
  private void attemptActivityStartup(LiveActivityRunner liveActivityRunner) {
    String uuid = liveActivityRunner.getUuid();
    getSpaceEnvironment().getExtendedLog().formatInfo("Attempting startup of activity %s", uuid);

    liveActivityRunner = ensureRunnerNotStale(liveActivityRunner);

    attemptActivityStateTransition(liveActivityRunner, ActivityStateTransition.STARTUP,
        "Attempt to startup live activity %s which was running, sending RUNNING");
  }

  /**
   * Attempt to shut an activity down.
   *
   * @param activity
   *          the activity to shutdown
   */
  private void attemptActivityShutdown(LiveActivityRunner activity) {
    attemptActivityStateTransition(activity, ActivityStateTransition.SHUTDOWN,
        "Attempt to shutdown live activity %s which wasn't running, sending READY");
  }

  /**
   * Attempt to activate an activity.
   *
   * @param activity
   *          the activity to activate
   */
  private void attemptActivityActivate(LiveActivityRunner activity) {
    if (ActivityStateTransition.STARTUP
        .canTransition(activity.sampleActivityStatus().getState()) == TransitionResult.OK) {
      setupSequencedActivateTarget(activity);
    } else {
      attemptActivityStateTransition(activity, ActivityStateTransition.ACTIVATE,
          "Attempt to activate live activity %s which was activated, sending ACTIVE");
    }
  }

  /**
   * Need to set up a target of going to active after a startup.
   *
   * <p>
   * This will start moving towards the goal.
   *
   * @param liveActivityRunner
   *          the runner for the activity to go to startup
   */
  @SuppressWarnings("unchecked")
  private void setupSequencedActivateTarget(LiveActivityRunner liveActivityRunner) {
    liveActivityRunner = ensureRunnerNotStale(liveActivityRunner);

    String uuid = liveActivityRunner.getUuid();

    SimpleGoalStateTransitioner<ActivityState, ActivityControl> transitioner =
        new SimpleGoalStateTransitioner<ActivityState, ActivityControl>(liveActivityRunner,
            getSpaceEnvironment().getLog()).addTransitions(ActivityStateTransition.STARTUP,
                ActivityStateTransition.ACTIVATE);
    activityStateTransitioners.addTransitioner(uuid, transitioner);

    startupActivitySequence(liveActivityRunner, uuid);
  }

  /**
   * Startup an activity sequence.
   *
   * @param liveActivityRunner
   *          the runner for the live activity
   * @param uuid
   *          the UUID for the live activity
   */
  private void startupActivitySequence(final LiveActivityRunner liveActivityRunner,
      final String uuid) {
    // TODO(keith): Android hates garbage collection. This may need an
    // object
    // pool.
    taskQueue.addTask(new Runnable() {
      @Override
      public void run() {
        activityStateTransitioners.transition(uuid,
            liveActivityRunner.sampleActivityStatus().getState());
      }
    });
  }

  /**
   * Attempt to deactivate an activity.
   *
   * @param activity
   *          the activity to deactivate
   */
  private void attemptActivityDeactivate(LiveActivityRunner activity) {
    attemptActivityStateTransition(activity, ActivityStateTransition.DEACTIVATE,
        "Attempt to deactivate live activity %s which wasn't activated, sending RUNNING");
  }

  /**
   * Attempt to do a state transition on an activity.
   *
   * @param liveActivityRunner
   *          the runner for the activity
   * @param transition
   *          the transition to take place
   * @param noopMessage
   *          the message to log if this is a no-op
   *
   * @return {@code true} if the transition actually happened
   */
  private boolean attemptActivityStateTransition(LiveActivityRunner liveActivityRunner,
      SimpleGoalStateTransition<ActivityState, ActivityControl> transition, String noopMessage) {
    TransitionResult transitionResult = transition.attemptTransition(
        liveActivityRunner.sampleActivityStatus().getState(), liveActivityRunner);

    if (transitionResult == TransitionResult.OK) {
      return true;
    } else if (transitionResult == TransitionResult.ILLEGAL) {
      reportIllegalActivityStateTransition(liveActivityRunner, transition);
    } else if (transitionResult == TransitionResult.NOOP) {
      // If didn't do anything, report the message requested.
      getSpaceEnvironment().getExtendedLog()
          .warn(String.format(noopMessage, liveActivityRunner.getUuid()));
      publishActivityStatus(liveActivityRunner.getUuid(),
          liveActivityRunner.getCachedActivityStatus());
    } else {
      getSpaceEnvironment().getExtendedLog().formatWarn(
          "Unexpected activity state transition %s for live activity %s", transitionResult,
          liveActivityRunner.getUuid());
    }

    return false;
  }

  /**
   * Attempted an activity transition and it couldn't take place.
   *
   * @param activity
   *          the activity that was being transitioned
   * @param attemptedChange
   *          where the activity was going
   */
  private void reportIllegalActivityStateTransition(LiveActivityRunner activity,
      SimpleGoalStateTransition<ActivityState, ActivityControl> attemptedChange) {
    getSpaceEnvironment().getExtendedLog().formatError("Tried to %s activity %s, was in state %s",
        attemptedChange, activity.getUuid(), activity.getCachedActivityStatus().toString());
  }

  /**
   * Get a list of all activities running in the controller.
   *
   * <p>
   * Returned in no particular order. A new collection is made each time.
   *
   * @return All activities running in the controller.
   */
  public Collection<LiveActivityRunner> getAllActiveActivities() {
    // TODO(keith): Think about how this should be in the controller.
    synchronized (liveActivityRunners) {
      return Lists.newArrayList(liveActivityRunners.values());
    }
  }

  /**
   * Publish the status of a live activity.
   *
   * @param uuid
   *          uuid of the live activity
   * @param status
   *          the status of the live activity
   */
  private void publishActivityStatus(String uuid, ActivityStatus status) {
    liveActivityStatusPublisher.publishActivityStatus(uuid, status);
  }

  /**
   * The activity installer is signaling an install.
   *
   * @param uuid
   *          UUID of the installed activity.
   */
  private void handleActivityInstall(String uuid) {
    synchronized (liveActivityRunners) {
      LiveActivityRunner liveActivityRunner = liveActivityRunners.get(uuid);
      if (liveActivityRunner != null) {
        liveActivityRunner.markStale();
      }
    }
  }

  /**
   * The activity installer is signaling a removal.
   *
   * @param uuid
   *          UUID of the installed activity
   * @param result
   *          result of the removal
   */
  private void handleActivityRemove(String uuid, RemoveActivityResult result) {
    getSpaceEnvironment().getExtendedLog().formatInfo("Removed live activity %s", uuid);

    if (result == RemoveActivityResult.SUCCESS) {
      removeLiveActivityRunner(uuid);
    } else if (result == RemoveActivityResult.DOESNT_EXIST) {
      ActivityStatus status =
          new ActivityStatus(ActivityState.DOESNT_EXIST, "Activity does not exist");
      publishActivityStatus(uuid, status);
    }
  }

  /**
   * Remove a live activity runner from the known runners.
   *
   * <p>
   * Does nothing if the runner was not there.
   *
   * @param uuid
   *          UUID for the live activity
   */
  private void removeLiveActivityRunner(String uuid) {
    synchronized (liveActivityRunners) {
      liveActivityRunners.remove(uuid);
    }
  }

  @Override
  public void
      setLiveActivityStatusPublisher(LiveActivityStatusPublisher liveActivityStatusPublisher) {
    this.liveActivityStatusPublisher = liveActivityStatusPublisher;
  }

  /**
   * Set the alert status manager.
   *
   * @param alertStatusManager
   *          the alert status manager
   */
  @VisibleForTesting
      void setAlertStatusManager(AlertStatusManager alertStatusManager) {
    this.alertStatusManager = alertStatusManager;
  }

  /**
   * Get the activity listener used by the controller.
   *
   * @return the activity listener used by the controller
   */
  @VisibleForTesting
      ActivityListener getActivityListener() {
    return activityListener;
  }

  /**
   * Set the live activity runner sampler to use.
   *
   * @param liveActivityRunnerSampler
   *          the sampler to use
   */
  @VisibleForTesting
      void setLiveActivityRunnerSampler(LiveActivityRunnerSampler liveActivityRunnerSampler) {
    this.liveActivityRunnerSampler = liveActivityRunnerSampler;
  }

  @Override
  public LiveActivityRunnerFactory getLiveActivityRunnerFactory() {
    return liveActivityRunnerFactory;
  }

  @Override
  public LiveActivityStorageManager getLiveActivityStorageManager() {
    return liveActivityStorageManager;
  }
}
