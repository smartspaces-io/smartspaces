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

import io.smartspaces.activity.ActivityState;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse;
import io.smartspaces.container.control.message.activity.LiveActivityDeploymentResponse.ActivityDeployStatus;
import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.GroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.Space;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.SpaceControllerConfiguration;
import io.smartspaces.domain.basic.pojo.SimpleActivity;
import io.smartspaces.domain.basic.pojo.SimpleLiveActivity;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.api.master.MasterApiUtilities;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.event.BaseMasterEventListener;
import io.smartspaces.master.event.MasterEventListener;
import io.smartspaces.master.event.MasterEventManager;
import io.smartspaces.master.server.services.ActiveSpaceControllerManager;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.master.server.services.model.ActiveLiveActivity;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;
import io.smartspaces.messaging.dynamic.SmartSpacesMessages;
import io.smartspaces.resource.repository.ResourceRepositoryManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
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
 * Simple Master API manager for activity operations.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiActivityManager extends BaseMasterApiManager implements
    InternalMasterApiActivityManager {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for activities.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Repository manager for resources, like activities.
   */
  private ResourceRepositoryManager resourceRepositoryManager;

  /**
   * Manager for activity operations.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * Listener for master events.
   */
  private MasterEventListener masterEventListener = new BaseMasterEventListener() {
    @Override
    public void onLiveActivityDeploy(ActiveLiveActivity liveActivity,
        LiveActivityDeploymentResponse result, long timestamp) {
      handleLiveActivityInstallMasterEvent(liveActivity, result, timestamp);
    }
  };

  /**
   * The even manager for the master.
   */
  private MasterEventManager masterEventManager;

  @Override
  public void startup() {
    masterEventManager.addListener(masterEventListener);
  }

  @Override
  public void shutdown() {
    masterEventManager.removeListener(masterEventListener);
  }

  @Override
  public Map<String, Object> saveActivity(SimpleActivity activityDescription,
      InputStream activityContentStream) {
    try {
      Activity finalActivity = resourceRepositoryManager.addActivity(activityContentStream);

      return SmartSpacesMessagesSupport.getSuccessResponse(extractBasicActivityApiData(finalActivity));
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Attempt to import activity data failed", response);

      return response;
    } finally {
      try {
        Closeables.close(activityContentStream, true);
      } catch (IOException e) {
        // Won't ever be thrown.
      }
    }
  }

  @Override
  public Map<String, Object> getActivitiesByFilter(String filter) {
    List<Map<String, Object>> responseData = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<Activity> activities = activityRepository.getActivities(filterExpression);
      Collections.sort(activities, MasterApiUtilities.ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR);
      for (Activity activity : activities) {
        responseData.add(extractBasicActivityApiData(activity));
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Attempt to get activity data failed", response);

      return response;
    }
  }

  @Override
  public Map<String, Object> getActivityView(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      return SmartSpacesMessagesSupport.getSuccessResponse(extractBasicActivityApiData(activity));
    } else {
      return getNoSuchActivityResponse(id);
    }
  }

  @Override
  public Map<String, Object> getActivityFullView(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      Map<String, Object> fullView = new HashMap<>();

      Map<String, Object> activityData = extractBasicActivityApiData(activity);
      fullView.put("activity", activityData);
      extractActivityDependencyData(activity, activityData);
      fullView.put("liveactivities",
          extractLiveActivities(activityRepository.getLiveActivitiesByActivity(activity)));

      return SmartSpacesMessagesSupport.getSuccessResponse(fullView);
    } else {
      return getNoSuchActivityResponse(id);
    }
  }

  /**
   * Get basic information about an activity.
   *
   * @param activity
   *          the activity
   *
   * @return a Master API coded object giving the basic information
   */
  private Map<String, Object> extractBasicActivityApiData(Activity activity) {
    Map<String, Object> data = new HashMap<>();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, activity.getId());
    data.put("identifyingName", activity.getIdentifyingName());
    data.put("version", activity.getVersion());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, activity.getName());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION,
        activity.getDescription());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA, activity.getMetadata());
    data.put("lastUploadDate", activity.getLastUploadDate());
    data.put("lastStartDate", activity.getLastStartDate());
    data.put("bundleContentHash", activity.getBundleContentHash());

    return data;
  }

  /**
   * Add in the activity dependency data.
   *
   * @param activity
   *          the activity
   * @param activityData
   *          the dependencies
   */
  private void extractActivityDependencyData(Activity activity, Map<String, Object> activityData) {
    List<Map<String, Object>> dependencies = new ArrayList<>();
    activityData.put("dependencies", dependencies);

    List<? extends ActivityDependency> activityDependencies = activity.getDependencies();
    if (activityDependencies != null) {
      for (ActivityDependency activityDependency : activityDependencies) {
        Map<String, Object> dependencyData = new HashMap<>();
        dependencies.add(dependencyData);

        dependencyData.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_IDENTIFYING_NAME,
            activityDependency.getIdentifyingName());
        dependencyData.put("minimumVersion", activityDependency.getMinimumVersion());
        dependencyData.put("maximumVersion", activityDependency.getMaximumVersion());
        dependencyData.put("required", activityDependency.isRequired());
      }
    }
  }

  @Override
  public Map<String, Object> deleteActivity(String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      activityRepository.deleteActivity(activity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchActivityResponse(id);
    }
  }

  @Override
  public Map<String, Object> updateActivityMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      Activity activity = activityRepository.getActivityById(id);
      if (activity == null) {
        return getNoSuchActivityResponse(id);
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        activity.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        activity.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        activity.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_SPACE_COMMAND_UNKNOWN,
            String.format("Unknown activity metadata update command %s", command));
      }

      activityRepository.saveActivity(activity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Could not modify activity metadata", response);

      return response;
    }
  }

  @Override
  public Map<String, Object> getLiveActivitiesByFilter(String filter) {
    List<Map<String, Object>> responseData = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<LiveActivity> liveActivities = activityRepository.getLiveActivities(filterExpression);
      Collections.sort(liveActivities, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);
      for (LiveActivity activity : liveActivities) {
        Map<String, Object> activityData = new HashMap<>();

        extractLiveActivityApiData(activity, activityData);

        responseData.add(activityData);
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Attempt to get live activity data failed", response);

      return response;
    }
  }

  @Override
  public Map<String, Object> getLiveActivityView(String typedId) {
    LiveActivity liveactivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveactivity != null) {
      Map<String, Object> data = new HashMap<>();

      extractLiveActivityApiData(liveactivity, data);

      return SmartSpacesMessagesSupport.getSuccessResponse(data);
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> getLiveActivityFullView(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      Map<String, Object> responseData = new HashMap<>();

      Map<String, Object> liveActivityData = new HashMap<>();
      extractLiveActivityApiData(liveActivity, liveActivityData);

      responseData.put("liveactivity", liveActivityData);

      List<LiveActivityGroup> liveActivityGroups =
          Lists.newArrayList(activityRepository.getLiveActivityGroupsByLiveActivity(liveActivity));
      Collections.sort(liveActivityGroups,
          MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);
      responseData.put("liveActivityGroups", extractLiveActivityGroups(liveActivityGroups));

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public boolean canCreateLiveActivities() {
    return activityRepository.getNumberActivities() > 0
        && spaceControllerRepository.getNumberSpaceControllers() > 0;
  }

  @Override
  public Map<String, Object> deleteLiveActivity(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      activityRepository.deleteLiveActivity(liveActivity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> createLiveActivity(Map<String, Object> args) {
    if (!canCreateLiveActivities()) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE_CANNOT_CREATE_LIVE_ACTIVITY,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_FAILURE_CANNOT_CREATE_LIVE_ACTIVITY);
    }

    String activityId = (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ACTIVITY_ID);
    if (activityId == null) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_MISSING, "Missing argument activityId");
    }

    Activity activity = activityRepository.getActivityById(activityId);
    if (activity == null) {
      return getNoSuchActivityResponse(activityId);
    }

    String spaceControllerId =
        (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_ID);
    if (spaceControllerId == null) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_MISSING, "Missing argument spaceControllerId");
    }

    SpaceController spaceController =
        spaceControllerRepository.getSpaceControllerById(spaceControllerId);
    if (spaceController == null) {
      return getNoSuchSpaceControllerResponse(spaceControllerId);
    }

    String name = (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME);
    if (name == null || name.trim().isEmpty()) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_MISSING, "Missing argument "
              + MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME);
    }

    // This field is not required.
    String description =
        (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION);

    SimpleLiveActivity liveActivityTemplate = new SimpleLiveActivity();
    liveActivityTemplate.setActivity(activity);
    liveActivityTemplate.setController(spaceController);
    liveActivityTemplate.setName(name);
    liveActivityTemplate.setDescription(description);

    LiveActivity newLiveActivity = activityRepository.newAndSaveLiveActivity(liveActivityTemplate);

    Map<String, Object> responseData = new HashMap<>();
    extractLiveActivityApiData(newLiveActivity, responseData);

    return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
  }

  @Override
  public Map<String, Object> getLiveActivityConfiguration(String typedId) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      Map<String, String> data = new HashMap<>();

      ActivityConfiguration config = liveActivity.getConfiguration();
      if (config != null) {
        for (ConfigurationParameter parameter : config.getParameters()) {
          data.put(parameter.getName(), parameter.getValue());
        }
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(data);
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> configureLiveActivity(String typedId, Map<String, String> newConfigurationMap) {
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(typedId);
    if (liveActivity != null) {
      if (saveLiveActivityConfiguration(liveActivity, newConfigurationMap)) {
        activityRepository.saveLiveActivity(liveActivity);
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(typedId);
    }
  }

  @Override
  public Map<String, Object> editLiveActivity(Map<String, Object> args) {
    String liveActivityId =
        (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID);
    LiveActivity liveActivity = activityRepository.getLiveActivityByTypedId(liveActivityId);
    if (liveActivity == null) {
      return getNoSuchLiveActivityResponse(liveActivityId);
    }

    boolean edited = false;

    String name = (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME);
    if (name != null) {
      liveActivity.setName(name);

      edited |= true;
    }

    String description =
        (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION);
    if (description != null) {
      liveActivity.setDescription(description);

      edited |= true;
    }

    String activityId = (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_ACTIVITY_ID);
    if (activityId != null) {
      Activity newActivity = activityRepository.getActivityById(activityId);
      if (newActivity == null) {
        return getNoSuchActivityResponse(activityId);
      }

      liveActivity.setActivity(newActivity);

      edited |= true;
    }

    String spaceControllerId =
        (String) args.get(MasterApiMessages.MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_ID);
    if (spaceControllerId != null) {
      SpaceController spaceController =
          spaceControllerRepository.getSpaceControllerById(spaceControllerId);
      if (spaceController == null) {
        return getNoSuchSpaceControllerResponse(spaceControllerId);
      }

      liveActivity.setController(spaceController);

      edited |= true;
    }

    if (edited) {
      activityRepository.saveLiveActivity(liveActivity);
    }

    return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
  }

  /**
   * Get the new configuration into the live activity.
   *
   * @param liveactivity
   *          the live activity being configured
   * @param newConfigurationMap
   *          the map representing the new configuration
   *
   * @return {@code true} if there was a change to the configuration
   */
  private boolean saveLiveActivityConfiguration(LiveActivity liveactivity, Map<String, String> newConfigurationMap) {
    ActivityConfiguration configuration = liveactivity.getConfiguration();
    if (configuration != null) {
      return mergeActivityConfigurationParameters(newConfigurationMap, configuration);
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (newConfigurationMap.isEmpty()) {
        return false;
      }

      newLiveActivityConfiguration(liveactivity, newConfigurationMap);

      return true;
    }
  }

  /**
   * merge the values in the map with the configuration.
   *
   * @param newConfigurationMap
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the
   *         configuration
   */
  private boolean mergeActivityConfigurationParameters(Map<String, String> newConfigurationMap,
      ActivityConfiguration configuration) {
    boolean changed = false;

    Map<String, ConfigurationParameter> existingMap = configuration.getParameterMap();

    // Delete all items removed
    for (Entry<String, ConfigurationParameter> entry : existingMap.entrySet()) {
      if (!newConfigurationMap.containsKey(entry.getKey())) {
        changed = true;

        configuration.removeParameter(entry.getValue());
      }
    }

    // Now everything in the submitted map will be check. if the name exists
    // in the old configuration, we will try and change the value. if the
    // name doesn't exist, add it.
    for (Entry<String, String> entry : newConfigurationMap.entrySet()) {
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
   * Create a new configuration for a live activity.
   *
   * @param liveactivity
   *          the live activity
   * @param map
   *          the new configuration
   */
  private void newLiveActivityConfiguration(LiveActivity liveactivity, Map<String, String> map) {
    ActivityConfiguration configuration;
    configuration = activityRepository.newActivityConfiguration();
    liveactivity.setConfiguration(configuration);

    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = activityRepository.newActivityConfigurationParameter();
      parameter.setName(entry.getKey());
      parameter.setValue(entry.getValue());

      configuration.addParameter(parameter);
    }
  }

  @Override
  public Map<String, Object> updateLiveActivityMetadata(String typedId, Map<String, Object> metadataCommand) {
    try {
      LiveActivity activity = activityRepository.getLiveActivityByTypedId(typedId);
      if (activity == null) {
        return getNoSuchLiveActivityResponse(typedId);
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        activity.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        activity.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = activity.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        activity.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_SPACE_COMMAND_UNKNOWN,
            String.format("Unknown live activity metadata update command %s", command));
      }

      activityRepository.saveLiveActivity(activity);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Could not modify live activity metadata", response);

      return response;
    }
  }

  @Override
  public Map<String, Object> deleteLiveActivityGroup(String id) {
    LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
    if (group != null) {
      activityRepository.deleteLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> getBasicSpaceControllerApiData(SpaceController controller) {
    Map<String, Object> data = new HashMap<>();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, controller.getId());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID, controller.getUuid());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, controller.getName());

    return data;
  }

  @Override
  public Map<String, Object> getSpaceControllerConfiguration(String id) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
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
      return getNoSuchLiveActivityResponse(id);
    }
  }

  @Override
  public Map<String, Object> configureSpaceController(String id, Map<String, String> map) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
    if (spaceController != null) {
      if (saveSpaceControllerConfiguration(spaceController, map)) {
        spaceControllerRepository.saveSpaceController(spaceController);
      }

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchLiveActivityResponse(id);
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
      return mergeSpaceControllerConfigurationParameters(map, configuration);
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
   * Merge the values in the map with the configuration.
   *
   * @param map
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the
   *         configuration
   */
  private boolean mergeSpaceControllerConfigurationParameters(Map<String, String> map,
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

        parameter = spaceControllerRepository.newSpaceControllerConfigurationParameter();
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

  /**
   * Get the Master API response data for a live activity.
   *
   * @param liveActivity
   *          the live activity to get data from
   * @param data
   *          the map where the data will be stored
   */
  private void extractLiveActivityApiData(LiveActivity liveActivity, Map<String, Object> data) {
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, liveActivity.getId());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_UUID, liveActivity.getUuid());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, liveActivity.getName());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION,
        liveActivity.getDescription());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA,
        liveActivity.getMetadata());
    data.put("outOfDate", liveActivity.isOutOfDate());

    Activity activity = liveActivity.getActivity();
    data.put("activity", extractBasicActivityApiData(activity));

    SpaceController controller = liveActivity.getController();
    data.put("controller", getBasicSpaceControllerApiData(controller));

    Date lastDeployDate = liveActivity.getLastDeployDate();
    data.put("lastDeployDate", (lastDeployDate != null) ? lastDeployDate.toString() : null);

    getLiveActivityStatusApiData(liveActivity, data);
  }

  @Override
  public void getLiveActivityStatusApiData(LiveActivity liveActivity, Map<String, Object> data) {
    ActiveLiveActivity active = activeSpaceControllerManager.getActiveLiveActivity(liveActivity);

    Map<String, Object> activeData = new HashMap<>();
    data.put("active", activeData);

    ActivityState runtimeState = active.getRuntimeState();
    activeData.put("runtimeState", runtimeState.name());
    activeData.put("runtimeStateDescription", runtimeState.getDescription());
    activeData.put("runtimeStateDetail", active.getRuntimeStateDetail());
    ActivityState deployState = active.getDeployState();
    activeData.put("deployState", deployState.name());
    activeData.put("deployStateDescription", deployState.getDescription());
    activeData.put("deployStateDetail", active.getDeployStateDetail());
    activeData.put("directRunning", active.isDirectRunning());
    activeData.put("directActivated", active.isDirectActivated());
    activeData.put("numberLiveActivityGroupRunning", active.getNumberLiveActivityGroupRunning());
    activeData
        .put("numberLiveActivityGroupActivated", active.getNumberLiveActivityGroupActivated());
    Date lastStateUpdateDate = active.getLastStateUpdateDate();
    activeData.put("lastStateUpdate",
        (lastStateUpdateDate != null) ? lastStateUpdateDate.toString() : null);
  }

  @Override
  public Map<String, Object> getLiveActivityGroupView(String id) {
    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      return SmartSpacesMessagesSupport
          .getSuccessResponse(getLiveActivityGroupApiData(liveActivityGroup));
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> getLiveActivityGroupFullView(String id) {
    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      Map<String, Object> responseData = new HashMap<>();
      responseData.put("liveactivitygroup", getLiveActivityGroupApiData(liveActivityGroup));

      List<LiveActivity> liveActivities = new ArrayList<>();
      for (GroupLiveActivity gla : liveActivityGroup.getLiveActivities()) {
        liveActivities.add(gla.getActivity());
      }

      Collections.sort(liveActivities, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);
      responseData.put("liveactivities", extractLiveActivities(liveActivities));

      List<Space> spaces =
          Lists.newArrayList(activityRepository.getSpacesByLiveActivityGroup(liveActivityGroup));
      Collections.sort(spaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("spaces", getSpaceApiData(spaces));

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchLiveActivityGroupResponse(id);
    }
  }

  @Override
  public Map<String, Object> getLiveActivityGroupsByFilter(String filter) {
    List<Map<String, Object>> responseData = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<LiveActivityGroup> liveActivityGroups =
          activityRepository.getLiveActivityGroups(filterExpression);
      Collections.sort(liveActivityGroups,
          MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      for (LiveActivityGroup group : liveActivityGroups) {
        Map<String, Object> groupData = new HashMap<>();

        extractLiveActivityGroup(group, groupData);

        responseData.add(groupData);
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Attempt to get live activity group data failed", response);

      return response;
    }
  }

  /**
   * Get the Master API response data describing a live activity group.
   *
   * @param liveActivityGroup
   *          the live activity group
   *
   * @return the API Response data describing the group
   */
  private Map<String, Object> getLiveActivityGroupApiData(LiveActivityGroup liveActivityGroup) {
    Map<String, Object> data = new HashMap<>();

    extractLiveActivityGroup(liveActivityGroup, data);

    List<Map<String, Object>> activityData = new ArrayList<>();
    data.put("liveActivities", activityData);

    List<LiveActivity> liveActivities = new ArrayList<>();
    for (GroupLiveActivity gactivity : liveActivityGroup.getLiveActivities()) {
      liveActivities.add(gactivity.getActivity());
    }
    Collections.sort(liveActivities, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    for (LiveActivity liveActivity : liveActivities) {
      Map<String, Object> liveActivityData = new HashMap<>();
      activityData.add(liveActivityData);

      extractLiveActivityApiData(liveActivity, liveActivityData);
    }

    return data;
  }

  @Override
  public List<Map<String, Object>> getAllUiLiveActivitiesByController(SpaceController controller) {
    List<LiveActivity> liveActivitiesByController =
        Lists.newArrayList(activityRepository.getLiveActivitiesByController(controller));
    Collections.sort(liveActivitiesByController,
        MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    return extractLiveActivities(liveActivitiesByController);
  }

  /**
   * Extract the live activity data for all given live activities.
   *
   * @param liveActivities
   *          the live activities
   *
   * @return list of the data for all live activities
   */
  private List<Map<String, Object>> extractLiveActivities(List<LiveActivity> liveActivities) {
    List<Map<String, Object>> result = new ArrayList<>();

    if (liveActivities != null) {
      for (LiveActivity liveActivity : liveActivities) {
        Map<String, Object> data = new HashMap<>();
        extractLiveActivityApiData(liveActivity, data);
        result.add(data);
      }
    }

    return result;
  }

  /**
   * Extract the live activity data for all given live activities.
   *
   * @param liveActivityGroups
   *          the live activity groups
   *
   * @return list of the data for all the live activity groups
   */
  private List<Map<String, Object>> extractLiveActivityGroups(
      List<LiveActivityGroup> liveActivityGroups) {
    List<Map<String, Object>> result = new ArrayList<>();

    if (liveActivityGroups != null) {
      for (LiveActivityGroup liveActivityGroup : liveActivityGroups) {
        Map<String, Object> data = new HashMap<>();
        extractLiveActivityGroup(liveActivityGroup, data);
        result.add(data);
      }
    }

    return result;
  }

  @Override
  public Map<String, Object> getSpacesByFilter(String filter) {
    List<Map<String, Object>> data = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<Space> spaces = activityRepository.getSpaces(filterExpression);
      Collections.sort(spaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);

      for (Space space : spaces) {
        data.add(getBasicSpaceViewApiResponse(space));
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(data);
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Attempt to get all space data failed", response);

      return response;
    }
  }

  @Override
  public Map<String, Object> deleteSpace(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      activityRepository.deleteSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> updateSpaceMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      Space space = activityRepository.getSpaceById(id);
      if (space == null) {
        return getNoSuchSpaceResponse(id);
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        space.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        space.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = space.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        space.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_SPACE_COMMAND_UNKNOWN,
            String.format("Unknown space metadata update command %s", command));
      }

      activityRepository.saveSpace(space);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Could not modify space metadata", response);

      return response;
    }
  }

  /**
   * Get the basic Master API view of a space.
   *
   * @param space
   *          the space
   *
   * @return the Master API data
   */
  private Map<String, Object> getBasicSpaceViewApiResponse(Space space) {
    Map<String, Object> spaceData = new HashMap<>();

    getBasicSpaceApiResponse(space, spaceData);

    return spaceData;
  }

  /**
   * Get the basic space data for a list of spaces.
   *
   * @param spaces
   *          a list of spaces
   *
   * @return a list of the basic space data
   */
  private List<Map<String, Object>> getSpaceApiData(List<Space> spaces) {
    List<Map<String, Object>> data = new ArrayList<>();

    for (Space space : spaces) {
      data.add(getBasicSpaceViewApiResponse(space));
    }

    return data;
  }

  /**
   * Add in the basic space data used in API calls.
   *
   * @param space
   *          the space to get the data from
   * @param response
   *          the Master API data being collected
   */
  private void getBasicSpaceApiResponse(Space space, Map<String, Object> response) {
    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, space.getId());
    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, space.getName());
    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION,
        space.getDescription());
    response.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA, space.getMetadata());
  }

  @Override
  public Map<String, Object> getSpaceView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      return SmartSpacesMessagesSupport.getSuccessResponse(getSpaceViewApiResponse(space));
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> getSpaceFullView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      Map<String, Object> responseData = new HashMap<>();

      responseData.put("space", getSpaceViewApiResponse(space));

      List<? extends LiveActivityGroup> liveActivityGroups = space.getActivityGroups();
      Collections.sort(liveActivityGroups,
          MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      responseData.put("liveActivityGroups", getLiveActivityGroupsMasterApi(liveActivityGroups));

      List<? extends Space> subspaces = space.getSpaces();
      Collections.sort(subspaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("subspaces", subspaces);

      List<Space> cspaces = Lists.newArrayList(activityRepository.getSpacesBySubspace(space));
      Collections.sort(cspaces, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);
      responseData.put("containingSpaces", cspaces);

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  @Override
  public Map<String, Object> getSpaceLiveActivityGroupView(String id) {
    Space space = activityRepository.getSpaceById(id);
    if (space != null) {
      Map<String, Object> responseData = new HashMap<>();

      responseData.put("space", getSpaceViewApiResponse(space));

      Set<LiveActivityGroup> liveActivityGroupsSet = new HashSet<>();
      collectLiveActivityGroupsForSpace(space, liveActivityGroupsSet);
      List<LiveActivityGroup> liveActivityGroups = Lists.newArrayList(liveActivityGroupsSet);
      Collections.sort(liveActivityGroups,
          MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

      responseData.put("liveActivityGroups",
          getLiveActivityGroupsLiveActivitiesMasterApi(liveActivityGroups));

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } else {
      return getNoSuchSpaceResponse(id);
    }
  }

  /**
   * Get the complete space view for a given space.
   *
   * @param space
   *          the space
   *
   * @return the Master API view
   */
  private Map<String, Object> getSpaceViewApiResponse(Space space) {
    Map<String, Object> data = getBasicSpaceViewApiResponse(space);

    addLiveActivityGroupsDataApiResponse(space, data);
    generateSubspacesViewApiResponse(space, data);

    return data;
  }

  /**
   * Add all data needed for groups.
   *
   * @param space
   *          the space which contains the groups
   * @param data
   *          the Master API result for the space
   */
  private void addLiveActivityGroupsDataApiResponse(Space space, Map<String, Object> data) {
    List<Map<String, Object>> groupData = new ArrayList<>();
    data.put("liveActivityGroups", groupData);

    for (LiveActivityGroup group : space.getActivityGroups()) {
      groupData.add(getLiveActivityGroupApiData(group));
    }
  }

  /**
   * Add all data needed for subspaces.
   *
   * @param space
   *          the space which contains the subspaces
   * @param data
   *          the Master API result for the space
   */
  private void generateSubspacesViewApiResponse(Space space, Map<String, Object> data) {
    List<Map<String, Object>> subspaceData = new ArrayList<>();
    data.put("subspaces", subspaceData);

    for (Space subspace : space.getSpaces()) {
      subspaceData.add(getSpaceViewApiResponse(subspace));
    }
  }

  /**
   * Collect the live activity groups from this space and all subspaces.
   *
   * @param space
   *          the root space
   * @param liveActivityGroups
   *          the set of all groups seen
   */
  private void collectLiveActivityGroupsForSpace(Space space,
      Set<LiveActivityGroup> liveActivityGroups) {
    liveActivityGroups.addAll(space.getActivityGroups());

    for (Space subspace : space.getSpaces()) {
      collectLiveActivityGroupsForSpace(subspace, liveActivityGroups);
    }
  }

  /**
   * Get a list of live activity groups master API data.
   *
   * @param groups
   *          list of groups
   *
   * @return the API data being collected
   */
  private List<Map<String, Object>> getLiveActivityGroupsMasterApi(
      List<? extends LiveActivityGroup> groups) {
    List<Map<String, Object>> response = new ArrayList<>();

    if (groups != null) {
      for (LiveActivityGroup group : groups) {
        Map<String, Object> groupData = new HashMap<>();
        extractLiveActivityGroup(group, groupData);
        response.add(groupData);
      }
    }

    return response;
  }

  /**
   * Get a list of live activity groups master API data.
   *
   * @param groups
   *          list of groups
   *
   * @return the API data being collected
   */
  private List<Map<String, Object>> getLiveActivityGroupsLiveActivitiesMasterApi(
      List<? extends LiveActivityGroup> groups) {
    List<Map<String, Object>> response = new ArrayList<>();

    if (groups != null) {
      for (LiveActivityGroup group : groups) {
        response.add(getLiveActivityGroupApiData(group));
      }
    }

    return response;
  }

  /**
   * Translate live activity group data into the form needed for the Master API.
   *
   * @param group
   *          the group to get the data from
   * @param data
   *          the API data being collected
   */
  private void extractLiveActivityGroup(LiveActivityGroup group, Map<String, Object> data) {
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, group.getId());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, group.getName());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION, group.getDescription());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA, group.getMetadata());
  }

  @Override
  public Map<String, Object> updateLiveActivityGroupMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      LiveActivityGroup group = activityRepository.getLiveActivityGroupById(id);
      if (group == null) {
        return getNoSuchLiveActivityGroupResponse(id);
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        group.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = group.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        group.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = group.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        group.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_SPACE_COMMAND_UNKNOWN,
            String.format("Unknown live activity group metadata update command %s", command));
      }

      activityRepository.saveLiveActivityGroup(group);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      Map<String, Object> response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
              e);

      logResponseError("Could not modify live activity group metadata", response);

      return response;
    }
  }

  /**
   * Update the deployment time of a live activity.
   *
   * @param activeLiveActivity
   *          the live activity
   * @param timestamp
   *          timestamp of the time of deployment
   */
  private void updateLiveActivityDeploymentTime(ActiveLiveActivity activeLiveActivity,
      long timestamp) {
    String uuid = activeLiveActivity.getLiveActivity().getUuid();
    LiveActivity liveActivity = activityRepository.getLiveActivityByUuid(uuid);
    if (liveActivity != null) {
      liveActivity.setLastDeployDate(new Date(timestamp));

      activityRepository.saveLiveActivity(liveActivity);
    } else {
      spaceEnvironment.getLog().warn(
          String.format("Attempt to update deployment time for an unknown live activity %s", uuid));
    }
  }

  /**
   * Get the Master API response for no such activity.
   *
   * @param liveActivity
   *          the live activity
   * @param result
   *          the result from the install
   * @param timestamp
   *          when the install was completed
   */
  private void handleLiveActivityInstallMasterEvent(ActiveLiveActivity liveActivity,
      LiveActivityDeploymentResponse result, long timestamp) {
    if (result.getStatus() == ActivityDeployStatus.SUCCESS) {
      updateLiveActivityDeploymentTime(liveActivity, timestamp);
    }
  }

  /**
   * Get the master event listener for the manager.
   *
   * @return the master event listener
   */
  @VisibleForTesting
  MasterEventListener getMasterEventListener() {
    return masterEventListener;
  }

  /**
   * Set the master event manager.
   *
   * @param masterEventManager
   *          the master event manager
   */
  public void setMasterEventManager(MasterEventManager masterEventManager) {
    this.masterEventManager = masterEventManager;
  }

  /**
   * Set the activity repository.
   *
   * @param activityRepository
   *          the activity repository
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Set the space controller repository.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * Set the resource repository manager.
   *
   * @param resourceRepositoryManager
   *          the resource repository manager
   */
  public void setResourceRepositoryManager(ResourceRepositoryManager resourceRepositoryManager) {
    this.resourceRepositoryManager = resourceRepositoryManager;
  }

  /**
   * Set the active space controller manager.
   *
   * @param activeSpaceControllerManager
   *          the active space controller manager
   */
  public void setActiveSpaceControllerManager(
      ActiveSpaceControllerManager activeSpaceControllerManager) {
    this.activeSpaceControllerManager = activeSpaceControllerManager;
  }
}
