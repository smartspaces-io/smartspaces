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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.ros.message.MessageListener;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import io.smartspaces.activity.component.ros.RosActivityComponent;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.messaging.codec.MessageDecoder;
import io.smartspaces.messaging.codec.MessageEncoder;
import io.smartspaces.messaging.route.CompositeRouteMessagePublisher;
import io.smartspaces.messaging.route.CompositeRouteMessageSubscriber;
import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.messaging.route.InternalRouteMessageSubscriber;
import io.smartspaces.messaging.route.MapByteArrayCodec;
import io.smartspaces.messaging.route.RouteMessagePublisher;
import io.smartspaces.messaging.route.RouteMessageSubscriber;
import io.smartspaces.messaging.route.mqtt.MqttRouteMessagePublisher;
import io.smartspaces.messaging.route.mqtt.MqttRouteMessageSubscriber;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageDecoder;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageEncoder;
import io.smartspaces.messaging.route.ros.RosRouteMessagePublisher;
import io.smartspaces.messaging.route.ros.RosRouteMessageSubscriber;
import io.smartspaces.time.TimeProvider;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;
import io.smartspaces.util.messaging.mqtt.MqttPublishers;
import io.smartspaces.util.messaging.mqtt.MqttSubscribers;
import io.smartspaces.util.messaging.mqtt.StandardMqttPublishers;
import io.smartspaces.util.messaging.mqtt.StandardMqttSubscribers;
import io.smartspaces.util.messaging.ros.RosPublishers;
import io.smartspaces.util.messaging.ros.RosSubscribers;
import io.smartspaces.util.messaging.ros.StandardRosPublishers;
import io.smartspaces.util.messaging.ros.StandardRosSubscribers;
import smartspaces_msgs.GenericMessage;

/**
 * The basic implementation of the ROS message router activity component.
 *
 * @author Keith M. Hughes
 */
public class BasicMessageRouterActivityComponent extends BaseMessageRouterActivityComponent
    implements IncomingRouteMessageHandler {

  /**
   * The ROS message type for routes.
   */
  private static final String ROS_ROUTE_MESSAGE_TYPE = GenericMessage._TYPE;

  /**
   * The ROS activity component this component requires.
   */
  private RosActivityComponent rosActivityComponent;

  /**
   * {@code true} if the component is "running", false otherwise.
   */
  private volatile boolean running;

  /**
   * The listener for input messages.
   */
  private RoutableInputMessageListener messageListener;

  /**
   * All topic inputs mapped to their subscribers.
   */
  private final Map<String, InternalRouteMessageSubscriber> inputSubscribers =
      Maps.newConcurrentMap();

  /**
   * All topic outputs mapped to their publishers.
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
  private MessageCodec<Map<String, Object>, byte[]> mqttMessageCodec = new MapByteArrayCodec();

  private MqttBrokerDescription mqttBrokerDescription;

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
  public void setRoutableInputMessageListener(RoutableInputMessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  protected void onConfigureComponent(Configuration configuration) {
    rosActivityComponent =
        componentContext.getRequiredActivityComponent(RosActivityComponent.COMPONENT_NAME);
  }

  @Override
  public void onPostStartupComponent() {
    running = true;
  }

  @Override
  public void shutdownComponent() {
    running = false;

    Log log = getComponentContext().getActivity().getLog();
    log.info("Shutting down ROS message router activity component");

    long timeStart = System.currentTimeMillis();

    clearAllChannelTopics();

    if (log.isInfoEnabled()) {
      log.info(String.format("ROS message router activity component shut down in %d msecs",
          System.currentTimeMillis() - timeStart));
    }
  }

  @Override
  public boolean isComponentRunning() {
    return running;
  }

  @Override
  public String getNodeName() {
    return rosActivityComponent.getNodeName();
  }

  @Override
  protected InternalRouteMessagePublisher
      internalRegisterOutputRoute(RouteDescription routeDescription) {
    String channelId = routeDescription.getChannelId();
    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    List<InternalRouteMessagePublisher> routeMessagePublishers = new ArrayList<>();
    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosPublishers<GenericMessage> publishers =
            new StandardRosPublishers<GenericMessage>(getComponentContext().getActivity().getLog());
        publishers.addPublishers(rosActivityComponent.getNode(), ROS_ROUTE_MESSAGE_TYPE,
            topicNamesForProtocol);

        if (rosMessageEncoder == null) {
          rosMessageEncoder = new MapGenericMessageMessageEncoder(publishers);
        }

        routeMessagePublishers
            .add(new RosRouteMessagePublisher(channelId, publishers, rosMessageEncoder));
      } else if ("mqtt".equals(routeProtocol)) {
        MqttPublishers<Map<String, Object>> publishers =
            new StandardMqttPublishers<Map<String, Object>>(getNodeName(), mqttMessageCodec,
                getComponentContext().getActivity().getLog());
        publishers.addPublishers(getMqttBrokerDescription(), topicNamesForProtocol);

        routeMessagePublishers.add(new MqttRouteMessagePublisher(channelId, publishers));
      } else {
        getComponentContext().getActivity().getLog()
            .warn(String.format("unknown route protocol %s", routeProtocol));
      }
    }

    InternalRouteMessagePublisher routeMessagePublisher = null;
    int size = routeMessagePublishers.size();
    if (size > 1) {
      routeMessagePublisher = new CompositeRouteMessagePublisher(channelId, routeMessagePublishers,
          getComponentContext().getActivity().getLog());
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
  protected void internalRegisterInputRoute(RouteDescription routeDescription) {
    String channelId = routeDescription.getChannelId();
    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    List<InternalRouteMessageSubscriber> routeMessageSubscribers = new ArrayList<>();

    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosSubscribers<GenericMessage> subscribers = new StandardRosSubscribers<GenericMessage>(
            getComponentContext().getActivity().getLog());

        final RosRouteMessageSubscriber rosRouteMessageSubscriber =
            new RosRouteMessageSubscriber(channelId, subscribers, rosMessageDecoder);
        routeMessageSubscribers.add(rosRouteMessageSubscriber);

        subscribers.addSubscribers(rosActivityComponent.getNode(), ROS_ROUTE_MESSAGE_TYPE,
            topicNamesForProtocol, new MessageListener<GenericMessage>() {
              @Override
              public void onNewMessage(GenericMessage message) {
                handleNewIncomingMessage(message, rosRouteMessageSubscriber);
              }
            });
      } else if ("mqtt".equals(routeProtocol)) {
        MqttSubscribers subscribers = new StandardMqttSubscribers(getNodeName(),
            getComponentContext().getActivity().getLog());

        final MqttRouteMessageSubscriber mqttRouteMessageSubscriber =
            new MqttRouteMessageSubscriber(channelId, subscribers, mqttMessageCodec);
        routeMessageSubscribers.add(mqttRouteMessageSubscriber);

        subscribers.addSubscribers(getMqttBrokerDescription(), topicNamesForProtocol,
            new MqttCallback() {
              @Override
              public void connectionLost(Throwable cause) {
                getComponentContext().getActivity().getLog().warn("Lost connection to MQTT broker");
              }

              @Override
              public void messageArrived(String topic, MqttMessage message) throws Exception {
                handleNewIncomingMessage(message.getPayload(), mqttRouteMessageSubscriber);
              }

              @Override
              public void deliveryComplete(IMqttDeliveryToken token) {
                // Not used since not publishing
              }
            });
      }

    }

    InternalRouteMessageSubscriber routeMessageSubscriber = null;
    int size = routeMessageSubscribers.size();
    if (size > 1) {
      routeMessageSubscriber = new CompositeRouteMessageSubscriber(channelId,
          routeMessageSubscribers, getComponentContext().getActivity().getLog());
    } else if (size == 1) {
      routeMessageSubscriber = routeMessageSubscribers.get(0);
    }

    if (routeMessageSubscriber != null) {
      inputSubscribers.put(channelId, routeMessageSubscriber);
    }
  }

  /**
   * Get the MQTT broker description.
   * 
   * @return the MQTT broker description
   */
  private MqttBrokerDescription getMqttBrokerDescription() {
    if (mqttBrokerDescription == null) {
      mqttBrokerDescription =
          new MqttBrokerDescription(getComponentContext().getActivity().getConfiguration()
              .getRequiredPropertyString(CONFIGURATION_MESSAGING_MQTT_BROKERDESCRIPTION_DEFAULT));
    }

    return mqttBrokerDescription;
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

  @Override
  public void handleNewIncomingMessage(Object message, RouteMessageSubscriber subscriber) {
    if (!getComponentContext().canHandlerRun()) {
      return;
    }

    String channelId = subscriber.getChannelId();

    try {
      getComponentContext().enterHandler();

      TimeProvider timeProvider =
          getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
      long start = timeProvider.getCurrentTime();
      String handlerInvocationId = newHandlerInvocationId();
      Log log = getComponentContext().getActivity().getLog();
      if (log.isTraceEnabled()) {
        log.trace(
            String.format("Entering ROS route message handler invocation %s", handlerInvocationId));
      }

      // Send the message out to the listener.
      Map<String, Object> msg = subscriber.decodeMessage(message);
      messageListener.onNewRoutableInputMessage(channelId, msg);

      if (log.isTraceEnabled()) {
        log.trace(String.format("Exiting ROS route message handler invocation %s after %dms",
            handlerInvocationId, timeProvider.getCurrentTime() - start));
      }
    } catch (Throwable e) {
      handleError(String.format("Error after receiving routing message for channel %s", channelId),
          e);
    } finally {
      getComponentContext().exitHandler();
    }
  }

  @Override
  public void writeOutputMessage(String outputChannelId, Map<String, Object> message) {
    try {
      getComponentContext().enterHandler();

      if (outputChannelId != null) {
        RouteMessagePublisher output = outputPublishers.get(outputChannelId);
        if (output != null) {
          output.writeOutputMessage(message);

        } else {
          handleError(
              String.format("Unknown route output channel %s. Message dropped.", outputChannelId),
              null);
        }
      } else {
        handleError("Route output channel has no name. Message dropped.", null);
      }
    } catch (Throwable e) {
      handleError(String.format("Error writing message on channel %s", outputChannelId), e);
    } finally {
      getComponentContext().exitHandler();
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

  @Override
  public RouteMessagePublisher getMessagePublisher(String outputChannelId) {
    return outputPublishers.get(outputChannelId);
  }
}
