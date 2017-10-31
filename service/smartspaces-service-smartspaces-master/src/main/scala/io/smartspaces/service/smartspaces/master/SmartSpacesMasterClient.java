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

package io.smartspaces.service.smartspaces.master;

import java.util.Map;

import io.smartspaces.resource.managed.ManagedResource;

/**
 * A client for Smart with the Smart Spaces Master API.
 *
 * <p>
 * Filters use OGNL. The OGNL expressions must return true or false. For details
 * on OGNL see <a
 * href="http://commons.apache.org/proper/commons-ognl/language-guide.html">the
 * OGNL reference</a>.
 *
 * @author Keith M. Hughes
 */
public interface SmartSpacesMasterClient extends ManagedResource {

  /**
   * Get the address of the master this is a client for.
   *
   * @return the address of the master
   */
  String getMasterAddress();

  /**
   * Get full live activity view. u
   * 
   * @param typedId
   *          the typed ID of the live activity
   * @param callback
   *          the callback for the call
   */
  void getLiveActivityFullView(String typedId, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all live activities.
   *
   * @param callback
   *          the callback to run after the response
   */
  void getAllLiveActivities(SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all live activities that match the specified filter.
   *
   * <p>
   * A {@code null} filter returns all live activities.
   *
   * @param filter
   *          the filter for matching, can be {@code null}
   * @param callback
   *          the callback to run after the response
   */
  void getAllLiveActivities(String filter, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get a live activity configuration.
   *
   * <p>
   * The data in the response will contain the configuration.
   *
   * @param id
   *          the ID of the live activity to start up
   * @param callback
   *          the callback for the call
   */
  void getLiveActivityConfiguration(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Set a live activity configuration.
   *
   * @param id
   *          the ID of the live activity to start up
   * @param newConfiguration
   *          the new configuration
   * @param callback
   *          the callback for the call
   */
  void setLiveActivityConfiguration(String id, Map<String, String> newConfiguration,
      SmartSpacesMasterApiMessageHandler callback);

  /**
   * Start up a live activity.
   *
   * @param id
   *          the ID of the live activity to start up
   * @param callback
   *          the callback for the call
   */
  void startupLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Activate a live activity.
   *
   * @param id
   *          the ID of the live activity to activate
   * @param callback
   *          the callback for the call
   */
  void activateLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Deactivate a live activity.
   *
   * @param id
   *          the ID of the live activity to deactivate
   * @param callback
   *          the callback for the call
   */
  void deactivateLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Shutdown a live activity.
   *
   * @param id
   *          the ID of the live activity to shutdown
   * @param callback
   *          the callback for the call
   */
  void shutdownLiveActivity(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get full live activity group view.
   *
   * @param id
   *          the ID of the live activity group
   * @param callback
   *          the callback for the call
   */
  void getLiveActivityGroupFullView(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all live activity groups.
   *
   * @param callback
   *          the callback to run after the response
   */
  void getAllLiveActivityGroups(SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all live activity groups that match the specified filter.
   *
   * <p>
   * A {@code null} filter returns all live activity groups.
   *
   * @param filter
   *          the filter for matching, can be {@code null}
   * @param callback
   *          the callback to run after the response
   */
  void getAllLiveActivityGroups(String filter, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Start up a live activity group.
   *
   * @param id
   *          the ID of the live activity group to start up
   * @param callback
   *          the callback for the call
   */
  void startupLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Activate a live activity group.
   *
   * @param id
   *          the ID of the live activity group to activate
   * @param callback
   *          the callback for the call
   */
  void activateLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Deactivate a live activity group.
   *
   * @param id
   *          the ID of the live activity group to deactivate
   * @param callback
   *          the callback for the call
   */
  void deactivateLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Shutdown a live activity group.
   *
   * @param id
   *          the ID of the live activity group to shutdown
   * @param callback
   *          the callback for the call
   */
  void shutdownLiveActivityGroup(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all spaces.
   *
   * @param callback
   *          the callback to run after the response
   */
  void getAllSpaces(SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get full space view.
   *
   * @param id
   *          the ID of the lspace
   * @param callback
   *          the callback for the call
   */
  void getSpaceFullView(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Get all spaces that match the specified filter.
   *
   * <p>
   * A {@code null} filter returns all spaces.
   *
   * @param filter
   *          the filter for matching, can be {@code null}
   * @param callback
   *          the callback to run after the response
   */
  void getAllSpaces(String filter, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Start up a space.
   *
   * @param id
   *          the ID of the space to start up
   * @param callback
   *          the callback for the call
   */
  void startupSpace(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Activate a space.
   *
   * @param id
   *          the ID of the space to activate
   * @param callback
   *          the callback for the call
   */
  void activateSpace(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Deactivate a space.
   *
   * @param id
   *          the ID of the space to deactivate
   * @param callback
   *          the callback for the call
   */
  void deactivateSpace(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Shutdown a space.
   *
   * @param id
   *          the ID of the space to shutdown
   * @param callback
   *          the callback for the call
   */
  void shutdownSpace(String id, SmartSpacesMasterApiMessageHandler callback);

  /**
   * Add an event message handler.
   *
   * <p>
   * Event message handlers are handlers that are not in response to a call initiated in
   * this service. They include events like a live activity starting up or
   * shutting down.
   *
   * @param handler
   *          the handler to be called
   */
  void addEventMessageHandler(SmartSpacesMasterApiMessageHandler handler);

  /**
   * Remove an event message handler.
   *
   * <p>
   * Does nothing if the handler was never added.
   *
   * @param handler
   *          the handler to be removed
   */
  void removeEventMessageHandler(SmartSpacesMasterApiMessageHandler handler);
}
