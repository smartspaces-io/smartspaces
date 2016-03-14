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

package io.smartspaces.activity.component.route.ros;

import io.smartspaces.activity.component.ros.RosActivityComponent;
import io.smartspaces.activity.component.route.BaseMessageRouterActivityComponent;
import io.smartspaces.activity.component.route.RoutableInputMessageListener;
import io.smartspaces.activity.component.route.RouteDescription;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.messaging.MessageDecoder;
import io.smartspaces.messaging.MessageEncoder;
import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.messaging.route.RouteMessagePublisher;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageDecoder;
import io.smartspaces.messaging.route.ros.MapGenericMessageMessageEncoder;
import io.smartspaces.messaging.route.ros.RosRouteMessagePublisher;
import io.smartspaces.time.TimeProvider;
import io.smartspaces.util.ros.BasePublisherListener;
import io.smartspaces.util.ros.BaseSubscriberListener;
import io.smartspaces.util.ros.RosPublishers;
import io.smartspaces.util.ros.RosSubscribers;
import io.smartspaces.util.ros.StandardRosPublishers;
import io.smartspaces.util.ros.StandardRosSubscribers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

import smartspaces_msgs.GenericMessage;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

/**
 * The basic implementation of the ROS message router activity component.
 *
 * @author Keith M. Hughes
 */
public class BasicRosMessageRouterActivityComponent extends BaseMessageRouterActivityComponent {

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
  private final RoutableInputMessageListener messageListener;

  /**
   * The name of the ROS message type.
   */
  private final String rosMessageType;

  /**
   * All topic inputs mapped to their subscribers.
   */
  private final Map<String, RosSubscribers<GenericMessage>> inputSubscribers = Maps
      .newConcurrentMap();

  /**
   * All topic outputs mapped to their publishers.
   */
  private final Map<String, InternalRouteMessagePublisher> outputPublishers = Maps
      .newConcurrentMap();

  /**
   * A creator for handler invocation IDs.
   */
  private final AtomicLong handlerInvocationId = new AtomicLong();

  /**
   * The publisher listener to get events to this router instance.
   */
  private final PublisherListener<GenericMessage> publisherListener =
      new BasePublisherListener<GenericMessage>() {
        @Override
        public void onNewSubscriber(Publisher<GenericMessage> publisher,
            SubscriberIdentifier subscriberIdentifier) {
          handleNewSubscriber(publisher, subscriberIdentifier);
        }
      };

  /**
   * The subscriber listener to get events to this router instance.
   */
  private final SubscriberListener<GenericMessage> subscriberListener =
      new BaseSubscriberListener<GenericMessage>() {
        @Override
        public void onNewPublisher(Subscriber<GenericMessage> subscriber,
            PublisherIdentifier publisherIdentifier) {
          handleNewPublisher(subscriber, publisherIdentifier);
        }
      };

  /**
   * The decoder for route messages to ROS messages.
   */
  private MessageDecoder<Map<String, Object>, GenericMessage> messageDecoder =
      new MapGenericMessageMessageDecoder();

  /**
   * The decoder for route messages to ROS messages.
   */
  private MessageEncoder<Map<String, Object>, GenericMessage> messageEncoder;

  /**
   * Construct a new ROS message router activity component.
   *
   * @param rosMessageType
   *          the ROS message type for the route
   * @param messageListener
   *          the listener for message events
   */
  public BasicRosMessageRouterActivityComponent(String rosMessageType,
      RoutableInputMessageListener messageListener) {
    this.rosMessageType = rosMessageType;
    this.messageListener = messageListener;
  }

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
  protected InternalRouteMessagePublisher internalRegisterOutputRoute(
      RouteDescription routeDescription) {
    String channelId = routeDescription.getChannelId();
    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    List<InternalRouteMessagePublisher> routeMessagePublishers = new ArrayList<>();
    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosPublishers<GenericMessage> publishers =
            new StandardRosPublishers<GenericMessage>(getComponentContext().getActivity().getLog());
        publishers.addPublisherListener(publisherListener);
        publishers.addPublishers(rosActivityComponent.getNode(), rosMessageType,
            topicNamesForProtocol);

        if (messageEncoder == null) {
          messageEncoder = new MapGenericMessageMessageEncoder(publishers);
        }

        routeMessagePublishers.add(new RosRouteMessagePublisher(channelId, publishers,
            messageEncoder));
      }

    }

    outputPublishers.put(channelId, routeMessagePublishers.get(0));

    return routeMessagePublishers.get(0);
  }

  @Override
  protected void internalRegisterInputRoute(RouteDescription routeDescription) {
    final String channelId = routeDescription.getChannelId();
    SetMultimap<String, String> protocolToTopic = routeDescription.getProtocolToTopic();

    for (String routeProtocol : protocolToTopic.keySet()) {
      Set<String> topicNamesForProtocol = protocolToTopic.get(routeProtocol);
      if ("ros".equals(routeProtocol)) {
        RosSubscribers<GenericMessage> subscribers =
            new StandardRosSubscribers<GenericMessage>(getComponentContext().getActivity().getLog());
        subscribers.addSubscriberListener(subscriberListener);

        subscribers.addSubscribers(rosActivityComponent.getNode(), rosMessageType,
            topicNamesForProtocol, new MessageListener<GenericMessage>() {
              @Override
              public void onNewMessage(GenericMessage message) {
                handleNewIncomingMessage(channelId, message);
              }
            });

        inputSubscribers.put(channelId, subscribers);
      }
    }
  }

  @Override
  public synchronized void clearAllChannelTopics() {
    for (RosSubscribers<GenericMessage> input : inputSubscribers.values()) {
      input.shutdown();
    }
    inputSubscribers.clear();

    for (InternalRouteMessagePublisher output : outputPublishers.values()) {
      output.shutdown();
    }
    outputPublishers.clear();
  }

  /**
   * Handle a new route message.
   *
   * @param channelId
   *          ID of the channel the message came in on
   * @param message
   *          the message that came in
   */
  void handleNewIncomingMessage(String channelId, GenericMessage message) {
    if (!getComponentContext().canHandlerRun()) {
      return;
    }

    try {
      getComponentContext().enterHandler();

      TimeProvider timeProvider =
          getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
      long start = timeProvider.getCurrentTime();
      String handlerInvocationId = newHandlerInvocationId();
      Log log = getComponentContext().getActivity().getLog();
      if (log.isTraceEnabled()) {
        log.trace(String.format("Entering ROS route message handler invocation %s",
            handlerInvocationId));
      }

      // Send the message out to the listener.
      Map<String, Object> msg = messageDecoder.decode(message);
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

  /**
   * Handle the event of a new subscriber coming in for a publisher.
   *
   * @param publisher
   *          the publisher which has received a new subscriber
   * @param subscriberIdentifier
   *          the identifier for the new subscriber
   */
  private void handleNewSubscriber(Publisher<GenericMessage> publisher,
      SubscriberIdentifier subscriberIdentifier) {
    // String topicName = publisher.getTopicName().toString();
    // String subscriberName =
    // subscriberIdentifier.getNodeIdentifier().getName().toString();
    //
    // for (MessageRouterActivityComponentListener listener : listeners) {
    // try {
    // listener.onNewSubscriber(topicName, subscriberName);
    // } catch (Exception e) {
    // getComponentContext().getActivity().getLog().error("Error notifying
    // listener about new subscriber",
    // e);
    // }
    // }
  }

  /**
   * Handle the event of a new publisher coming in for a subscriber.
   *
   * @param subscriber
   *          the subscriber which has received a new publisher
   * @param publisherIdentifier
   *          the identifier for the new publisher
   */
  private void handleNewPublisher(Subscriber<GenericMessage> subscriber,
      PublisherIdentifier publisherIdentifier) {
    // String topicName = subscriber.getTopicName().toString();
    // String publisherName =
    // publisherIdentifier.getNodeIdentifier().getName().toString();
    //
    // for (MessageRouterActivityComponentListener listener : listeners) {
    // try {
    // listener.onNewPublisher(topicName, publisherName);
    // } catch (Exception e) {
    // getComponentContext().getActivity().getLog().error("Error notifying
    // listener about new publisher",
    // e);
    // }
    // }
  }
}
