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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.Space;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.SpaceControllerConfiguration;
import io.smartspaces.domain.basic.SpaceControllerMode;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.api.master.MasterApiSpaceControllerManager;
import io.smartspaces.master.api.master.MasterApiUtilities;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.master.server.services.internal.DataBundleState;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.master.server.services.model.ActiveSpaceController;
import io.smartspaces.messaging.dynamic.SmartSpacesMessages;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;
import io.smartspaces.spacecontroller.SpaceControllerState;

import com.google.common.collect.Lists;

import scala.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A master API manager for space controllers.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiSpaceControllerManager extends BaseMasterApiManager
    implements MasterApiSpaceControllerManager {

  /**
   * Repository for obtaining controller entities.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Repository for obtaining activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Handle operations on remote controllers.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * Master API manager for activity operations.
   */
  private InternalMasterApiActivityManager masterApiActivityManager;

  @Override
  public Map<String, Object> getSpaceControllersByFilter(String filter) {
    List<Map<String, Object>> responseData = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<SpaceController> spaceControllers =
          Lists.newArrayList(spaceControllerRepository.getSpaceControllers(filterExpression));
      Collections.sort(spaceControllers, MasterApiUtilities.SPACE_CONTROLLER_BY_NAME_COMPARATOR);
      for (ActiveSpaceController acontroller : activeSpaceControllerManager
          .getActiveSpaceControllers(spaceControllers)) {
        Map<String, Object> controllerData = new HashMap<>();

        SpaceController controller = acontroller.spaceController();
        getSpaceControllerMasterApiData(controller, controllerData);

        responseData.add(controllerData);
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Attempt to get activity data failed", e);

      return SmartSpacesMessagesSupport
          .getFailureResponse(MasterApiMessages.MESSAGE_API_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> getSpaceControllerAllView() {
    List<Map<String, Object>> data = new ArrayList<>();

    List<SpaceController> spaceControllers = spaceControllerRepository.getSpaceControllers(null);
    Collections.sort(spaceControllers, MasterApiUtilities.SPACE_CONTROLLER_BY_NAME_COMPARATOR);
    for (ActiveSpaceController acontroller : activeSpaceControllerManager
        .getActiveSpaceControllers(spaceControllers)) {
      Map<String, Object> controllerData = new HashMap<>();

      SpaceController controller = acontroller.spaceController();
      getSpaceControllerMasterApiData(controller, controllerData);
      getActiveSpaceControllerMasterApiData(acontroller, controllerData);

      data.add(controllerData);
    }

    return SmartSpacesMessagesSupport.getSuccessResponse(data);
  }

  @Override
  public Map<String, Object> getSpaceControllerFullView(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      Map<String, Object> responseData = new HashMap<>();

      Map<String, Object> controllerData = new HashMap<>();

      getSpaceControllerMasterApiData(controller, controllerData);

      ActiveSpaceController acontroller =
          activeSpaceControllerManager.getActiveSpaceController(controller);
      getActiveSpaceControllerMasterApiData(acontroller, controllerData);

      responseData.put("spacecontroller", controllerData);

      List<Map<String, Object>> liveActivities =
          masterApiActivityManager.getAllUiLiveActivitiesByController(controller);
      responseData.put("liveactivities", liveActivities);

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> getSpaceControllerView(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      Map<String, Object> controllerData = new HashMap<>();

      getSpaceControllerMasterApiData(controller, controllerData);

      return SmartSpacesMessagesSupport.getSuccessResponse(controllerData);
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> getSpaceControllerConfiguration(String typedId) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (spaceController != null) {
      Map<String, String> data = new HashMap<>();

      SpaceControllerConfiguration config = spaceController.getConfiguration();
      if (config != null) {
        for (ConfigurationParameter parameter : config.getParameters()) {
          data.put(parameter.getName(), parameter.getValue());
        }
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(data);
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> setSpaceControllerConfiguration(String typedId, Map<String, String> map) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (spaceController != null) {
      if (saveSpaceControllerConfiguration(spaceController, map)) {
        spaceControllerRepository.saveSpaceController(spaceController);
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> configureSpaceController(String typedId) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (spaceController != null) {
      activeSpaceControllerManager.configureSpaceController(spaceController);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  /**
   * Get the new configuration into the space controller.
   *
   * @param spaceController
   *          the space controller being configured
   * @param map
   *          the map representing the new configuration
   *
   * @return {@code true} if there was a change to the configuration
   */
  private boolean saveSpaceControllerConfiguration(SpaceController spaceController,
      Map<String, String> map) {
    SpaceControllerConfiguration configuration = spaceController.getConfiguration();
    if (configuration != null) {
      return mergeParameters(map, configuration);
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty()) {
        return false;
      }

      newSpaceControllerConfiguration(spaceController, map);

      return true;
    }
  }

  /**
   * merge the values in the map with the configuration.
   *
   * @param map
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the
   *         configuration
   */
  private boolean mergeParameters(Map<String, String> map,
      SpaceControllerConfiguration configuration) {
    boolean changed = false;

    Map<String, ConfigurationParameter> existingMap = configuration.getParameterMap();

    // Delete all items removed
    for (Entry<String, ConfigurationParameter> entry : existingMap.entrySet()) {
      if (!map.containsKey(entry.getKey())) {
        changed = true;

        configuration.removeParameter(entry.getValue());
      }
    }

    // Now everything in the submitted map will be check. if the name exists
    // in the old configuration, we will try and change the value. if the
    // name doesn't exist, add it.
    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = existingMap.get(entry.getKey());
      if (parameter != null) {
        // Existed
        String oldValue = parameter.getValue();
        if (!oldValue.equals(entry.getValue())) {
          changed = true;
          parameter.setValue(entry.getValue());
        }
      } else {
        // Didn't exist
        changed = true;

        parameter = activityRepository.newActivityConfigurationParameter();
        parameter.setName(entry.getKey());
        parameter.setValue(entry.getValue());

        configuration.addParameter(parameter);
      }

    }
    return changed;
  }

  /**
   * Create a new configuration for a space controller.
   *
   * @param spaceController
   *          the space controller
   * @param map
   *          the new configuration
   */
  private void newSpaceControllerConfiguration(SpaceController spaceController,
      Map<String, String> map) {
    SpaceControllerConfiguration configuration =
        spaceControllerRepository.newSpaceControllerConfiguration();
    spaceController.setConfiguration(configuration);

    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter =
          spaceControllerRepository.newSpaceControllerConfigurationParameter();
      parameter.setName(entry.getKey());
      parameter.setValue(entry.getValue());

      configuration.addParameter(parameter);
    }
  }

  @Override
  public Map<String, Object> updateSpaceControllerMetadata(String typedId, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      SpaceController spaceController = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (spaceController == null) {
        return getNoSuchSpaceControllerResponse(typedId);
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement = (Map<String, Object>) metadataCommand
            .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        spaceController.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = spaceController.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications = (Map<String, Object>) metadataCommand
            .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        spaceController.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = spaceController.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        spaceController.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_API_COMMAND_UNKNOWN,
            String.format("Unknown space controller metadata update command %s", command));
      }

      spaceControllerRepository.saveSpaceController(spaceController);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Could not modify space controller metadata", e);

      return SmartSpacesMessagesSupport
          .getFailureResponse(MasterApiMessages.MESSAGE_API_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> deleteSpaceController(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      spaceControllerRepository.deleteSpaceController(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> connectToAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.connectSpaceController(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog().error(String.format("Unable to connect to controller %s (%s)",
            controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> disconnectFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.disconnectSpaceController(controller, false);
      } catch (Throwable e) {
        spaceEnvironment.getLog().error(String.format("Unable to disconnect to controller %s (%s)",
            controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.statusSpaceController(controller, false);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to get the status from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> forceStatusFromAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.statusSpaceController(controller, true);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to force the status from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        try {
          activeSpaceControllerManager.shutdownSpaceController(controller);
        } catch (Throwable e) {
          spaceEnvironment.getLog().formatError(e, "Unable to shut down controller %s (%s)",
              controller.getUuid(), controller.getName());
        }
      } else {
        spaceEnvironment.getLog().formatError("Unknown controller %s", typedId);

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> hardRestartSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        try {
          activeSpaceControllerManager.hardRestartSpaceController(controller);
        } catch (Throwable e) {
          spaceEnvironment.getLog().error(String.format("Unable to hard restart controller %s (%s)",
              controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().formatError("Unknown controller %s", typedId);

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> softRestartSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        try {
          activeSpaceControllerManager.softRestartSpaceController(controller);
        } catch (Throwable e) {
          spaceEnvironment.getLog().error(String.format("Unable to soft restart controller %s (%s)",
              controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().formatError("Unknown controller %s", typedId);

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> connectToSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        try {
          activeSpaceControllerManager.connectSpaceController(controller);
        } catch (Throwable e) {
          spaceEnvironment.getLog().error(String.format("Unable to shut down controller %s (%s)",
              controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", typedId));

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> disconnectFromSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        try {
          activeSpaceControllerManager.disconnectSpaceController(controller, false);
        } catch (Throwable e) {
          spaceEnvironment.getLog()
              .error(String.format("Unable to disconnect from controller %s (%s)",
                  controller.getUuid(), controller.getName()), e);
        }
      } else {
        spaceEnvironment.getLog().error(String.format("Unknown controller %s", typedId));

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.shutdownSpaceController(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog().formatError(e, "Unable to shut down controller %s (%s)",
            controller.getUuid(), controller.getName());
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> hardRestartAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.hardRestartSpaceController(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog().formatError(e, "Unable to hard restart controller %s (%s)",
            controller.getUuid(), controller.getName());
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> softRestartAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.softRestartSpaceController(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog().formatError(e, "Unable to soft restart controller %s (%s)",
            controller.getUuid(), controller.getName());
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> statusSpaceControllers(List<String> typedIds) {
    for (String typedId : typedIds) {
      SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
      if (controller != null) {
        activeSpaceControllerManager.statusSpaceController(controller, false);
      } else {
        spaceEnvironment.getLog()
            .error(String.format("Attempted status of unknown controller %s", typedId));

        return getNoSuchSpaceControllerResponse(typedId);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerTempData(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerTempData(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerTempDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerTempData(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to clean temp data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerPermanentData(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerPermanentData(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerPermanentDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerPermanentData(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to clean permanent data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesTempData(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerActivitiesTempData(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesTempDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerActivitiesTempData(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to clean all temp data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesPermanentData(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to clean all permanent data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> captureDataSpaceController(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.captureSpaceControllerDataBundle(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> restoreDataSpaceController(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.restoreSpaceControllerDataBundle(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> captureDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.captureSpaceControllerDataBundle(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to capture data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> restoreDataAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.restoreSpaceControllerDataBundle(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to capture data from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> shutdownAllLiveActivities(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      activeSpaceControllerManager.shutdownAllActivities(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> shutdownAllLiveActivitiesAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.shutdownAllActivities(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to shut down all live activities from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> deployAllLiveActivitiesSpaceController(String typedId) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerByTypedId(typedId);
    if (controller != null) {
      deployAllActivitysForSpaceController(controller);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceControllerResponse(typedId);
    }
  }

  /**
   * Deploy all activities found on a given controller.
   *
   * @param controller
   *          the controller
   */
  private void deployAllActivitysForSpaceController(SpaceController controller) {
    for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByController(controller)) {
      activeSpaceControllerManager.deployLiveActivity(liveActivity);
    }
  }

  @Override
  public Map<String, Object> deployAllLiveActivitiesAllSpaceControllers() {
    for (SpaceController controller : getAllEnabledSpaceControllers()) {
      try {
        activeSpaceControllerManager.shutdownAllActivities(controller);
      } catch (Throwable e) {
        spaceEnvironment.getLog()
            .error(String.format("Unable to deploy all live activities from controller %s (%s)",
                controller.getUuid(), controller.getName()), e);
      }
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  @Override
  public Map<String, Object> deployAllActivityLiveActivities(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      for (LiveActivity liveActivity : activityRepository.getLiveActivitiesByActivity(activity)) {
        activeSpaceControllerManager.deployLiveActivity(liveActivity);
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchActivityResponse(id);
    }
  }

  @Override
  public Map<String, Object> deleteLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.deleteLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> deployLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.deployLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> configureLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.configureLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> startupLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.startupLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> activateLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.activateLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.deactivateLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.shutdownLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> statusLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.statusLiveActivity(liveActivity);

      Map<String, Object> statusData = new HashMap<>();

      masterApiActivityManager.getLiveActivityStatusApiData(liveActivity, statusData);

      return SmartSpacesMessagesSupport.getSuccessResponse(statusData);
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanLiveActivityPermanentData(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.cleanLiveActivityPermanentData(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> cleanLiveActivityTempData(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activeSpaceControllerManager.cleanLiveActivityTempData(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> deployLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.deployLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> configureLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.configureLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> startupLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.startupLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> activateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.activateLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> deactivateLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.deactivateLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> shutdownLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activeSpaceControllerManager.shutdownLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> forceShutdownLiveActivitiesLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {

      for (LiveActivityGroupLiveActivity gla : group.getLiveActivities()) {
        activeSpaceControllerManager.shutdownLiveActivity(gla.getLiveActivity());
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> statusLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {

      for (LiveActivityGroupLiveActivity gla : group.getLiveActivities()) {
        statusLiveActivity(gla.getLiveActivity().getId());
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> liveActivityStatusSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space == null) {

      Set<String> liveActivityIds = new HashSet<>();
      statusSpace(space, liveActivityIds);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  /**
   * Request status for all live activities in the space and all subspaces.
   *
   * <p>
   * A given live activity will only be queried once even if in multiple
   * activity groups.
   *
   * @param space
   *          the space to examine
   * @param liveActivityIds
   *          IDs of all live activities which have had their status requested
   *          so far
   */
  private void statusSpace(Space space, Set<String> liveActivityIds) {
    for (LiveActivityGroup group : space.getActivityGroups()) {
      for (LiveActivityGroupLiveActivity gla : group.getLiveActivities()) {
        String id = gla.getLiveActivity().getId();
        if (liveActivityIds.add(id)) {
          statusLiveActivity(id);
        }
      }
    }

    for (Space subspace : space.getSpaces()) {
      statusSpace(subspace, liveActivityIds);
    }
  }

  @Override
  public Map<String, Object> deploySpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.deploySpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> configureSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.configureSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }

  }

  @Override
  public Map<String, Object> startupSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.startupSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> shutdownSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.shutdownSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> activateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.activateSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> deactivateSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activeSpaceControllerManager.deactivateSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> statusSpace(String id) {
    try {
      Space space = activityRepository.getSpaceById(id);
      if (space != null) {

        Map<String, Object> response = generateSpaceStatusApiResponse(space);

        return SmartSpacesMessagesSupport.getSuccessResponse(response);
      } else {
        return getNoSuchSpaceResponse(id);
      }
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Could not modify activity metadata", e);

      return SmartSpacesMessagesSupport
          .getFailureResponse(MasterApiMessages.MESSAGE_API_CALL_FAILURE, e);
    }
  }

  /**
   * Create the Master API status object for a space.
   *
   * <p>
   * This will include all subspaces, live activity groups, and the live
   * activities contained in the groups.
   *
   * @param space
   *          the space to get the status for
   *
   * @return the Master API status object
   */
  private Map<String, Object> generateSpaceStatusApiResponse(Space space) {
    Map<String, Object> data = new HashMap<>();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, space.getId());
    data.put("subspaces", generateSubSpacesStatusesApiResponse(space));
    data.put("liveActivityGroups", generateLiveActivityGroupsStatusesApiResponse(space));

    return data;
  }

  /**
   * Get a list of Master API status objects for all subspaces of a space.
   *
   * @param space
   *          the space containing the subspaces
   *
   * @return a list for all subspace Master API status objects
   */
  private List<Map<String, Object>> generateSubSpacesStatusesApiResponse(Space space) {
    List<Map<String, Object>> subspaces = new ArrayList<>();

    for (Space subspace : space.getSpaces()) {
      subspaces.add(generateSpaceStatusApiResponse(subspace));
    }

    return subspaces;
  }

  /**
   * Get a list of Master API status objects for all live activity groups in a
   * space.
   *
   * @param space
   *          the space containing the subspaces
   *
   * @return a list for all group Master API status objects
   */
  private List<Map<String, Object>> generateLiveActivityGroupsStatusesApiResponse(Space space) {
    List<Map<String, Object>> groups = new ArrayList<>();

    for (LiveActivityGroup group : space.getActivityGroups()) {
      groups.add(generateLiveActivityGroupStatusApiResponse(group));
    }
    return groups;
  }

  /**
   * Generate the Master API response data for a live activity group.
   *
   * @param group
   *          the live activity group
   *
   * @return the master API response data
   */
  private Map<String, Object> generateLiveActivityGroupStatusApiResponse(LiveActivityGroup group) {
    Map<String, Object> result = new HashMap<>();

    result.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, group.getId());
    result.put("liveactivities", generateLiveActivitiesStatusesApiResponse(group));

    return result;
  }

  /**
   * Get a list of Master API status objects for all live activities in a space.
   *
   * @param group
   *          the group containing the live activities
   *
   * @return a list for all live activity Master API status objects
   */
  private List<Map<String, Object>>
      generateLiveActivitiesStatusesApiResponse(LiveActivityGroup group) {
    List<Map<String, Object>> activities = new ArrayList<>();

    for (LiveActivityGroupLiveActivity activity : group.getLiveActivities()) {
      activities.add(generateApiLiveActivityStatus(activity.getLiveActivity()));
    }

    return activities;
  }

  /**
   * Get the Master API status object for the given live activity.
   *
   * @param liveActivity
   *          the live activity
   *
   * @return the Master API status object
   */
  private Map<String, Object> generateApiLiveActivityStatus(LiveActivity liveActivity) {
    ActiveLiveActivity active = activeSpaceControllerManager.getActiveLiveActivity(liveActivity);

    Map<String, Object> response = new HashMap<>();

    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
    response.put("status", active.getRuntimeState().getDescription());

    return response;
  }

  /**
   * Get all enabled space controllers, which are ones that are not marked
   * disabled or otherwise should not be contacted for normal "all" operations.
   *
   * @return list of enabled space controllers
   */
  private List<SpaceController> getAllEnabledSpaceControllers() {
    List<SpaceController> allControllers = spaceControllerRepository.getAllSpaceControllers();
    List<SpaceController> liveControllers =
        Lists.newArrayListWithExpectedSize(allControllers.size());
    for (SpaceController controller : allControllers) {
      if (SpaceControllerMode.isControllerEnabled(controller)) {
        liveControllers.add(controller);
      }
    }
    return liveControllers;
  }

  /**
   * Get the Master API data for a controller.
   *
   * @param controller
   *          the space controller
   * @param controllerData
   *          where the data should be stored
   */
  private void getSpaceControllerMasterApiData(SpaceController controller,
      Map<String, Object> controllerData) {
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, controller.getId());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID,
        controller.getUuid());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME,
        controller.getName());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION,
        controller.getDescription());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA,
        controller.getMetadata());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_HOSTID,
        controller.getHostId());
    controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_HOST_NAME,
        controller.getHostName());
    controllerData.put(
        MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_HOST_CONTROL_PORT,
        controller.getHostControlPort());

    SpaceControllerMode mode = controller.getMode();
    if (mode != null) {
      controllerData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_MODE,
          mode.name());
      controllerData.put(
          MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_MODE_DESCRIPTION,
          mode.getDescription());
    }
  }

  /**
   * Get the Master API data for an active controller.
   *
   * @param controller
   *          the space controller
   * @param controllerData
   *          where the data should be stored
   */
  private void getActiveSpaceControllerMasterApiData(ActiveSpaceController controller,
      Map<String, Object> controllerData) {
    SpaceControllerState state = controller.getState();
    controllerData.put("state", state);
    controllerData.put("stateDescription", state.getDescription());
    Option<Date> lastStateUpdateDate = controller.getLastStateUpdateDate();
    controllerData.put("lastStateUpdateDate",
        lastStateUpdateDate.isDefined() ? lastStateUpdateDate.get().toString() : null);
    DataBundleState dataBundleState = controller.getDataBundleState();
    controllerData.put("dataBundleState", dataBundleState.name());
    controllerData.put("dataBundleStateDescription", dataBundleState.getDescription());

    Date lastDataBundleStateUpdateDate = controller.getLastDataBundleStateUpdateDate();
    controllerData.put("lastDataBundleStateUpdateDate",
        lastDataBundleStateUpdateDate != null ? lastDataBundleStateUpdateDate.toString() : null);
  }

  /**
   * Set the space controller repository to use.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * Set the activity repository to use.
   *
   * @param activityRepository
   *          the activity repository
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Set the active controller manager.
   *
   * @param activeControllerManager
   *          the active controller manager
   */
  public void
      setActiveSpaceControllerManager(ActiveSpaceControllerManager activeControllerManager) {
    this.activeSpaceControllerManager = activeControllerManager;
  }

  /**
   * Set the Master API manager for activity operations.
   *
   * @param masterApiActivityManager
   *          the Master API manager for activity operations
   */
  public void
      setMasterApiActivityManager(InternalMasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }
}
