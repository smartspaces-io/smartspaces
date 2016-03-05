/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.activity.component.route;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.component.ActivityComponent;
import io.smartspaces.messaging.route.RouteMessagePublisher;

import java.util.Map;
import java.util.Set;

/**
 * An activity component which supports route messaging.
 *
 * @author Keith M. Hughes
 */
public interface MessageRouterActivityComponent extends ActivityComponent {

  /**
   * Separator for configuration values which allow multiple values.
   */
  String CONFIGURATION_VALUES_SEPARATOR = ":";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_ROUTES_INPUTS = "space.activity.routes.inputs";

  /**
   * Configuration name prefix for route inputs.
   */
  String CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX = "space.activity.route.input.";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_ROUTES_OUTPUTS = "space.activity.routes.outputs";

  /**
   * Configuration name prefix for route outputs.
   */
  String CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX = "space.activity.route.output.";

  /**
   * Get the node name for the router.
   *
   * @return the node name
   */
  String getNodeName();

  /**
   * Send out a message on one of the output channels.
   *
   * <p>
   * The message is dropped if there is no such channel, though it will be
   * logged.
   *
   * @param outputChannelId
   *          ID of the output channel
   * @param message
   *          message to send
   */
  void writeOutputMessage(String outputChannelId, Map<String,Object> message);

  /**
   * Register a new channel output topic route.
   *
   * @param outputChannelId
   *          the output channel ID
   * @param topicNames
   *          output topic names
   * @param latch
   *          should output be latched
   *
   * @return the message publisher for the route
   *
   * @throws SmartSpacesException
   *           the output ID has been used before
   */
  RouteMessagePublisher registerOutputChannelTopic(String outputChannelId,
      Set<String> topicNames, boolean latch) throws SmartSpacesException;

  /**
   * Register a new input topic channel.
   *
   * @param inputChannelId
   *          input channel ID
   * @param topicNames
   *          input topic names
   *
   * @throws SmartSpacesException
   *           the input name has been used before
   */
  void registerInputChannelTopic(String inputChannelId, Set<String> topicNames)
      throws SmartSpacesException;

  /**
   * Shutdown and clear all the input/output message topics.
   */
  void clearAllChannelTopics();

  /**
   * Add a new message router listener to the component.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(MessageRouterActivityComponentListener listener);

  /**
   * Remove a message router listener to the component.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(MessageRouterActivityComponentListener listener);

  /**
   * Get all output channel IDs from the component.
   *
   * @return all output channel IDs
   */
  Set<String> getOutputChannelIds();

  /**
   * get the output publisher for a given route.
   *
   * @param outputChannelId
   *          the output channel ID
   *
   * @return the output publisher, or {@code null} if none found for the
   *         specified ID
   */
  RouteMessagePublisher getMessagePublisher(String outputChannelId);

  /**
   * Get all input channel IDs from the component.
   *
   * @return all input channel IDs
   */
  Set<String> getInputChannelIds();
}
