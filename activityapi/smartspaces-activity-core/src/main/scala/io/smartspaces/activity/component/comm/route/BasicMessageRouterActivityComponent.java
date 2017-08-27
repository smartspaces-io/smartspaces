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

package io.smartspaces.activity.component.comm.route;

import io.smartspaces.activity.component.comm.PubSubActivityComponent;
import io.smartspaces.activity.component.comm.ros.RosActivityComponent;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.handler.ProtectedHandlerContext;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.route.MessageRouter;
import io.smartspaces.messaging.route.RouteDescription;
import io.smartspaces.messaging.route.RouteMessageListener;
import io.smartspaces.messaging.route.RouteMessagePublisher;
import io.smartspaces.messaging.route.StandardMessageRouter;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.time.provider.TimeProvider;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The basic implementation of the ROS message router activity component.
 *
 * @author Keith M. Hughes
 */
public class BasicMessageRouterActivityComponent extends BaseMessageRouterActivityComponent {

  /**
   * The ROS activity component this component requires.
   */
  private RosActivityComponent rosActivityComponent;

  /**
   * The catchall listener for input messages.
   */
  private RouteMessageListener catchallMessageListener;

  /**
   * The messages listeners for specific channel IDs.
   */
  private Map<String, RouteMessageListener> messageListeners = new HashMap<>();

  /**
   * {@code true} if the component is "running", false otherwise.
   */
  private volatile boolean running;

  /**
   * The message router to be used.
   */
  private StandardMessageRouter messageRouter;

  /**
   * The time provider for the component.
   */
  private TimeProvider timeProvider;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public String getDescription() {
    return COMPONENT_DESCRIPTION;
  }

  @Override
  public List<String> getBaseDependencies() {
    return BASE_COMPONENT_DEPENDENCIES;
  }

  @Override
  public void setRoutableInputMessageListener(RouteMessageListener messageListener) {
    this.catchallMessageListener = messageListener;
  }

  @Override
  public void addRoutableInputMessageListener(String channelId,
      RouteMessageListener messageListener) {
    if (running) {
      messageRouter.addRoutableInputMessageListener(channelId, messageListener);
    } else {
      messageListeners.put(channelId, messageListener);
    }
  }

  @Override
  protected void onConfigureComponent(Configuration configuration) {
    // TODO(keith): Make this lazily evaluated.
    // rosActivityComponent =
    // componentContext.getRequiredActivityComponent(RosActivityComponent.COMPONENT_NAME);
  }

  @Override
  protected void onPreStartupComponent() {
    log = getComponentContext().getActivity().getLog();
    timeProvider = getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();

    messageRouter = new StandardMessageRouter(new BasicMessageRouterProtectedHandlerContext(),
        timeProvider, log);
    Configuration configuration = componentContext.getActivity().getConfiguration();
    messageRouter.setHostId(
        configuration.getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOSTID));
    messageRouter.setNodeName(configuration.getRequiredPropertyString(
        PubSubActivityComponent.CONFIGURATION_NAME_ACTIVITY_PUBSUB_NODE_NAME));

    // messageRouter.setRosNode(rosActivityComponent.getNode());
    messageRouter.setRoutableInputMessageListener(catchallMessageListener);
    messageRouter.addRoutableInputMessageListeners(messageListeners);

    messageRouter.setMqttBrokerDescriptionDefault(getMqttBrokerDescription());
  }

  @Override
  public void onPostStartupComponent() {
    running = true;
  }

  @Override
  public void shutdownComponent() {
    running = false;

    log.info("Shutting down message router activity component");

    TimeProvider timeProvider =
        getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
    long timeStart = timeProvider.getCurrentTime();

    messageRouter.clearAllChannelTopics();

    if (log.isInfoEnabled()) {
      log.info(String.format("Message router activity component shut down in %d msecs",
          timeProvider.getCurrentTime() - timeStart));
    }
  }

  @Override
  public boolean isComponentRunning() {
    return running;
  }

  /**
   * Get the MQTT broker description.
   * 
   * @return the MQTT broker description, or {@code null} if none defined
   */
  private MqttBrokerDescription getMqttBrokerDescription() {
    String mqttBrokerDescriptionString = getComponentContext().getActivity().getConfiguration()
        .getPropertyString(CONFIGURATION_NAME_MESSAGING_MQTT_BROKERDESCRIPTION_DEFAULT);
    if (mqttBrokerDescriptionString != null) {
      return MqttBrokerDescription$.MODULE$.parse(mqttBrokerDescriptionString);
    } else {
      return null;
    }
  }

  @Override
  public String getNodeName() {
    return messageRouter.getNodeName();
  }

  @Override
  public void sendMessage(String outputChannelId, Map<String, Object> message) {
    messageRouter.sendMessage(outputChannelId, message);
  }

  @Override
  public void clearAllChannelTopics() {
    messageRouter.clearAllChannelTopics();
  }

  @Override
  public RouteMessagePublisher getMessagePublisher(String outputChannelId) {
    return messageRouter.getMessagePublisher(outputChannelId);
  }

  @Override
  public MessageRouter getMessageRouter() {
    return messageRouter;
  }

  @Override
  protected RouteMessagePublisher internalRegisterOutputRoute(RouteDescription routeDescription) {
    return messageRouter.registerOutputChannelTopic(routeDescription);
  }

  @Override
  protected void internalRegisterInputRoute(RouteDescription routeDescription) {
    messageRouter.registerInputChannelTopic(routeDescription);
  }

  /**
   * The protected handler context for this activity component.
   * 
   * @author Keith M. Hughes
   */
  public class BasicMessageRouterProtectedHandlerContext implements ProtectedHandlerContext {
    @Override
    public boolean canHandlerRun() {
      return getComponentContext().canHandlerRun();
    }

    @Override
    public void enterHandler() {
      getComponentContext().enterHandler();
    }

    @Override
    public void exitHandler() {
      getComponentContext().exitHandler();
    }

    @Override
    public void handleHandlerError(String message, Throwable t) {
      handleError(message, t);
    }
  }
}
