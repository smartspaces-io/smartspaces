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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.component.comm.PubSubActivityComponent;
import io.smartspaces.handler.ProtectedHandlerContext;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MapByteArrayMessageCodec;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.messaging.codec.MessageDecoder;
import io.smartspaces.messaging.codec.MessageEncoder;
import io.smartspaces.messaging.route.mqtt.MqttRouteMessagePublisher;
import io.smartspaces.messaging.route.mqtt.MqttRouteMessageSubscriber;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageDecoder;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageEncoder;
import io.smartspaces.messaging.route.ros.RosRouteMessagePublisher;
import io.smartspaces.messaging.route.ros.RosRouteMessageSubscriber;
import io.smartspaces.time.provider.TimeProvider;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;
import io.smartspaces.util.messaging.mqtt.MqttPublishers;
import io.smartspaces.util.messaging.mqtt.MqttSubscribers;
import io.smartspaces.util.messaging.mqtt.PahoMqttClient;
import io.smartspaces.util.messaging.mqtt.StandardMqttPublishers;
import io.smartspaces.util.messaging.mqtt.StandardMqttSubscribers;
import io.smartspaces.util.messaging.ros.RosPublishers;
import io.smartspaces.util.messaging.ros.RosSubscribers;
import io.smartspaces.util.messaging.ros.StandardRosPublishers;
import io.smartspaces.util.messaging.ros.StandardRosSubscribers;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import smartspaces_msgs.GenericMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The standard message router.
 * 
 * <p>
 * Handles all standard route protocols, such as MQTT and ROS.
 * 
 * @author Keith M. Hughes
 */
public class StandardMessageRouter implements MessageRouter {

  /**
   * The ROS message type for routes.
   */
  private static final String ROS_ROUTE_MESSAGE_TYPE = GenericMessage._TYPE;

  /**
   * The default route protocol.
   */
  private String defaultRouteProtocol = DEFAULT_ROUTE_PROTOCOL_DEFAULT;

  /**
   * The handler for input messages for messages not explicitly handled
   * {@see #messageHandlers}.
   */
  private RouteMessageHandler defaultMessageHandler;

  /**
   * The messages listeners for specific channel IDs.
   */
  private Map<String, RouteMessageHandler> messageListeners = new HashMap<>();

  /**
   * All input topic channel IDs mapped to their subscribers.
   */
  private final Map<String, InternalRouteMessageSubscriber> inputSubscribers =
      Maps.newConcurrentMap();

  /**
   * All output topic channel IDs mapped to their publishers.
   */
  private final Map<String, InternalRouteMessagePublisher> outputPublishers =
      Maps.newConcurrentMap();

  /**
   * A creator for handler invocation IDs.
   */
  private final AtomicLong handlerInvocationId = new AtomicLong();

  /**
   * The decoder for route messages to ROS messages.
   */
  private MessageDecoder<Map<String, Object>, GenericMessage> rosMessageDecoder =
      new MapGenericMessageMessageDecoder();

  /**
   * The decoder for route messages to ROS messages.
   */
  private MessageEncoder<Map<String, Object>, GenericMessage> rosMessageEncoder;

  /**
   * A message codec for MQTT route messages.
   */
  private MessageCodec<Map<String, Object>, byte[]> mqttMessageCodec = new MapByteArrayMessageCodec();

  /**
   * The description for the default MQTT broker.
   * 
   * <p>
   * Will be {@code null} if none defined.
   */
  private MqttBrokerDescription mqttBrokerDescriptionDefault;

  /**
   * The route descriptions for all inputs keyed by channel IDs.
   */
  private Map<String, RouteDescription> inputRouteDescriptions = new HashMap<>();

  /**
   * The route descriptions for all outputs keyed by channel IDs.
   */
  private Map<String, RouteDescription> outputRouteDescriptions = new HashMap<>();

  /**
   * The route descriptions for all outputs keyed by channel IDs.
   */
  private Map<MqttBrokerDescription, PahoMqttClient> brokerDescriptionToMqttClient =
      new HashMap<>();

  /**
   * Host ID of the container.
   */
  private String hostId;

  /**
   * The node name of the router.
   */
  private String nodeName;

  /**
   * The ROS node the router is using.
   */
  private ConnectedNode rosNode;

  /**
   * The context object for running protected handlers.
   */
  private ProtectedHandlerContext protectedHandlerContext;

  /**
   * The time provider for the router.
   */
  private TimeProvider timeProvider;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * Construct a new router.
   * 
   * @param protectedHandlerContext
   *          the handler context for message handling
   * @param timeProvider
   *          the time provider to use
   * @param log
   *          the logger to uses
   */
  public StandardMessageRouter(ProtectedHandlerContext protectedHandlerContext,
      TimeProvider timeProvider, ExtendedLog log) {
    this.protectedHandlerContext = protectedHandlerContext;
    this.timeProvider = timeProvider;
    this.log = log;
  }

  @Override
  public String getDefaultRouteProtocol() {
    return defaultRouteProtocol;
  }

  @Override
  public String getNodeName() {
    return nodeName;
  }

  @Override
  public boolean isOutputChannelRegistered(String channelId) {
    return outputRouteDescriptions.containsKey(channelId);
  }

  @Override
  public RouteMessageSender registerOutputChannelTopic(RouteDescription routeDescription) {
    String channelId = routeDescription.getChannelId();
    if (isOutputChannelRegistered(channelId)) {
      throw new SimpleSmartSpacesException("Output channel already registered: " + channelId);
    }

    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    List<InternalRouteMessagePublisher> routeMessagePublishers = new ArrayList<>();
    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosPublishers<GenericMessage> publishers = new StandardRosPublishers<GenericMessage>(log);
        publishers.addPublishers(rosNode, ROS_ROUTE_MESSAGE_TYPE, topicNamesForProtocol);

        if (rosMessageEncoder == null) {
          rosMessageEncoder = new MapGenericMessageMessageEncoder(publishers);
        }

        routeMessagePublishers
            .add(new RosRouteMessagePublisher(channelId, publishers, rosMessageEncoder));
      } else if ("mqtt".equals(routeProtocol)) {
        MqttPublishers<Map<String, Object>> publishers =
            new StandardMqttPublishers<Map<String, Object>>(mqttMessageCodec, log);
        publishers.addPublishers(getMqttClient(getMqttBrokerDescription()), topicNamesForProtocol);

        routeMessagePublishers.add(new MqttRouteMessagePublisher(channelId, publishers));
      } else {
        log.warn(String.format("unknown route protocol %s", routeProtocol));
      }
    }

    InternalRouteMessagePublisher routeMessagePublisher = null;
    int size = routeMessagePublishers.size();
    if (size > 1) {
      routeMessagePublisher =
          new CompositeRouteMessagePublisher(channelId, routeMessagePublishers, log);
    } else if (size == 1) {
      routeMessagePublisher = routeMessagePublishers.get(0);
    }

    if (routeMessagePublisher != null) {
      outputPublishers.put(channelId, routeMessagePublisher);
    }

    // TODO(keith): Decide what to do if no messages map properly.

    return routeMessagePublisher;
  }

  @Override
  public boolean isInputChannelRegistered(String channelId) {
    return inputRouteDescriptions.containsKey(channelId);
  }

  @Override
  public void registerInputChannelTopic(RouteDescription routeDescription) {
    String channelId = routeDescription.getChannelId();
    if (isInputChannelRegistered(channelId)) {
      throw new SimpleSmartSpacesException("Input channel already registered: " + channelId);
    }

    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    List<InternalRouteMessageSubscriber> routeMessageSubscribers = new ArrayList<>();

    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosSubscribers<GenericMessage> subscribers =
            new StandardRosSubscribers<GenericMessage>(log);

        final RosRouteMessageSubscriber rosRouteMessageSubscriber =
            new RosRouteMessageSubscriber(channelId, subscribers, rosMessageDecoder);
        routeMessageSubscribers.add(rosRouteMessageSubscriber);

        subscribers.addSubscribers(rosNode, ROS_ROUTE_MESSAGE_TYPE, topicNamesForProtocol,
            new MessageListener<GenericMessage>() {
              @Override
              public void onNewMessage(GenericMessage message) {
                handleNewMessage(message, rosRouteMessageSubscriber);
              }
            });
      } else if ("mqtt".equals(routeProtocol)) {
        // If the node name doesn't start with the component separator,
        // add in
        // the host ID.
        String mqttNodeName = nodeName;
        if (!nodeName.startsWith(PubSubActivityComponent.TOPIC_COMPONENT_SEPARATOR)) {
          mqttNodeName = hostId + PubSubActivityComponent.TOPIC_COMPONENT_SEPARATOR + nodeName;
        }

        MqttSubscribers subscribers = new StandardMqttSubscribers(mqttNodeName, log);

        final MqttRouteMessageSubscriber mqttRouteMessageSubscriber =
            new MqttRouteMessageSubscriber(channelId, subscribers, mqttMessageCodec);
        routeMessageSubscribers.add(mqttRouteMessageSubscriber);

        subscribers.addSubscribers(getMqttClient(getMqttBrokerDescription()), topicNamesForProtocol,
            new IMqttMessageListener() {

              @Override
              public void messageArrived(String topic, MqttMessage message) throws Exception {
                handleNewMessage(message.getPayload(), mqttRouteMessageSubscriber);
              }
            });
      }

    }

    InternalRouteMessageSubscriber routeMessageSubscriber = null;
    int size = routeMessageSubscribers.size();
    if (size > 1) {
      routeMessageSubscriber =
          new CompositeRouteMessageSubscriber(channelId, routeMessageSubscribers, log);
    } else if (size == 1) {
      routeMessageSubscriber = routeMessageSubscribers.get(0);
    }

    if (routeMessageSubscriber != null) {
      inputSubscribers.put(channelId, routeMessageSubscriber);
    }
  }

  @Override
  public void handleNewMessage(Object message, RouteMessageSubscriber subscriber) {
    if (!protectedHandlerContext.canHandlerRun()) {
      return;
    }

    String channelId = subscriber.getChannelId();

    try {
      protectedHandlerContext.enterHandler();

      long start = timeProvider.getCurrentTime();
      String handlerInvocationId = newHandlerInvocationId();
      if (log.isTraceEnabled()) {
        log.trace(
            String.format("Entering route message handler invocation %s", handlerInvocationId));
      }

      // Send the message out to the listener.
      Map<String, Object> msg = subscriber.decodeMessage(message);

      getAppropriateMessageListener(channelId).onNewMessage(channelId, msg);

      if (log.isTraceEnabled()) {
        log.trace(String.format("Exiting route message handler invocation %s after %dms",
            handlerInvocationId, timeProvider.getCurrentTime() - start));
      }
    } catch (Throwable e) {
      protectedHandlerContext.handleHandlerError(
          String.format("Error after receiving routing message for channel %s", channelId), e);
    } finally {
      protectedHandlerContext.exitHandler();
    }
  }

  /**
   * Get the appropriate message listener for a given channel ID.
   * 
   * @param channelId
   *          the channel ID
   * 
   * @return the appropriate listener
   */
  private RouteMessageHandler getAppropriateMessageListener(String channelId) {
    RouteMessageHandler handler = messageListeners.get(channelId);
    if (handler != null) {
      return handler;
    } else {
      return defaultMessageHandler;
    }
  }

  @Override
  public RouteMessageSender getMessagePublisher(String outputChannelId) {
    return outputPublishers.get(outputChannelId);
  }

  @Override
  public void sendMessage(String outputChannelId, Map<String, Object> message) {
    try {
      protectedHandlerContext.enterHandler();

      if (outputChannelId != null) {
        RouteMessageSender output = outputPublishers.get(outputChannelId);
        if (output != null) {
          output.sendMessage(message);

        } else {
          protectedHandlerContext.handleHandlerError(
              String.format("Unknown route output channel %s. Message dropped.", outputChannelId),
              null);
        }
      } else {
        protectedHandlerContext
            .handleHandlerError("Route output channel has no name. Message dropped.", null);
      }
    } catch (Throwable e) {
      protectedHandlerContext.handleHandlerError(
          String.format("Error writing message on channel %s", outputChannelId), e);
    } finally {
      protectedHandlerContext.exitHandler();
    }
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
  public synchronized void clearAllChannelTopics() {
    for (InternalRouteMessageSubscriber input : inputSubscribers.values()) {
      input.shutdown();
    }
    inputSubscribers.clear();

    for (InternalRouteMessagePublisher output : outputPublishers.values()) {
      output.shutdown();
    }
    outputPublishers.clear();
  }

  /**
   * Get the MQTT client allocated with a given broker description.
   * 
   * @param mqttBrokerDescription
   *          the MQTT broker description
   */
  private PahoMqttClient getMqttClient(MqttBrokerDescription mqttBrokerDescription) {
    PahoMqttClient client = brokerDescriptionToMqttClient.get(mqttBrokerDescription);
    if (client == null) {
      client = new PahoMqttClient(mqttBrokerDescription, getNodeName(), log);
      client.startup();
      
      brokerDescriptionToMqttClient.put(mqttBrokerDescription, client);
    }
    
    return client;
  }

  /**
   * Set the host ID for the router.
   * 
   * @param hostId
   *          the host ID
   */
  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  /**
   * Set the node name for the router.
   * 
   * @param nodeName
   *          the node name
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  /**
   * Set the ROS node for the router.
   * 
   * @param rosNode
   *          the ROS node name
   */
  public void setRosNode(ConnectedNode rosNode) {
    this.rosNode = rosNode;
  }

  /**
   * Set the MQTT broker description.
   * 
   * @param mqttBrokerDescriptionDefault
   *          the description
   */
  public void setMqttBrokerDescriptionDefault(MqttBrokerDescription mqttBrokerDescriptionDefault) {
    this.mqttBrokerDescriptionDefault = mqttBrokerDescriptionDefault;
  }

  @Override
  public void setDefaultRoutableInputMessageHandler(RouteMessageHandler messageHandler) {
    this.defaultMessageHandler = messageHandler;
  }

  @Override
  public void addRoutableInputMessageHandler(String channelId,
      RouteMessageHandler messageListener) {
    messageListeners.put(channelId, messageListener);
  }

  @Override
  public void addRoutableInputMessageHandlers(Map<String, RouteMessageHandler> messageHandlers) {
    messageListeners.putAll(messageHandlers);
  }

  /**
   * Set the default route protocol.
   * 
   * @param defaultRouteProtocol
   *          the default route protocol
   */
  public void setDefaultRouteProtocol(String defaultRouteProtocol) {
    this.defaultRouteProtocol = defaultRouteProtocol;
  }

  /**
   * Get the MQTT broker description.
   * 
   * @return the MQTT broker description
   * 
   * @throws SmartSpacesException
   *           no description is found
   */
  private MqttBrokerDescription getMqttBrokerDescription() throws SmartSpacesException {
    if (mqttBrokerDescriptionDefault != null) {
      return mqttBrokerDescriptionDefault;
    } else {
      throw new SmartSpacesException("No default MQTT broker defined");
    }
  }

  /**
   * Create a new handler invocation ID.
   *
   * @return a unique ID for the handler invocation
   */
  private String newHandlerInvocationId() {
    return Long.toHexString(handlerInvocationId.getAndIncrement());
  }
}
