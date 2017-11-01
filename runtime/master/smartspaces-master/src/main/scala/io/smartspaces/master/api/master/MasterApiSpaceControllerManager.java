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

package io.smartspaces.master.api.master;

import java.util.List;
import java.util.Map;

/**
 * Manager for the Master API control of space controllers.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiSpaceControllerManager {

  /**
   * Get all activities in the repository that match the filter.
   *
   * @param filter
   *          the filter expression
   *
   * @return all activities in the repository matching the filter
   */
  Map<String, Object> getSpaceControllersByFilter(String filter);

  /**
   * Get the view of all space controllers.
   *
   * @return the Master API view of all space controllers
   */
  Map<String, Object> getSpaceControllerAllView();

  /**
   * Get the full view of a space controller.
   *
   * @param typedId
   *          ID of the space controller
   *
   * @return the Master API view of the space controller
   */
  Map<String, Object> getSpaceControllerFullView(String typedId);

  /**
   * Get the view of a space controller.
   *
   * @param typedId
   *          ID of the space controller
   *
   * @return the Master API view of the space controller
   */
  Map<String, Object> getSpaceControllerView(String typedId);

  /**
   * Get the configuration of a space controller.
   *
   * @param typedId
   *          ID of the space controller
   *
   * @return the configuration
   */
  Map<String, Object> getSpaceControllerConfiguration(String typedId);

  /**
   * Set the configuration for a space controller.
   *
   * @param typedId
   *          ID of the space controller
   * @param map
   *          the new configuration
   *
   * @return API response
   */
  Map<String, Object> setSpaceControllerConfiguration(String typedId, Map<String, String> map);

  /**
   * Configure a space controller.
   *
   * @param typedId
   *          ID of the space controller
   *
   * @return API response
   */
  Map<String, Object> configureSpaceController(String typedId);

  /**
   * Modify a space controller's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in data</li>
   * </ul>
   *
   * @param typedId
   *          ID of the space controller
   * @param metadataCommandObj
   *          the modification command
   *
   * @return the master API response
   */
  Map<String, Object> updateSpaceControllerMetadata(String typedId, Object metadataCommandObj);

  /**
   * Delete a controller from the controller repository.
   *
   * @param typedId
   *          ID of the controller.
   *
   * @return the Master API response
   */
  Map<String, Object> deleteSpaceController(String typedId);

  /**
   * Shut down a set of controllers controllers.
   *
   * @param typedIds
   *          IDs of the controllers
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownSpaceControllers(List<String> typedIds);

  /**
   * Hard restart a set of controllers controllers.
   *
   * @param typedIds
   *          IDs of the controllers
   *
   * @return the Master API response
   */
  Map<String, Object> hardRestartSpaceControllers(List<String> typedIds);

  /**
   * Soft restart a set of controllers controllers.
   *
   * @param typedIds
   *          IDs of the controllers
   *
   * @return the Master API response
   */
  Map<String, Object> softRestartSpaceControllers(List<String> typedIds);

  /**
   * Connect to all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> connectToAllSpaceControllers();

  /**
   * Disconnect to all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> disconnectFromAllSpaceControllers();

  /**
   * Shut down all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllSpaceControllers();

  /**
   * Hard restart all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> hardRestartAllSpaceControllers();

  /**
   * Soft restart all controllers in the repository.
   *
   * @return the Master API response
   */
  Map<String, Object> softRestartAllSpaceControllers();

  /**
   * Get the status from all controllers in the repository that are not marked
   * unknown. This means no one has tried connecting.
   *
   * @return the Master API response
   */
  Map<String, Object> statusFromAllSpaceControllers();

  /**
   * Get the status from all controllers in the repository, whether or not they
   * are connected.
   *
   * @return the Master API response
   */
  Map<String, Object> forceStatusFromAllSpaceControllers();

  /**
   * Connect to all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param typedIds
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> connectToSpaceControllers(List<String> typedIds);

  /**
   * Disconnect from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param typedIds
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> disconnectFromSpaceControllers(List<String> typedIds);

  /**
   * Get a status from all controllers listed.
   *
   * <p>
   * Illegal controller IDs will be ignored.
   *
   * @param typedIds
   *          IDs of all controllers
   *
   * @return the Master API response
   */
  Map<String, Object> statusSpaceControllers(List<String> typedIds);

  /**
   * Clean the temp data folder for the specified controller.
   *
   * @param typedId
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerTempData(String typedId);

  /**
   * Clean the temp data folder for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerTempDataAllSpaceControllers();

  /**
   * Clean the permanent data folder for the controller.
   *
   * @param typedId
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerPermanentData(String typedId);

  /**
   * Clean the permanent data folder for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerPermanentDataAllSpaceControllers();

  /**
   * Clean the temp data folder for all live activities on a controller.
   *
   * @param typedId
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerActivitiesTempData(String typedId);

  /**
   * Clean the temp data folder for all live activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerActivitiesTempDataAllSpaceControllers();

  /**
   * Clean the permanent data folder for all live activities on a controller.
   *
   * @param typedId
   *          ID of the controller to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerActivitiesPermanentData(String typedId);

  /**
   * Clean the permanent data folder all live activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers();

  /**
   * Capture the data bundle for the given controller ID.
   *
   * @param typedId
   *          Id of the target controller
   *
   * @return the Master API response
   */
  Map<String, Object> captureDataSpaceController(String typedId);

  /**
   * Restore the data bundle for the given controller ID.
   *
   * @param typedId
   *          Id of the target controller
   *
   * @return the Master API response
   */
  Map<String, Object> restoreDataSpaceController(String typedId);

  /**
   * Capture the data bundle for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> captureDataAllSpaceControllers();

  /**
   * Restore the data bundles for all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> restoreDataAllSpaceControllers();

  /**
   * Shut down all activities on the specified controller.
   *
   * @param typedId
   *          ID of the controller
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllLiveActivities(String typedId);

  /**
   * Shut down all activities on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownAllLiveActivitiesAllSpaceControllers();

  /**
   * Deploy all live activities which are on the specified controller.
   *
   * @param typedId
   *          ID of the controller
   *
   * @return the Master API response
   */
  Map<String, Object> deployAllLiveActivitiesSpaceController(String typedId);

  /**
   * Deploy all live activities which are on all controllers.
   *
   * @return the Master API response
   */
  Map<String, Object> deployAllLiveActivitiesAllSpaceControllers();

  /**
   * Deploy all out of date live activities which are based on the specified
   * activity.
   *
   * @param id
   *          ID of the activity
   *
   * @return the Master API response
   */
  Map<String, Object> deployAllActivityLiveActivities(String id);

  /**
   * Deploy the specified live activity to its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deployLiveActivity(String typedId);

  /**
   * Delete the specified live activity from its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deleteLiveActivity(String typedId);

  /**
   * Configure a live activity on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> configureLiveActivity(String typedId);

  /**
   * Start a live activity on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> startupLiveActivity(String typedId);

  /**
   * Activate a live activity on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> activateLiveActivity(String typedId);

  /**
   * Deactivate a live activity on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> deactivateLiveActivity(String typedId);

  /**
   * Shut a live activity down on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> shutdownLiveActivity(String typedId);

  /**
   * Status of a live activity on its controller.
   *
   * @param typedId
   *          ID of the live activity
   *
   * @return the Master API response
   */
  Map<String, Object> statusLiveActivity(String typedId);

  /**
   * Clean the permanent data folder for the live activity.
   *
   * @param typedId
   *          ID of the live activity to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanLiveActivityPermanentData(String typedId);

  /**
   * Clean the temp data folder for the live activity.
   *
   * @param typedId
   *          ID of the live activity to clean
   *
   * @return the Master API response
   */
  Map<String, Object> cleanLiveActivityTempData(String typedId);

  /**
   * Deploy the specified activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> deployLiveActivityGroup(String id);

  /**
   * Configure the specified activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> configureLiveActivityGroup(String id);

  /**
   * Start an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> startupLiveActivityGroup(String id);

  /**
   * Activate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> activateLiveActivityGroup(String id);

  /**
   * Deactivate an activity group on its controller.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> deactivateLiveActivityGroup(String id);

  /**
   * Shut an activity group.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response.
   */
  Map<String, Object> shutdownLiveActivityGroup(String id);

  /**
   * Force all live activities in a live activity group to shutdown.
   *
   * @param id
   *          ID of the activity group
   *
   * @return the Master API response
   */
  Map<String, Object> forceShutdownLiveActivitiesLiveActivityGroup(String id);

  /**
   * Status of all live activities in a live activity group.
   *
   * @param id
   *          ID of the live activity group
   *
   * @return the Master API response
   */
  Map<String, Object> statusLiveActivityGroup(String id);

  /**
   * Status of all live activities in all live activity groups in a space and
   * its subspaces.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> liveActivityStatusSpace(String id);

  /**
   * Deploy everything needed a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> deploySpace(String id);

  /**
   * Configure all components in a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> configureSpace(String id);

  /**
   * Start the given space.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> startupSpace(String id);

  /**
   * Shut a given space down. Child spaces will be shut down first.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> shutdownSpace(String id);

  /**
   * Activate a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> activateSpace(String id);

  /**
   * Deactivate a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> deactivateSpace(String id);

  /**
   * Get the status for a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the master API response
   */
  Map<String, Object> statusSpace(String id);
}
