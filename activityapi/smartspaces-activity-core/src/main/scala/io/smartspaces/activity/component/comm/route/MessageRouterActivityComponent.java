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

package io.smartspaces.activity.component.comm.route;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.component.comm.PubSubActivityComponent;
import io.smartspaces.messaging.route.MessageRouter;
import io.smartspaces.messaging.route.RouteMessageListener;
import io.smartspaces.messaging.route.RouteMessagePublisher;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An activity component which supports route messaging.
 *
 * @author Keith M. Hughes
 */
public interface MessageRouterActivityComponent extends PubSubActivityComponent {

  /**
   * Name of the component.
   */
  String COMPONENT_NAME = "comm.router";

  /**
   * Description of this component.
   */
  String COMPONENT_DESCRIPTION = "Pub/Sub Message Router";

  /**
   * Dependencies for the component.
   */
  List<String> BASE_COMPONENT_DEPENDENCIES = ImmutableList.of();
  //List<String> BASE_COMPONENT_DEPENDENCIES = ImmutableList.of(RosActivityComponent.COMPONENT_NAME);

  /**
   * Set the message listener for the component for messages not caught by explicit listeners.
   * 
   * @param messageListener
   *          the message listener
   */
  void setRoutableInputMessageListener(RouteMessageListener messageListener);

  /**
   * Set the message listener for the component for messages for a specific channel ID.
   * 
   * <p>
   * There can only be one message listener per channel ID registered here. Additional registrations will delete
   * the previous one.
   * 
   * @param channelId
   *          ID of the channel for this specific listener
   * @param messageListener
   *          the message listener
   */
  void addRoutableInputMessageListener(String channelId, RouteMessageListener messageListener);

  /**
   * Separator for configuration values which allow multiple values.
   */
  String CONFIGURATION_VALUES_SEPARATOR = "\\s+";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_NAME_ROUTES_INPUTS = "space.activity.routes.inputs";

  /**
   * Configuration name prefix for route inputs.
   */
  String CONFIGURATION_NAME_PREFIX_ROUTE_INPUT_TOPIC = "space.activity.route.input.";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_NAME_ROUTES_OUTPUTS = "space.activity.routes.outputs";

  /**
   * Configuration name prefix for route outputs.
   */
  String CONFIGURATION_NAME_PREFIX_ROUTE_OUTPUT_TOPIC = "space.activity.route.output.";

  /**
   * Configuration property to set the route protocol default.
   */
  String CONFIGURATION_NAME_ROUTE_PROTOCOL_DEFAULT = "space.activity.route.protocol.default";

  /**
   * Configuration property value for the default MQTT broker description.
   */
  // TODO(keith): Move this somewhere more reasonable.
  String CONFIGURATION_NAME_MESSAGING_MQTT_BROKERDESCRIPTION_DEFAULT =
      "smartspaces.messaging.mqtt.brokerdescription.default";

  /**
   * Get the node name for the router.
   *
   * @return the node name
   */
  String getNodeName();

  /**
   * Get the default protocol for routes.
   * 
   * <p>
   * This is only valid after the component is configured.
   * 
   * @return the default protocol for routes
   */
  String getDefaultRouteProtocol();

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
  void writeOutputMessage(String outputChannelId, Map<String, Object> message);

  /**
   * Register a new channel output topic route.
   *
   * @param outputChannelId
   *          the output channel ID
   * @param topicNames
   *          output topic names
   *
   * @return the message publisher for the route
   *
   * @throws SmartSpacesException
   *           an unknown publisher protocol has been found or the channel has
   *           already been registered
   */
  RouteMessagePublisher registerOutputChannelTopic(String outputChannelId, Set<String> topicNames)
      throws SmartSpacesException;

  /**
   * Register a new input topic channel.
   *
   * @param inputChannelId
   *          input channel ID
   * @param topicNames
   *          input topic names
   *
   * @throws SmartSpacesException
   *           an unknown publisher protocol has been found or the channel has
   *           already been registered
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

  /**
   * Get the message router for this component.
   * 
   * @return the message router
   */
  MessageRouter getMessageRouter();
}
