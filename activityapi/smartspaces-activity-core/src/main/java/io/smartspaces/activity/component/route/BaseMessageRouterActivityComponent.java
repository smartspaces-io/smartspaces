/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.component.BaseActivityComponent;
import io.smartspaces.activity.component.route.ros.RosMessageRouterActivityComponent;
import io.smartspaces.activity.impl.StatusDetail;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.messaging.route.RouteMessagePublisher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ros.namespace.GraphName;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * A helpful superclass for implementors of the ROS message routing activity
 * component.
 *
 * @param <T>
 *          the type of messages handled by the component
 *
 * @author Keith M. Hughes
 */
public abstract class BaseMessageRouterActivityComponent extends BaseActivityComponent implements
    RosMessageRouterActivityComponent {

  /**
   * The listeners for this component.
   */
  private final List<MessageRouterActivityComponentListener> listeners =
      new CopyOnWriteArrayList<>();

  /**
   * The default protocol for the router.
   */
  private String defaultRouteProtocol;

  /**
   * The route descriptions for all inputs.
   */
  private Map<String, RouteDescription> inputRouteDescriptions = new HashMap<>();

  /**
   * The route descriptions for all outputs.
   */
  private Map<String, RouteDescription> outputRouteDescriptions = new HashMap<>();

  @Override
  public void addListener(MessageRouterActivityComponentListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(MessageRouterActivityComponentListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void configureComponent(Configuration configuration) {
    onConfigureComponent(configuration);

    StringBuilder routeErrors = new StringBuilder();

    defaultRouteProtocol =
        configuration.getPropertyString(CONFIGURATION_ROUTE_PROTOCOL_DEFAULT,
            CONFIGURATION_VALUE_DEFAULT_ROUTE_PROTOCOL_DEFAULT);

    String inputChannelIds = configuration.getPropertyString(CONFIGURATION_ROUTES_INPUTS);
    if (inputChannelIds != null) {
      inputChannelIds = inputChannelIds.trim();
      for (String channelId : inputChannelIds.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX + channelId;
        String topicNames = configuration.getPropertyString(propertyName);
        if (topicNames != null && !topicNames.trim().isEmpty()) {
          SetMultimap<String, String> protocolToTopicName =
              parseProtocolToTopicMap(parseTopicNames(topicNames));
          for (String protocolName : protocolToTopicName.keySet()) {
            // TODO(keith: Check and confirm protocol is actually supported
            Set<String> bareTopicNames = protocolToTopicName.get(protocolName);
            for (String topicName : bareTopicNames) {
              if (!isSyntacticallyCorrectTopicName(topicName)) {
                String message =
                    String.format("Input route topic name %s is of the wrong form", topicName);
                handleError(message, null);
                routeErrors.append(routeErrors.length() > 0 ? ", " : "").append(message);
              }
            }
          }

          inputRouteDescriptions.put(channelId,
              new RouteDescription(channelId, protocolToTopicName));
        } else {
          handleError(String.format("Input route %s not defined, missing topic configuration %s",
              channelId, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing input route=")
              .append(channelId).append(" which is defined by configuration property ")
              .append(propertyName);
        }
      }
    }

    String outputChannelIds = configuration.getPropertyString(CONFIGURATION_ROUTES_OUTPUTS);
    if (outputChannelIds != null) {
      outputChannelIds = outputChannelIds.trim();
      for (String channelId : outputChannelIds.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX + channelId;
        String topicNames = configuration.getPropertyString(propertyName);
        if (topicNames != null && !topicNames.trim().isEmpty()) {
          SetMultimap<String, String> protocolToTopicName =
              parseProtocolToTopicMap(parseTopicNames(topicNames));
          for (String protocolName : protocolToTopicName.keySet()) {
            // TODO(keith: Check and confirm protocol is actually supported
            Set<String> bareTopicNames = protocolToTopicName.get(protocolName);
            for (String topicName : bareTopicNames) {
              if (!isSyntacticallyCorrectTopicName(topicName)) {
                String message =
                    String.format("Output route topic name %s is of the wrong form", topicName);
                handleError(message, null);
                routeErrors.append(routeErrors.length() > 0 ? ", " : "").append(message);
              }
            }
          }

          outputRouteDescriptions.put(channelId, new RouteDescription(channelId,
              protocolToTopicName));
        } else {
          handleError(String.format("Output route %s not defined, missing topic configuration %s",
              channelId, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing output route=")
              .append(channelId).append(" which is defined by configuration property ")
              .append(propertyName);
        }
      }
    }

    if (routeErrors.length() > 0) {
      throw new SimpleSmartSpacesException(routeErrors.toString());
    }

    if ((inputChannelIds == null || inputChannelIds.isEmpty())
        && (outputChannelIds == null || outputChannelIds.isEmpty())) {
      throw new SimpleSmartSpacesException(String.format(
          "Router has no routes. Define either %s or %s in your configuration",
          CONFIGURATION_ROUTES_INPUTS, CONFIGURATION_ROUTES_OUTPUTS));
    }
  }

  @Override
  public void startupComponent() {
    onPreStartupComponent();

    for (RouteDescription outputRouteDescription : outputRouteDescriptions.values()) {
      internalRegisterOutputRoute(outputRouteDescription);
    }

    for (RouteDescription inputRouteDescription : inputRouteDescriptions.values()) {
      internalRegisterInputRoute(inputRouteDescription);
    }

    onPostStartupComponent();
  }

  @Override
  public String getDefaultRouteProtocol() {
    return defaultRouteProtocol;
  }

  @Override
  public synchronized RouteMessagePublisher registerOutputChannelTopic(String channelId,
      Set<String> topicNames) throws SmartSpacesException {
    if (outputRouteDescriptions.containsKey(channelId)) {
      throw new SimpleSmartSpacesException("Output channel already registered: " + channelId);
    }

    SetMultimap<String, String> protocolToTopic = parseProtocolToTopicMap(topicNames);
    // TODO(keith): Check protocols for existence and topics for syntactic
    // correctness.

    RouteDescription routeDescription = new RouteDescription(channelId, protocolToTopic);
    outputRouteDescriptions.put(channelId, routeDescription);

    return internalRegisterOutputRoute(routeDescription);
  }

  @Override
  public synchronized void
      registerInputChannelTopic(final String channelId, Set<String> topicNames)
          throws SmartSpacesException {
    if (inputRouteDescriptions.containsKey(channelId)) {
      throw new SimpleSmartSpacesException("Input channel already registered: " + channelId);
    }

    SetMultimap<String, String> protocolToTopic = parseProtocolToTopicMap(topicNames);
    // TODO(keith): Check protocols for existence and topics for syntactic
    // correctness.

    RouteDescription routeDescription = new RouteDescription(channelId, protocolToTopic);
    inputRouteDescriptions.put(channelId, routeDescription);

    internalRegisterInputRoute(routeDescription);
  }

  /**
   * Register an output route.
   * 
   * @param routeDescription
   *          the route description
   * 
   * @return the message publisher
   */
  protected abstract InternalRouteMessagePublisher internalRegisterOutputRoute(
      RouteDescription routeDescription);

  /**
   * Register an input route.
   * 
   * @param routeDescription
   *          the route description
   */
  protected abstract void internalRegisterInputRoute(RouteDescription routeDescription);

  /**
   * Parse topic names out of a string.
   *
   * @param topicNames
   *          the topic names to parse
   *
   * @return the set of unique names
   */
  protected Set<String> parseTopicNames(String topicNames) {
    Set<String> topicNameSet = new HashSet<>();

    for (String topicName : topicNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
      topicName = topicName.trim();
      if (!topicName.isEmpty()) {
        topicNameSet.add(topicName);
      }
    }

    return topicNameSet;
  }

  /**
   * Is the supplied topic name a syntactically correct topic name?
   *
   * @param topicName
   *          the topic name to check
   *
   * @return {@code true if syntactically correct

   */
  protected boolean isSyntacticallyCorrectTopicName(String topicName) {
    return GraphName.VALID_GRAPH_NAME_PATTERN.matcher(topicName).matches();
  }

  /**
   * Do any specialty configuration needed by the implementing class.
   *
   * @param configuration
   *          the configuration
   */
  protected void onConfigureComponent(Configuration configuration) {
    // Default is to do nothing
  }

  /**
   * Starting the {@link #startupComponent()} method.
   */
  protected void onPreStartupComponent() {
    // Default is to do nothing
  }

  /**
   * Finishing up the {@link #startupComponent()} method.
   */
  protected void onPostStartupComponent() {
    // Default is to do nothing
  }

  /**
   * Parse a set of topic names to a map of protocol to the actual topics.
   * 
   * @param topicNames
   *          the topic names, potentially with protocol prefixes.
   * 
   * @return the map of protocols to the topics covered by that protocol, the
   *         topics will not have the protocol designator
   */
  protected SetMultimap<String, String> parseProtocolToTopicMap(Set<String> topicNames) {
    SetMultimap<String, String> protocolToTopic = HashMultimap.create();
    for (String topicName : topicNames) {
      String protocolName = defaultRouteProtocol;
      int colonPos = topicName.indexOf(':');
      if (colonPos != -1) {
        protocolName = topicName.substring(0, colonPos);
        topicName = topicName.substring(colonPos + 1);
      }
      protocolToTopic.put(protocolName, topicName);
    }

    return protocolToTopic;
  }

  @Override
  public Set<String> getOutputChannelIds() {
    return Sets.newHashSet(outputRouteDescriptions.keySet());
  }

  @Override
  public Set<String> getInputChannelIds() {
    return Sets.newHashSet(inputRouteDescriptions.keySet());
  }

  @Override
  public String getComponentStatusDetail() {
    Map<String, String> sortedRoutes = new TreeMap<>();
    for (RouteDescription input : inputRouteDescriptions.values()) {
      String key = input.getChannelId();
      sortedRoutes.put(
          key + ">",
          makeRouteDetail("input-route", key, StatusDetail.ARROW_LEFT, input.getProtocolToTopic()
              .toString()));
    }
    for (RouteDescription output : outputRouteDescriptions.values()) {
      String key = output.getChannelId();
      sortedRoutes.put(
          key + "<",
          makeRouteDetail("output-route", key, StatusDetail.ARROW_RIGHT, output
              .getProtocolToTopic().toString()));
    }
    String nodeName = getNodeName();
    return String.format(StatusDetail.HEADER_FORMAT, "route-detail")
        + makeRouteDetail("node-name", "Node Name", StatusDetail.ITEM_IS, nodeName) + "\n"
        + Joiner.on("\n").join(sortedRoutes.values()) + StatusDetail.FOOTER;
  }

  /**
   * Function to format the various parts of a status row together into a single
   * entity.
   *
   * @param className
   *          class name for the row
   * @param key
   *          key for entry
   * @param bridge
   *          bridge string between key/value
   * @param value
   *          value for the entry
   *
   * @return formatted line for a route detail
   */
  private String makeRouteDetail(String className, String key, String bridge, String value) {
    return String.format(StatusDetail.PREFIX_FORMAT, className) + key + StatusDetail.SEPARATOR
        + bridge + StatusDetail.SEPARATOR + value + StatusDetail.POSTFIX;
  }

}
