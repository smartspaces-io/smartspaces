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

package io.smartspaces.spacecontroller.runtime;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.activity.ActivityStatus;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeleteResponse.LiveActivityDeleteStatus;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import io.smartspaces.container.data.SpaceControllerInformationValidator;
import io.smartspaces.domain.basic.pojo.SimpleSpaceController;
import io.smartspaces.liveactivity.runtime.LiveActivityRunner;
import io.smartspaces.liveactivity.runtime.LiveActivityRuntime;
import io.smartspaces.liveactivity.runtime.LiveActivityStatusPublisher;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.spacecontroller.SpaceController;
import io.smartspaces.spacecontroller.SpaceControllerStatus;
import io.smartspaces.spacecontroller.resource.deployment.ContainerResourceDeploymentManager;
import io.smartspaces.spacecontroller.runtime.configuration.SpaceControllerConfigurationManager;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;
import io.smartspaces.tasks.SequentialTaskQueue;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.uuid.JavaUuidGenerator;
import io.smartspaces.util.uuid.UuidGenerator;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of {@link SpaceController} which gives basic
 * implementation. Does not supply communication for the remote master.
 *
 * @author Keith M. Hughes
 */
public class StandardSpaceController extends BaseSpaceController implements SpaceControllerControl,
    LiveActivityStatusPublisher {

  /**
   * The amount of time to wait for shutting down the controller if some startup
   * error happens, in milliseconds.
   */
  public static final int CONTROLLER_ERROR_SHUTDOWN_WAIT_TIME = 5000;

  /**
   * An operation detail message when a live activity cannot be deleted because
   * it is still running.
   */
  public static final String OPERATION_DETAIL_DELETION_FAIL_LIVE_ACTIVITY_RUNNING =
      "The live activity is running";

  /**
   * The default number of milliseconds the controllerHeartbeat thread delays
   * between beats.
   */
  public static final int HEARTBEAT_DELAY_DEFAULT = 10000;

  /**
   * The heartbeatLoop for this controller.
   */
  private SpaceControllerHeartbeat controllerHeartbeat;

  /**
   * Control for the controllerHeartbeat.
   */
  private ScheduledFuture<?> controllerHeartbeatControl;

  /**
   * Number of milliseconds the heartbeatLoop waits before each beat.
   */
  private final long heartbeatDelay = HEARTBEAT_DELAY_DEFAULT;

  /**
   * Manager for space controller data operations.
   */
  private final SpaceControllerDataBundleManager dataBundleManager;

  /**
   * The runtime for live activities.
   */
  private LiveActivityRuntime liveActivityRuntime;

  /**
   * The Smart Spaces system controller.
   */
  private final SmartSpacesSystemControl spaceSystemControl;

  /**
   * The controller communicator for remote control.
   */
  private final SpaceControllerCommunicator spaceControllerCommunicator;

  /**
   * The sequential task queue to be used for controller tasks.
   */
  private SequentialTaskQueue eventQueue;

  /**
   * {@code true} if the controller was started up.
   */
  private volatile boolean startedUp = false;

  /**
   * The persister for information about the controller.
   */
  private final SpaceControllerInfoPersister controllerInfoPersister;

  /**
   * The configuration manager for the controller.
   */
  private final SpaceControllerConfigurationManager spaceControllerConfigurationManager;

  /**
   * File control for the controller.
   *
   * <p>
   * This can be {@code null} if it wasn't requested.
   */
  private SpaceControllerFileControl fileControl;

  /**
   * Activity installer for the controller.
   */
  private SpaceControllerActivityInstallationManager spaceControllerActivityInstallManager;

  /**
   * The container resource deployment manager.
   */
  private final ContainerResourceDeploymentManager containerResourceDeploymentManager;

  /**
   * Support for file operations.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new StandardSpaceController.
   *
   * @param spaceControllerActivityInstaller
   *          activity installer
   * @param containerResourceDeploymentManager
   *          manager for deploying container resources
   * @param spaceControllerCommunicator
   *          the communicator for something controlling the controller
   * @param spaceControllerInfoPersister
   *          the persister for controller information
   * @param spaceSystemControl
   *          the system control for the container
   * @param dataBundleManager
   *          the manager for data bundle operations
   * @param spaceControllerConfigurationManager
   *          configuration manager for the space controller
   * @param liveActivityRuntime
   *          the live activity runtime for the controller
   * @param taskQueue
   *          the task queue for the controller
   * @param spaceEnvironment
   *          the space environment to use
   */
  public StandardSpaceController(
      SpaceControllerActivityInstallationManager spaceControllerActivityInstaller,
      ContainerResourceDeploymentManager containerResourceDeploymentManager,
      SpaceControllerCommunicator spaceControllerCommunicator,
      SpaceControllerInfoPersister spaceControllerInfoPersister,
      SmartSpacesSystemControl spaceSystemControl,
      SpaceControllerDataBundleManager dataBundleManager,
      SpaceControllerConfigurationManager spaceControllerConfigurationManager,
      LiveActivityRuntime liveActivityRuntime, SequentialTaskQueue taskQueue,
      SmartSpacesEnvironment spaceEnvironment) {
    super(spaceEnvironment);

    this.spaceControllerActivityInstallManager = spaceControllerActivityInstaller;
    this.containerResourceDeploymentManager = containerResourceDeploymentManager;
    this.spaceControllerConfigurationManager = spaceControllerConfigurationManager;

    this.dataBundleManager = dataBundleManager;
    dataBundleManager.setSpaceController(this);

    this.spaceControllerCommunicator = spaceControllerCommunicator;
    spaceControllerCommunicator.setSpaceControllerControl(this);

    this.controllerInfoPersister = spaceControllerInfoPersister;

    this.spaceSystemControl = spaceSystemControl;

    this.liveActivityRuntime = liveActivityRuntime;
    liveActivityRuntime.setLiveActivityStatusPublisher(this);

    this.eventQueue = taskQueue;
  }

  @Override
  public void startup() {
    super.startup();

    confirmUuid();

    final Log log = getSpaceEnvironment().getLog();

    spaceControllerCommunicator.onStartup(getControllerInfo());

    controllerHeartbeat = spaceControllerCommunicator.newSpaceControllerHeartbeat();
    controllerHeartbeatControl =
        getSpaceEnvironment().getExecutorService().scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            try {
              controllerHeartbeat.sendHeartbeat();
            } catch (Exception e) {
              log.error("Exception while trying to send a Space Controller heartbeat", e);
            }
          }
        }, heartbeatDelay, heartbeatDelay, TimeUnit.MILLISECONDS);

    dataBundleManager.startup();

    startupControllerControl();

    startupAutostartActivities();

    spaceControllerCommunicator.registerControllerWithMaster(getControllerInfo());

    startedUp = true;
  }

  /**
   * Make sure the controller had a UUID. If not, generate one.
   */
  private void confirmUuid() {
    SimpleSpaceController spaceControllerInfo = getControllerInfo();

    SpaceControllerInformationValidator spaceControllerInformationValidator =
        new SpaceControllerInformationValidator();
    StringBuilder errorBuilder =
        spaceControllerInformationValidator.checkControllerInfoForErrors(spaceControllerInfo,
            getSpaceEnvironment().getLog());
    if (errorBuilder.length() != 0) {
      // DO NOT LIKE THIS BUT HOW TO SHUT THE CONTAINER DOWN WITHOUT TONS OF
      // STARTUP ERRORS.
      getSpaceEnvironment().getExecutorService().schedule(new Runnable() {
        @Override
        public void run() {
          spaceSystemControl.shutdown();
        }
      }, CONTROLLER_ERROR_SHUTDOWN_WAIT_TIME, TimeUnit.MILLISECONDS);

      throw new SimpleSmartSpacesException(errorBuilder.toString());
    }

    String uuid = spaceControllerInfo.getUuid();
    if (uuid == null || uuid.trim().isEmpty()) {
      UuidGenerator uuidGenerator = new JavaUuidGenerator();
      uuid = uuidGenerator.newUuid();
      spaceControllerInfo.setUuid(uuid);

      getSpaceEnvironment().getLog().warn(
          String.format("No controller UUID found, generated UUID is %s", uuid));

      controllerInfoPersister.persist(spaceControllerInfo, getSpaceEnvironment());
    }
  }

  /**
   * Save the controller information in the configurations.
   */

  @Override
  public void shutdown() {
    super.shutdown();
    if (startedUp) {
      try {
        liveActivityRuntime.shutdown();

        if (fileControl != null) {
          fileControl.shutdown();
          fileControl = null;
        }

        shutdownAllLiveActivities();

        controllerHeartbeatControl.cancel(true);
        controllerHeartbeatControl = null;

        dataBundleManager.shutdown();

        spaceControllerCommunicator.onShutdown();
      } finally {
        startedUp = false;
      }
    }
  }

  /**
   * Startup the activities that need to start up when the controller starts.
   */
  private void startupAutostartActivities() {
    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
      switch (activity.getRuntimeStartupType()) {
        case STARTUP:
          startupLiveActivity(activity.getUuid());
          break;
        case ACTIVATE:
          activateLiveActivity(activity.getUuid());
          break;
        case READY:
          break;
        default:
          getSpaceEnvironment().getLog().error(
              String.format("Unknown startup type %s for activity %s/%s",
                  activity.getRuntimeStartupType(), activity.getIdentifyingName(),
                  activity.getUuid()));
      }
    }
  }

  @Override
  public void startupAllLiveActivities() {
    liveActivityRuntime.startupAllActivities();
  }

  @Override
  public void shutdownAllLiveActivities() {
    liveActivityRuntime.shutdownAllActivities();
  }

  @Override
  public void configureController(Map<String, String> configuration) {
    getSpaceEnvironment().getLog().info("Configuring the space controller");
    getSpaceEnvironment().getLog().info(configuration);

    spaceControllerConfigurationManager.update(configuration);
  }

  @Override
  public void captureControllerDataBundle(final String bundleUri) {
    getSpaceEnvironment().getLog().info("Capture controller data bundle");
    getSpaceEnvironment().getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        executeCaptureControllerDataBundle(bundleUri);
      }
    });
  }

  /**
   * Execute a capture controller data bundle request from the master. This will
   * trigger the data content push back to the master, and send off a
   * success/failure message.
   *
   * @param bundleUri
   *          Uri bundle to send the captured data bundle to.
   */
  private void executeCaptureControllerDataBundle(final String bundleUri) {
    try {
      dataBundleManager.captureControllerDataBundle(bundleUri);
      spaceControllerCommunicator.publishControllerDataStatus(
          SpaceControllerDataOperation.DATA_CAPTURE, SpaceControllerStatus.SUCCESS, null);
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error("Error capturing data bundle", e);
      spaceControllerCommunicator.publishControllerDataStatus(
          SpaceControllerDataOperation.DATA_CAPTURE, SpaceControllerStatus.FAILURE, e);
    }
  }

  @Override
  public void restoreControllerDataBundle(final String bundleUri) {
    getSpaceEnvironment().getLog().info("Restore controller data bundle");
    getSpaceEnvironment().getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        executeRestoreControllerDataBundle(bundleUri);
      }
    });
  }

  /**
   * Execute the restore data bundle operation. Will trigger the restore, and
   * send back a success/failure message.
   *
   * @param bundleUri
   *          URI for the data bundle to be fetched from
   */
  private void executeRestoreControllerDataBundle(final String bundleUri) {
    try {
      dataBundleManager.restoreControllerDataBundle(bundleUri);
      spaceControllerCommunicator.publishControllerDataStatus(
          SpaceControllerDataOperation.DATA_RESTORE, SpaceControllerStatus.SUCCESS, null);
    } catch (Exception e) {
      getSpaceEnvironment().getLog().error("Error restoring data bundle", e);
      spaceControllerCommunicator.publishControllerDataStatus(
          SpaceControllerDataOperation.DATA_RESTORE, SpaceControllerStatus.FAILURE, e);
    }
  }

  @Override
  public void startupLiveActivity(String uuid) {
    liveActivityRuntime.startupLiveActivity(uuid);
  }

  @Override
  public void shutdownLiveActivity(final String uuid) {
    liveActivityRuntime.shutdownLiveActivity(uuid);
  }

  @Override
  public void statusLiveActivity(String uuid) {
    liveActivityRuntime.statusLiveActivity(uuid);
  }

  @Override
  public void activateLiveActivity(String uuid) {
    liveActivityRuntime.activateLiveActivity(uuid);
  }

  @Override
  public void deactivateLiveActivity(String uuid) {
    liveActivityRuntime.deactivateLiveActivity(uuid);
  }

  @Override
  public void configureLiveActivity(String uuid, Map<String, String> configuration) {
    liveActivityRuntime.configureLiveActivity(uuid, configuration);
  }

  @Override
  public void cleanLiveActivityTmpData(String uuid) {
    liveActivityRuntime.cleanLiveActivityTmpData(uuid);
  }

  @Override
  public void cleanLiveActivityPermanentData(String uuid) {
    liveActivityRuntime.cleanLiveActivityPermanentData(uuid);
  }

  @Override
  public void cleanControllerTempData() {
    getSpaceEnvironment().getLog().info("Cleaning controller temp directory");

    fileSupport.deleteDirectoryContents(getSpaceEnvironment().getFilesystem().getTempDirectory());
  }

  @Override
  public void cleanControllerPermanentData() {
    getSpaceEnvironment().getLog().info("Cleaning controller permanent directory");

    fileSupport.deleteDirectoryContents(getSpaceEnvironment().getFilesystem().getDataDirectory());
  }

  @Override
  public void cleanControllerTempDataAll() {
    getSpaceEnvironment().getLog().info("Cleaning all live activity temporary data directories");

    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
      cleanLiveActivityTmpData(activity.getUuid());
    }
  }

  @Override
  public void cleanControllerPermanentDataAll() {
    getSpaceEnvironment().getLog().info("Cleaning all live activity permanent data directories");

    for (InstalledLiveActivity activity : getAllInstalledLiveActivities()) {
      cleanLiveActivityPermanentData(activity.getUuid());
    }
  }

  @Override
  public LiveActivityRunner getLiveActivityRunnerByUuid(String uuid) {
    return liveActivityRuntime.getLiveActivityRunnerByUuid(uuid);
  }

  @Override
  public void publishActivityStatus(final String uuid, final ActivityStatus status) {
    eventQueue.addTask(new Runnable() {
      @Override
      public void run() {
        spaceControllerCommunicator.publishActivityStatus(uuid, status);
      }
    });
  }

  @Override
  public void shutdownControllerContainer() {
    spaceSystemControl.shutdown();
  }

  @Override
  public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
    return liveActivityRuntime.getAllInstalledLiveActivities();
  }

  /**
   * Potentially start up any controller control points.
   */
  private void startupControllerControl() {
    boolean startupFileControl =
        getSpaceEnvironment().getSystemConfiguration().getRequiredPropertyBoolean(
            SmartSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE);
    if (startupFileControl) {
      fileControl = new SpaceControllerFileControl(this, spaceSystemControl, getSpaceEnvironment());
      fileControl.startup();
    }
  }

  @Override
  public ContainerResourceDeploymentQueryResponse handleContainerResourceDeploymentQueryRequest(
      ContainerResourceDeploymentQueryRequest request) {
    return containerResourceDeploymentManager.queryResources(request);
  }

  @Override
  public ContainerResourceDeploymentCommitResponse handleContainerResourceDeploymentCommitRequest(
      ContainerResourceDeploymentCommitRequest request) {
    return containerResourceDeploymentManager.commitResources(request);
  }

  @Override
  public LiveActivityDeploymentResponse installLiveActivity(LiveActivityDeploymentRequest request) {
    return spaceControllerActivityInstallManager.handleDeploymentRequest(request);
  }

  @Override
  public LiveActivityDeleteResponse deleteLiveActivity(LiveActivityDeleteRequest request) {
    String uuid = request.getUuid();
    if (liveActivityRuntime.isLiveActivityRunning(uuid)) {
      getSpaceEnvironment().getLog().error(
          String
              .format("Attempt to delete live activity %s failed, live activity is running", uuid));

      return new LiveActivityDeleteResponse(uuid, LiveActivityDeleteStatus.FAILURE,
          getSpaceEnvironment().getTimeProvider().getCurrentTime(),
          OPERATION_DETAIL_DELETION_FAIL_LIVE_ACTIVITY_RUNNING);
    } else {
      return spaceControllerActivityInstallManager.handleDeleteRequest(request);
    }
  }

  /**
   * Set the file support to be used.
   *
   * <p>
   * This is for testing and no other reason.
   *
   * @param fileSupport
   *          the file support to use
   */
  @VisibleForTesting
  void setFileSupport(FileSupport fileSupport) {
    this.fileSupport = fileSupport;
  }
}
