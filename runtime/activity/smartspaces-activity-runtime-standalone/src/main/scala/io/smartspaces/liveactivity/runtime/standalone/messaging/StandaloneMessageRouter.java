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

package io.smartspaces.liveactivity.runtime.standalone.messaging;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.SupportedActivity;
import io.smartspaces.activity.component.comm.ros.RosActivityComponent;
import io.smartspaces.activity.component.comm.route.BaseMessageRouterActivityComponent;
import io.smartspaces.activity.component.comm.route.MessageRouterActivityComponent;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.standalone.development.DevelopmentStandaloneLiveActivityRuntime;
import io.smartspaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;
import io.smartspaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageSetList;
import io.smartspaces.messaging.route.InternalRouteMessagePublisher;
import io.smartspaces.messaging.route.MessageRouter;
import io.smartspaces.messaging.route.MessageRouterSupportedMessageTypes;
import io.smartspaces.messaging.route.RouteMessageHandler;
import io.smartspaces.messaging.route.RouteDescription;
import io.smartspaces.messaging.route.RouteMessageSender;
import io.smartspaces.messaging.route.RouteMessageSubscriber;
import io.smartspaces.time.provider.TimeProvider;
import io.smartspaces.util.data.mapper.JsonDataMapper;
import io.smartspaces.util.data.mapper.StandardJsonDataMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A standalone message router that uses multicast.
 *
 * @author Trevor Pering
 */
public class StandaloneMessageRouter extends BaseMessageRouterActivityComponent {

  /**
   * Json mapper for message conversion.
   */
  private static final JsonDataMapper MAPPER = StandardJsonDataMapper.INSTANCE;

  /**
   * Key for message delay field.
   */
  public static final String TIME_DELAY_KEY = "delay";

  /**
   * Key for message source uuid field.
   */
  public static final String SOURCE_UUID_KEY = "sourceUuid";

  /**
   * Key for message segment name.
   */
  public static final String SEGMENT_KEY = "segment";

  /**
   * Key for message content.
   */
  public static final String MESSAGE_KEY = "message";

  /**
   * Router for, well, route messages.
   */
  private StandaloneRouter router;

  /**
   * Array to hold last message timestamps.
   */
  private long[] lastMessageTime = { -1, -1 };

  /**
   * List of whitelisted messages for trace capture.
   */
  private MessageSetList messageWhiteList = new MessageSetList();

  /**
   * The standalone activity runner that owns this message router.
   */
  private final DevelopmentStandaloneLiveActivityRuntime activityRunner;

  /**
   * A route input message listener for those not handled explicitly.
   */
  private RouteMessageHandler defaultMessageHandler;

  /**
   * Route input message listeners keyed by the channel ID they are supposed to handle.
   */
  private Map<String, RouteMessageHandler> messageHandlers = new HashMap<>();

  /**
   * Output for tracing received messages.
   */
  private PrintWriter receiveTraceWriter;

  /**
   * Output for tracing sent messages.
   */
  private PrintWriter sendTraceWriter;

  /**
   * Runner for checking produced messages.
   */
  private MessageCheckRunner messageCheckRunner;

  /**
   * Runner for checking playback messages.
   */
  private PlaybackRunner playbackRunner;

  /**
   * The activity that this router is supporting.
   */
  private SupportedActivity activity;

  /**
   * Mark time of when to check for completion (failure), in milliseconds.
   */
  private volatile long finishTime;

  /**
   * All topic inputs.
   */
  private final Multimap<String, String> inputRoutesToChannels = ArrayListMultimap.create();

  /**
   * All topic outputs.
   */
  private final Map<String, Set<String>> outputChannelsToRoutes = Maps.newConcurrentMap();

  /**
   * Time provider for message timestamps.
   */
  private TimeProvider timeProvider;

  /**
   * The message router for this component.
   */
  private MyMessageRouter myMessageRouter;

  /**
   * Create a new message router.
   *
   * @param activityRunner
   *          activity runner that utilizes this router
   */
  public StandaloneMessageRouter(DevelopmentStandaloneLiveActivityRuntime activityRunner) {
    this.activityRunner = activityRunner;
  }

  @Override
  public void setDefaultRoutableInputMessageHandler(RouteMessageHandler messageHandler) {
    this.defaultMessageHandler = messageHandler;
  }

  @Override
  public void addRoutableInputMessageHandler(String channelId, RouteMessageHandler messageHandler) {
	messageHandlers.put(channelId, messageHandler);
  }

@Override
  public String getNodeName() {
    return myMessageRouter.getNodeName();
  }

  @Override
  public void sendMessage(String outputChannelId, Map<String, Object> message) {
    myMessageRouter.sendMessage(outputChannelId, message);
  }

  @Override
  public String getName() {
    return MessageRouterActivityComponent.COMPONENT_NAME;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void onPreStartupComponent() {
    timeProvider = getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
    activity = getComponentContext().getActivity();
    router = getStandaloneRouter();
    defaultMessageHandler = (RouteMessageHandler) activity;
  }

  @Override
  protected void onPostStartupComponent() {
    myMessageRouter = new MyMessageRouter();

    initializeCommunication();
    if (playbackRunner != null) {
      getComponentContext().getActivity().getSpaceEnvironment().getExecutorService()
          .submit(playbackRunner);
    }
  }

  /**
   * Get the router to use for this standalone instance.
   *
   * @return the router
   */
  private StandaloneRouter getStandaloneRouter() {
    Configuration configuration = activity.getConfiguration();
    boolean useLoopback = configuration.getPropertyBoolean("standalone.router.loopback", false);

    return useLoopback ? new LoopbackStandaloneRouter(configuration)
        : new MulticastStandaloneRouter(configuration);
  }

  @Override
  public void shutdownComponent() {
    try {
      router.shutdown();
      if (playbackRunner != null) {
        playbackRunner.stop();
      }
    } finally {
      activity = null;
      defaultMessageHandler = null;
      playbackRunner = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    return router != null && router.isRunning();
  }

  @Override
  public MessageRouter getMessageRouter() {
    return myMessageRouter;
  }

  /**
   * Initialize communication. Essentially opens the multicast socket and
   * prepare.
   */
  private void initializeCommunication() {
    try {
      router.startup();

      activity.getManagedTasks().submit(new Runnable() {
        @Override
        public void run() {
          receiveLoop();
        }
      });

      if (!messageWhiteList.isEmpty()) {
        final boolean autoFlush = true;
        File receiveTraceFile =
            new File(activity.getActivityFilesystem().getLogDirectory(), "messages.recv");
        receiveTraceWriter = new PrintWriter(new FileOutputStream(receiveTraceFile), autoFlush);
        File sendTraceFile =
            new File(activity.getActivityFilesystem().getLogDirectory(), "messages.send");
        sendTraceWriter = new PrintWriter(new FileOutputStream(sendTraceFile), autoFlush);
      }
    } catch (Exception e) {
      throw new SimpleSmartSpacesException("While creating standalone message route", e);
    }
  }

  /**
   * Trace a message.
   *
   * @param message
   *          message to trace
   * @param isSend
   *          {@code true} if this is a trace send, else receive
   */
  private void traceMessage(MessageMap message, boolean isSend) {
    MessageMap traceMessage = makeTraceMessage(message);
    if (traceMessage == null) {
      return;
    }

    traceMessage.remove(SOURCE_UUID_KEY);
    traceMessage.remove(SEGMENT_KEY);

    PrintWriter traceWriter = isSend ? sendTraceWriter : receiveTraceWriter;
    long now = getCurrentTimestamp();
    int timeIndex = isSend ? 0 : 1;
    if (lastMessageTime[timeIndex] < 0) {
      lastMessageTime[timeIndex] = now;
    }
    traceMessage.put(TIME_DELAY_KEY, (now - lastMessageTime[timeIndex]));
    lastMessageTime[timeIndex] = now;
    traceWriter.println(MAPPER.toString(traceMessage));
  }

  /**
   * Make a trace message.
   *
   * @param message
   *          message to trace
   *
   * @return message for tracing, or {@code null} for untraced message
   */
  MessageMap makeTraceMessage(MessageMap message) {
    return MessageUtils.makeTraceMessage(message, messageWhiteList);
  }

  /**
   * Receive loop for handling incoming messages.
   */
  private void receiveLoop() {
    while (true) {
      try {
        receiveMessage();
      } catch (Exception e) {
        if (messageCheckRunner != null && !messageCheckRunner.isActive()) {
          getLog().info("Receive loop exiting, verification complete");
          return;
        } else {
          throw e;
        }
      }
    }
  }

  /**
   * Receive and process a single message.
   */
  private void receiveMessage() {
    try {
      MessageMap messageObject = router.receive();
      if (messageObject == null) {
        return;
      }
      processMessage(messageObject, false);

      boolean isSelf = activity.getUuid().equals(messageObject.get(SOURCE_UUID_KEY));
      traceMessage(messageObject, isSelf);

    } catch (MessageWarning w) {
      getLog().warn(w.getMessage());
    } catch (Exception e) {
      throw new SimpleSmartSpacesException("Error handling receive message", e);
    }
  }

  /**
   * Process a message.
   *
   * @param messageObject
   *          message to process
   *
   * @param sendOnRoute
   *          {@code true} if the result should be sent out on a route
   */
  void processMessage(MessageMap messageObject, boolean sendOnRoute) {
    String route = (String) messageObject.get("route");
    String type = (String) messageObject.get("type");

    if (route == null) {
      throw new IllegalStateException("Missing route in message");
    }

    Object rawMessage = messageObject.get("message");
    @SuppressWarnings("unchecked")
    Map<String, Object> message = (Map<String, Object>) rawMessage;

    if (sendOnRoute) {
      String channelId = (String) messageObject.get("channel");
      sendMessage(channelId, message);
    } else {
      Collection<String> channelIds = inputRoutesToChannels.get(route);
      if (channelIds != null) {
        for (String channelId : channelIds) {
          defaultMessageHandler.onNewMessage(channelId, message);
        }
      }
    }
  }

  /**
   * Setup message playback.
   *
   * @param playbackPath
   *          path of file containing playback messages
   * @param onRoute
   *          {@code true} if messages should be sent out on a route
   */
  public void playback(String playbackPath, boolean onRoute) {
    if (playbackRunner != null) {
      throw new SmartSpacesException("Multiple playback runners activated");
    }
    playbackRunner = new PlaybackRunner(this, new File(playbackPath), onRoute);
  }

  /**
   * Start a message check runner.
   *
   * @param checkPath
   *          path to file indicating messages to check
   */
  public void checkStart(String checkPath) {
    messageCheckRunner = new MessageCheckRunner(this, checkPath);
  }

  /**
   * Signal that injection is completed (check final state).
   */
  public void injectionFinished() {
    if (messageCheckRunner == null) {
      getLog().info("All messages sent with no message checking.");
      activityRunner.signalCompletion(true);
    } else {
      messageCheckRunner.finalizeVerification();
    }
  }

  /**
   * @return logger to use
   */
  public Log getLog() {
    return activityRunner.getLog();
  }

  /**
   * @return activity runner associated with this router
   */
  public DevelopmentStandaloneLiveActivityRuntime getActivityRunner() {
    return activityRunner;
  }

  /**
   * @return current timestamp to use for message processing
   */
  long getCurrentTimestamp() {
    return timeProvider.getCurrentTime();
  }

  /**
   * Set trace filter for messages.
   *
   * @param traceFilter
   *          path to trace filter file to use
   */
  public void setTraceFilter(String traceFilter) {
    File traceFilterFile = new File(traceFilter);
    messageWhiteList = MessageUtils.readMessageList(traceFilterFile);
  }

  /**
   * Get the finish time delta from current time until a finish check should be
   * performed.
   *
   * @return delta in ms
   */
  public long getFinishDelta() {
    return finishTime - getCurrentTimestamp();
  }

  /**
   * Set the finish time delta from current time to next check for send
   * finished.
   *
   * @param finishDelay
   *          delta in ms
   */
  public void setFinishDelta(long finishDelay) {
    this.finishTime = finishDelay + getCurrentTimestamp();
  }

  @Override
  public void handleError(String message, Throwable t) {
    if (getComponentContext() != null) {
      super.handleError(message, t);
    }
    activityRunner.handleError(message, t);
  }

  @Override
  public synchronized RouteMessageSender registerOutputChannelTopic(String outputChannelId,
      Set<String> topicNames) {
    if (outputChannelsToRoutes.containsKey(outputChannelId)) {
      throw new SimpleSmartSpacesException("Output channel already registered: " + outputChannelId);
    }

    getComponentContext().getActivity().getLog()
        .warn(String.format("Registering output %s --> %s", outputChannelId, topicNames));
    outputChannelsToRoutes.put(outputChannelId, topicNames);

    return new StandaloneRouteMessagePublisher(outputChannelId);
  }

  @Override
  public synchronized void registerInputChannelTopic(String inputChannelId,
      Set<String> topicNames) {
    if (myMessageRouter.isInputChannelRegistered(inputChannelId)) {
      SimpleSmartSpacesException.throwFormattedException("Duplicate route entry for channel %s",
          inputChannelId);
    }
    for (String topicName : topicNames) {
      getComponentContext().getActivity().getLog()
          .warn(String.format("Registering input %s <-- %s", inputChannelId, topicNames));
      inputRoutesToChannels.put(topicName, inputChannelId);
    }
  }

  @Override
  protected InternalRouteMessagePublisher
      internalRegisterOutputRoute(RouteDescription routeDescription) {
    // TODO(keith): Fill in when needed
    return null;
  }

  @Override
  protected void internalRegisterInputRoute(RouteDescription routeDescription) {
    // TODO(keith): Fill in when needed
  }

  @Override
  public synchronized void clearAllChannelTopics() {
    getComponentContext().getActivity().getLog().warn("Clearing all channel topics");
    myMessageRouter.clearAllChannelTopics();
  }

  @Override
  public RouteMessageSender getMessagePublisher(String outputChannelId) {
    return new StandaloneRouteMessagePublisher(outputChannelId);
  }

  private class MyMessageRouter implements MessageRouter {

    @Override
    public void handleNewMessage(Object message, RouteMessageSubscriber subscriber) {
      // TODO Auto-generated method stub

    }

    @Override
    public void setDefaultRoutableInputMessageHandler(RouteMessageHandler messageHandler) {
      // TODO Auto-generated method stub
    }

    @Override
	public void addRoutableInputMessageHandler(String channelId, RouteMessageHandler messageHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRoutableInputMessageHandlers(Map<String, RouteMessageHandler> messageHandlers) {
		// TODO Auto-generated method stub
	}

	@Override
    public String getNodeName() {
      // TODO(keith): For now only ROS is used for node names as ROS is our
      // only
      // router. Eventually change
      // to a more generic route name config parameter.
      return getComponentContext().getActivity().getConfiguration()
          .getPropertyString(RosActivityComponent.CONFIGURATION_NAME_ACTIVITY_PUBSUB_NODE_NAME);
    }

    @Override
    public String getDefaultRouteProtocol() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void sendMessage(String outputChannelId, Map<String, Object> message) {
      sendMessage(outputChannelId, MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE,
          MAPPER.toString(message));
    }

    @Override
    public boolean isOutputChannelRegistered(String channelId) {
      return outputChannelsToRoutes.containsKey(channelId);
    }

    @Override
    public RouteMessageSender registerOutputChannelTopic(RouteDescription routeDescription)
        throws SmartSpacesException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isInputChannelRegistered(String channelId) {
      // TODO Auto-generated method stub
      return inputRoutesToChannels.values().contains(channelId);
    }

    @Override
    public void registerInputChannelTopic(RouteDescription routeDescription)
        throws SmartSpacesException {
      // TODO Auto-generated method stub

    }

    @Override
    public void clearAllChannelTopics() {
      inputRoutesToChannels.clear();
      outputChannelsToRoutes.clear();
    }

    @Override
    public Set<String> getOutputChannelIds() {
      return Sets.newHashSet(outputChannelsToRoutes.keySet());
    }

    @Override
    public Set<String> getInputChannelIds() {
      return Sets.newHashSet(inputRoutesToChannels.values());
    }

    @Override
    public RouteMessageSender getMessagePublisher(String outputChannelId) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Send an output message.
     *
     * @param channelId
     *          the channel ID on which to send the message
     * @param type
     *          type of message to send
     * @param message
     *          message to send
     */
    private void sendMessage(String channelId, String type, String message) {
      try {
        Set<String> routes = outputChannelsToRoutes.get(channelId);
        if (routes == null) {
          getLog().error("Attempt to send on unregistered output channel " + channelId);
          Set<String> unknown = Sets.newHashSet("unknown");
          outputChannelsToRoutes.put(channelId, unknown);
          routes = unknown;
        }

        for (String route : routes) {
          // This is horribly inefficient but preserves the right
          // semantics. It's
          // more
          // flexible to keep everything as JSON, instead as a string
          // embedded in
          // Json.
          Object baseMessage = (MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE.equals(type))
              ? MAPPER.parseObject(message) : message;

          MessageMap messageObject = new MessageMap();
          messageObject.put("message", baseMessage);
          messageObject.put("type", type);
          messageObject.put("route", route);
          messageObject.put("channel", channelId);
          messageObject.put(SOURCE_UUID_KEY, activity.getUuid());
          router.send(messageObject);

          if (messageCheckRunner != null) {
            if (messageCheckRunner.checkMessage(messageObject)) {
              messageCheckRunner.finalizeVerification();
            }
          }
        }
      } catch (Exception e) {
        throw new SimpleSmartSpacesException("While sending standalone message", e);
      }
    }
  }

  /**
   * A route message publisher for the standalone publisher.
   *
   * @author Keith M. Hughes
   */
  private class StandaloneRouteMessagePublisher implements RouteMessageSender {

    /**
     * The channel ID for the output channel.
     */
    private String channelId;

    /**
     * Construct a new publisher.
     *
     * @param channelId
     *          channel ID for the publisher
     */
    public StandaloneRouteMessagePublisher(String channelId) {
      this.channelId = channelId;
    }

    @Override
    public String getChannelId() {
      return channelId;
    }

    @Override
    public void sendMessage(Map<String, Object> message) {
      StandaloneMessageRouter.this.sendMessage(channelId, message);
    }
  }
}
