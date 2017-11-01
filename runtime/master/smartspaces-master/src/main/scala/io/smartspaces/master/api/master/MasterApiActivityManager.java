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

import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleActivity;
import io.smartspaces.resource.managed.ManagedResource;

import java.io.InputStream;
import java.util.Map;

/**
 * A Master API manager for working with activities.
 *
 * <p>
 * This is mostly for crud operations. To work with activities on controllers,
 * see {@link MasterApiSpaceControllerManager}.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiActivityManager extends ManagedResource {

  /**
   * Save an activity.
   *
   * <p>
   * Includes saving the activity file in the activity repository. The method
   * also closes the input stream regardless of internal errors.
   *
   * @param activityDescription
   *          the activity description
   * @param activityContentStream
   *          the input stream containing the contents of the activity
   *
   * @return the master API message for the activity view for the activity
   */
  Map<String, Object> saveActivity(SimpleActivity activityDescription, InputStream activityContentStream);

  /**
   * Get all activities that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the Master API message for all activities that pass the filter, all
   *         are returned if filter is {@code null)
   */
  Map<String, Object> getActivitiesByFilter(String filter);

  /**
   * Get the view of an activity.
   *
   * @param id
   *          ID for the activity
   *
   * @return the master API message for the activity view
   */
  Map<String, Object> getActivityView(String id);

  /**
   * Delete an activity from the activity repository.
   *
   * <p>
   * Does nothing if there is no activity with the given ID.
   *
   * @param id
   *          ID of the activity.
   *
   * @return result of deleting activity
   */
  Map<String, Object> deleteActivity(String id);

  /**
   * Modify a activity's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in
   * data</li>
   * </ul>
   *
   * @param id
   *          ID of the activity
   * @param metadataCommandObj
   *          the modification command object
   *
   * @return the master API response object
   */
  Map<String, Object> updateActivityMetadata(String id, Object metadataCommandObj);

  /**
   * Get all live activities that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the master API response message for all live activities that pass
   *         the filter, all are returned if filter is {@code null)
   */
  Map<String, Object> getLiveActivitiesByFilter(String filter);

  /**
   * Get the view of a live activity.
   *
   * @param typedId
   *          the typed ID for the live activity
   *
   * @return the master API message for the live activity view
   */
  Map<String, Object> getLiveActivityView(String typedId);

  /**
   * Get the full view of a live activity.
   *
   * @param typedId
   *          the typed ID for the live activity
   *
   * @return the master API message for the live activity full view
   */
  Map<String, Object> getLiveActivityFullView(String typedId);

  /**
   * Can live activities be created?
   *
   * @return {@code true} if live activities can be created
   */
  boolean canCreateLiveActivities();

  /**
   * Delete a live activity from the activity repository.
   *
   * <p>
   * Does nothing if there is no live activity with the given ID.
   *
   * @param typedId
   *          the typed ID of the live activity
   *
   * @return API response to deletion
   */
  Map<String, Object> deleteLiveActivity(String typedId);

  /**
   * Create a new live activity.
   *
   * <p>
   * Does nothing if there is no live activity with the given ID.
   *
   * @param args
   *          the arguments for the live activity creation
   *
   * @return API response to creation
   */
  Map<String, Object> createLiveActivity(Map<String, Object> args);

  /**
   * Get the configuration of a live activity.
   *
   * @param typedId
   *          the typed ID of the live activity
   *
   * @return the configuration
   */
  Map<String, Object> getLiveActivityConfiguration(String typedId);

  /**
   * Configure a live activity.
   *
   * @param typedId
   *          the typed ID of the live activity
   * @param newConfigurationMap
   *          the new configuration
   *
   * @return API response
   */
  Map<String, Object> configureLiveActivity(String typedId, Map<String, String> newConfigurationMap);

  /**
   * Edit a live activity.
   *
   * @param args
   *          the arguments for the edit
   *
   * @return API response
   */
  Map<String, Object> editLiveActivity(Map<String, Object> args);

  /**
   * Get basic information about a space controller.
   *
   * @param controller
   *          the space controller
   *
   * @return a Master API Response coded object giving the basic information
   */
  Map<String, Object> getBasicSpaceControllerApiData(SpaceController controller);

  /**
   * Get the configuration of a space controller.
   *
   * @param id
   *          ID of the space controller
   *
   * @return the configuration
   */
  Map<String, Object> getSpaceControllerConfiguration(String id);

  /**
   * Configure a space controller.
   *
   * @param id
   *          ID of the space controller
   * @param map
   *          the new configuration
   *
   * @return API response
   */
  Map<String, Object> configureSpaceController(String id, Map<String, String> map);

  /**
   * Add in all data needed for the Master API response of the live activity.
   *
   * @param activity
   *          the live activity
   * @param data
   *          the JSON map to add the data into
   */
  void getLiveActivityStatusApiData(LiveActivity activity, Map<String, Object> data);

  /**
   * Get the view of a live activity group.
   *
   * @param id
   *          ID of the live activity group
   *
   * @return the Master API view of the group
   */
  Map<String, Object> getLiveActivityGroupView(String id);

  /**
   * Get the full view of a live activity group.
   *
   * @param id
   *          ID of the live activity group
   *
   * @return the Master API view of the group
   */
  Map<String, Object> getLiveActivityGroupFullView(String id);

  /**
   * Get all live activity groups that meet a filter.
   *
   * @param filter
   *          the filter for the group, can be {@code null}
   *
   * @return the Master API message for all groups that pass the filter, all are
   *         returned if filter is {@code null)
   */
  Map<String, Object> getLiveActivityGroupsByFilter(String filter);

  /**
   * Modify a live activity's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in
   * data</li>
   * </ul>
   *
   * @param typedId
   *          typed ID of the live activity
   * @param metadataCommand
   *          the modification command
   *
   * @return the Master API response object
   */
  Map<String, Object> updateLiveActivityMetadata(String typedId,
      Map<String, Object> metadataCommand);

  /**
   * Delete an activity group from the activity repository.
   *
   * <p>
   * Does nothing if there is no activity group with the given ID.
   *
   * @param id
   *          ID of the activity group.
   *
   * @return API response
   */
  Map<String, Object> deleteLiveActivityGroup(String id);

  /**
   * Modify a live activity group's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in
   * data</li>
   * </ul>
   *
   * @param id
   *          ID of the live activity
   * @param metadataCommandObj
   *          the modification command
   *
   * @return the Master API response object
   */
  Map<String, Object> updateLiveActivityGroupMetadata(String id, Object metadataCommandObj);

  /**
   * Get the full view of an activity. This will include additional information
   * about the activity, such as which live activities are based on it.
   *
   * @param id
   *          ID for the activity
   *
   * @return the Master API message for the activity view
   */
  Map<String, Object> getActivityFullView(String id);

  /**
   * Get all spaces that meet a filter.
   *
   * @param filter
   *          the filter, can be {@code null}
   *
   * @return the Master API response for spaces that pass the filter, all are
   *         returned if filter is {@code null)
   */
  Map<String, Object> getSpacesByFilter(String filter);

  /**
   * Get the view data for a specific space.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> getSpaceView(String id);

  /**
   * Get the full view data for a specific space.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> getSpaceFullView(String id);

  /**
   * Get the live activity group view of a space.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> getSpaceLiveActivityGroupView(String id);

  /**
   * Delete a space from the space repository.
   *
   * <p>
   * Does nothing if there is no space with the given ID.
   *
   * @param id
   *          ID of the space
   *
   * @return the Master API response
   */
  Map<String, Object> deleteSpace(String id);

  /**
   * Modify a space's metadata.
   *
   * <p>
   * The command map contains a field called command. This field will be one of
   *
   * <ul>
   * <li>replace - data contains a map, replace the entire metadata map with the
   * map</li>
   * <li>modify - data contains a map, replace just the fields found in the map
   * with the values found in the map</li>
   * <li>delete - data contains a list of keys, remove all keys found in
   * data</li>
   * </ul>
   *
   * @param id
   *          ID of the activity
   * @param metadataCommandObj
   *          the modification command
   *
   * @return the Master API response
   */
  Map<String, Object> updateSpaceMetadata(String id, Object metadataCommandObj);
}
