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

import java.util.Map;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.container.data.SpaceControllerInformationValidator;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.master.server.remote.master.RemoteMasterCommunicationHandler;
import io.smartspaces.master.server.remote.master.RemoteMasterServerListener;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.system.SmartSpacesEnvironment;

/**
 * A bridge between the {@link RemoteMasterCommunicationHandler} and the master.
 *
 * @author Keith M. Hughes
 */
public class RemoteMasterServerBridge implements RemoteMasterServerListener {

  /**
   * The repository for controllers.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The active space controller manager.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * The space environment to use.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The logger for this bridge.
   */
  private ExtendedLog log;

  /**
   * The validator for space controller information.
   */
  private SpaceControllerInformationValidator spaceControllerInformationValidator =
      new SpaceControllerInformationValidator();

  @Override
  public void onSpaceControllerRegistration(SpaceController registeringController) {
    log.formatInfo("Got registering controller %s", registeringController);
    SpaceController spaceController =
        spaceControllerRepository.getSpaceControllerByUuid(registeringController.getUuid());
    if (spaceController == null) {
      spaceController = handleNeverBeforeSeenSpaceController(registeringController);
    } else {
      spaceController = handleExistingSpaceController(registeringController, spaceController);
    }

    potentiallyConnectToSpaceController(spaceController);
  }

  /**
   * Potentially connect to the space controller.
   * 
   * @param spaceController
   *          the space controller
   */
  private void potentiallyConnectToSpaceController(SpaceController spaceController) {
    if (spaceEnvironment.getSystemConfiguration().getPropertyBoolean(
        SmartSpacesEnvironment.CONFIGURATION_NAME_AUTOCONFIGURE,
        SmartSpacesEnvironment.CONFIGURATION_VALUE_DEFAULT_AUTOCONFIGURE)) {
      log.formatInfo("Autoconnecting to spacecontroller %s", spaceController.getUuid());
      activeSpaceControllerManager.connectSpaceController(spaceController);
    } else {
      log.formatInfo("No autoconnecting to spacecontroller %s", spaceController.getUuid());
    }
  }

  /**
   * Handle a space controller that has never been seen before.
   *
   * @param registeringController
   *          the information about the registering controller
   * 
   * @return the space controller placed in the repository
   */
  private SpaceController
      handleNeverBeforeSeenSpaceController(SpaceController registeringController) {
    // If it is a new controller, an instance must be created and stored in the
    // space controller repository.

    String registeringUuid = registeringController.getUuid();
    if (log.isInfoEnabled()) {
      log.formatInfo("Controller %s was unrecognized. Creating new record.", registeringUuid);
      log.formatInfo(
          "\tName: %s\n\tDescription: %s\n\tHost ID: %s\n\tHost Name: %s\n\tHost Control Port: %d",
          registeringController.getName(), registeringController.getDescription(),
          registeringController.getHostId(), registeringController.getHostName(),
          registeringController.getHostControlPort());
    }

    checkSpaceControllerInfo(registeringController);

    SpaceController newController =
        spaceControllerRepository.newSpaceController(registeringUuid, registeringController);

    return spaceControllerRepository.saveSpaceController(newController);
  }

  /**
   * Handle an existing space controller.
   *
   * @param registeringController
   *          the information coming in
   * @param existingController
   *          the existing controller pulled from the space controller
   *          repository
   * 
   * @return the finalcontroller
   */
  private SpaceController handleExistingSpaceController(SpaceController registeringController,
      SpaceController existingController) {
    // if the space controller exists, check to see if enough has changed to
    // warrant updating the controller from the repository.

    if (shouldRecordBeUpdated(registeringController, existingController)) {
      String registeringHostId = registeringController.getHostId();
      String registeringName = registeringController.getName();
      String registeringHostName = registeringController.getHostName();
      int registeringHostControlPort = registeringController.getHostControlPort();
      log.formatInfo(
          "Changing space controller data: Old Name: %s\tNew Name: %s\tOld Host ID: %s\tNew Host ID: %s",
          existingController.getName(), registeringName, existingController.getHostId(),
          registeringHostId);

      existingController.setHostId(registeringHostId);
      existingController.setName(registeringName);
      existingController.setHostId(registeringHostId);
      existingController.setHostName(registeringHostName);
      existingController.setHostControlPort(registeringHostControlPort);

      if (!compareField(existingController.getMetadata(), registeringController.getMetadata())) {
        existingController.setMetadata(registeringController.getMetadata());
      }

      return spaceControllerRepository.saveSpaceController(existingController);
    } else {
      return existingController;
    }
  }

  /**
   * Should a controller record be updated?
   *
   * @param registeringController
   *          the incoming controller information
   * @param existingController
   *          the existing controller information
   *
   * @return {@code true} if the existing controller record should be updated
   */
  private boolean shouldRecordBeUpdated(SpaceController registeringController,
      SpaceController existingController) {
    return !compareField(existingController.getHostName(), registeringController.getHostName())
        || existingController.getHostControlPort() != registeringController.getHostControlPort()
        || !compareField(existingController.getName(), registeringController.getName())
        || !compareField(existingController.getHostId(), registeringController.getHostId())
        || !compareField(existingController.getMetadata(), registeringController.getMetadata());
  }

  /**
   * Compare 2 objects that handles {@code null} correctly.
   * 
   * @param s1
   *          the first field
   * @param s2
   *          the second field
   * 
   * @return {@ code true} if the fields are equal
   */
  private boolean compareField(Object s1, Object s2) {
    if (s1 != null) {
      return s1.equals(s2);
    } else {
      return s2 == null;
    }
  }

  /**
   * Check to see if the incoming controller host ID information is valid.
   *
   * @param registeringController
   *          the controller information
   *
   * @throws SmartSpacesException
   *           the hostID was invalid
   */
  private void checkSpaceControllerInfo(SpaceController registeringController)
      throws SmartSpacesException {
    StringBuilder errorBuilder = spaceControllerInformationValidator
        .checkControllerInfoForErrors(registeringController, log);
    if (errorBuilder.length() != 0) {
      throw new SimpleSmartSpacesException(errorBuilder.toString());
    }
  }

  /**
   * Set the space controller repository.
   * 
   * @param spaceControllerRepository
   *          the repository to use
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * Set the space environment.
   * 
   * @param spaceEnvironment
   *          the environment to us
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Set the active space controller manager.
   * 
   * @param activeSpaceControllerManagere
   *          the manager to use
   */
  public void
      setActiveSpaceControllerManager(ActiveSpaceControllerManager activeSpaceControllerManager) {
    this.activeSpaceControllerManager = activeSpaceControllerManager;
  }

  /**
   * Set the log.
   * 
   * @param log
   *          the log to use
   */
  public void setLog(ExtendedLog log) {
    this.log = log;
  }
}
