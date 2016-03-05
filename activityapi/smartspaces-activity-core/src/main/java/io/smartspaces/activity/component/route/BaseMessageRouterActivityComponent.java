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
import io.smartspaces.activity.component.BaseActivityComponent;
import io.smartspaces.activity.component.route.ros.RosMessageRouterActivityComponent;
import io.smartspaces.configuration.Configuration;

import com.google.common.collect.Lists;
import org.ros.namespace.GraphName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helpful superclass for implementors of the ROS message routing activity
 * component.
 *
 * @param <T>
 *          the type of messages handled by the component
 *
 * @author Keith M. Hughes
 */
public abstract class BaseMessageRouterActivityComponent extends BaseActivityComponent
    implements RosMessageRouterActivityComponent {

  /**
   * The listeners for this component.
   */
  private final List<MessageRouterActivityComponentListener> listeners = Lists
      .newCopyOnWriteArrayList();

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

    String inputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_INPUTS);
    if (inputNames != null) {
      inputNames = inputNames.trim();
      for (String inputName : inputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX + inputName;
        String topicNames = configuration.getPropertyString(propertyName);
        if (topicNames != null) {
          for (String topicName : parseTopicNames(topicNames)) {
            if (!isSyntacticallyCorrectTopicName(topicName)) {
              String message =
                  String.format("Input route topic name %s is of the wrong form", topicName);
              handleError(message, null);
              routeErrors.append(routeErrors.length() > 0 ? ", " : "").append(message);
            }
          }
        } else {
          handleError(String.format("Input route %s not defined, missing topic configuration %s",
              inputName, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing input route=")
              .append(inputName).append(" which is defined by configuration property ")
              .append(propertyName);
        }
      }
    }

    String outputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_OUTPUTS);
    if (outputNames != null) {
      outputNames = outputNames.trim();
      for (String outputName : outputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX + outputName;
        String topicNames = configuration.getPropertyString(propertyName);
        if (topicNames != null) {
          for (String topicName : parseTopicNames(topicNames)) {
            if (!isSyntacticallyCorrectTopicName(topicName)) {
              String message =
                  String.format("Output route topic name %s is of the wrong form", topicName);
              handleError(message, null);
              routeErrors.append(routeErrors.length() > 0 ? ", " : "").append(message);
            }
          }
        } else {
          handleError(String.format("Output route %s not defined, missing topic configuration %s",
              outputName, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing output route=")
              .append(outputName).append(" which is defined by configuration property ")
              .append(propertyName);
        }
      }
    }

    if (routeErrors.length() > 0) {
      throw new SimpleSmartSpacesException(routeErrors.toString());
    }

    if ((inputNames == null || inputNames.isEmpty())
        && (outputNames == null || outputNames.isEmpty())) {
      throw new SimpleSmartSpacesException(String.format(
          "Router has no routes. Define either %s or %s in your configuration",
          CONFIGURATION_ROUTES_INPUTS, CONFIGURATION_ROUTES_OUTPUTS));
    }
  }

  @Override
  public void startupComponent() {
    onPreStartupComponent();

    Configuration configuration = getComponentContext().getActivity().getConfiguration();

    String inputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_INPUTS);
    if (inputNames != null) {
      for (String inputName : inputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        inputName = inputName.trim();
        if (!inputName.isEmpty()) {
          String inputTopicNames =
              configuration.getRequiredPropertyString(CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX
                  + inputName);
          registerInputChannelTopic(inputName, parseTopicNames(inputTopicNames));
        }
      }
    }

    String outputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_OUTPUTS);
    if (outputNames != null) {
      for (String outputName : outputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        outputName = outputName.trim();
        if (!outputName.isEmpty()) {
          String outputTopicNames =
              configuration.getRequiredPropertyString(CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX
                  + outputName);

          boolean latch = false;
          int semiPos = outputTopicNames.indexOf(';');
          if (semiPos != -1) {
            String extra = outputTopicNames.substring(0, semiPos);
            outputTopicNames = outputTopicNames.substring(semiPos + 1);

            String[] pair = extra.split("=");
            if (pair.length > 1) {
              if ("latch".equals(pair[0].trim())) {
                latch = "true".equals(pair[1].trim());
              }
            }
          }

          registerOutputChannelTopic(outputName, parseTopicNames(outputTopicNames), latch);
        }
      }
    }

    onPostStartupComponent();
  }

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
}
