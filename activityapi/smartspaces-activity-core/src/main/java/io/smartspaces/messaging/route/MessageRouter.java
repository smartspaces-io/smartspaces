/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.messaging.route;

import java.util.Map;
import java.util.Set;

import io.smartspaces.SmartSpacesException;

/**
 * An element that can route messages in a protocol-independent manner. s
 * 
 * @author Keith M. Hughes
 */
public interface MessageRouter extends IncomingRouteMessageHandler {

  /**
   * The default value for the route protocol default.
   */
  String DEFAULT_ROUTE_PROTOCOL_DEFAULT = "ros";

  /**
   * Set the message listener for the component.
   * 
   * @param messageListener
   *          the message listener
   */
  void setRoutableInputMessageListener(RoutableInputMessageListener messageListener);

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
   * Is the given output channel ID already registered?
   * 
   * @param channelId
   *          the channel ID to check
   * 
   * @return {@code true} if the channel is registered
   */
  boolean isOutputChannelRegistered(String channelId);

  /**
   * Register a new channel output topic route.
   *
   * @param routeDescription
   *          the description of the route
   *
   * @return the message publisher for the route
   *
   * @throws SmartSpacesException
   *           an unknown publisher protocol has been found or the channel has
   *           already been registered
   */
  RouteMessagePublisher registerOutputChannelTopic(RouteDescription routeDescription)
      throws SmartSpacesException;

  /**
   * Is the given input channel ID already registered?
   * 
   * @param channelId
   *          the channel ID to check
   * 
   * @return {@code true} if the channel is registered
   */
  boolean isInputChannelRegistered(String channelId);

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
  void registerInputChannelTopic(RouteDescription routeDescription) throws SmartSpacesException;

  /**
   * Shutdown and clear all the input/output message topics.
   */
  void clearAllChannelTopics();

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
