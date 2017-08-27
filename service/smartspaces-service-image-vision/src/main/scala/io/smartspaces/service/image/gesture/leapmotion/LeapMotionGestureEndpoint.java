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

package io.smartspaces.service.image.gesture.leapmotion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MapStringMessageCodec;
import io.smartspaces.service.image.gesture.Gesture;
import io.smartspaces.service.image.gesture.Gesture.GestureState;
import io.smartspaces.service.image.gesture.GestureEndpoint;
import io.smartspaces.service.image.gesture.GestureHand;
import io.smartspaces.service.image.gesture.GestureHandListener;
import io.smartspaces.service.image.gesture.GestureListener;
import io.smartspaces.service.image.gesture.GesturePointable;
import io.smartspaces.service.image.gesture.GesturePointableListener;
import io.smartspaces.service.web.WebSocketMessageHandler;
import io.smartspaces.service.web.client.WebSocketClient;
import io.smartspaces.service.web.client.WebSocketClientService;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.data.dynamic.DynamicObject;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator;
import io.smartspaces.util.geometry.Vector3;

/**
 * A gesture connection using the Leap Motion.
 *
 * <p>
 * The Leap Motion uses a system process called leapd which listens to the
 * hardware. Clients connect to the leapd server over a web socket connection.
 * leapd uses a JSON-based protocol to communicate the gesture data to clients.
 *
 * <p>
 * This client supports version 4 of the leapd protocol.
 *
 * @author Keith M. Hughes
 */
public class LeapMotionGestureEndpoint implements GestureEndpoint {

  /**
   * The default websocket hostt for the leapd server.
   */
  public static final String LEAPD_WEBSOCKET_HOST_DEFAULT = "localhost";

  /**
   * The default websocket port for the leapd server.
   */
  public static final int LEAPD_WEBSOCKET_PORT_DEFAULT = 6437;

  /**
   * The gestures array from the leap motion.
   */
  public static final String LEAPMOTION_NAME_GESTURES = "gestures";

  /**
   * The ID of the gesture.
   */
  public static final String LEAPMOTION_NAME_GESTURE_ID = "id";

  /**
   * The center of a gesture from the leap motion. This is an array of 3 floats,
   * giving the location in millimeters.
   */
  public static final String LEAPMOTION_NAME_GESTURE_CENTER = "center";

  /**
   * The time duration of a gesture from the leap motion. In microseconds.
   */
  public static final String LEAPMOTION_NAME_GESTURE_DURATION = "duration";

  /**
   * An array giving hand IDs of a gesture from the leap motion.
   */
  public static final String LEAPMOTION_NAME_GESTURE_HANDIDS = "handIds";

  /**
   * An array giving pointable IDs of a gesture from the leap motion.
   */
  public static final String LEAPMOTION_NAME_GESTURE_POINTABLEIDS = "pointableIds";

  /**
   * The radius of a circle gesture from the leap motion in millimeters.
   */
  public static final String LEAPMOTION_NAME_GESTURE_RADIUS = "radius";

  /**
   * The state of a gesture from the leap motion, e.g. whether it has started,
   * stopped, or is continuing.
   */
  public static final String LEAPMOTION_NAME_GESTURE_STATE = "state";

  /**
   * The gesture state from the leap motion which says that a gesture has been
   * recognized and is starting.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_STATE_START = "start";

  /**
   * The gesture state from the leap motion which says the gesture has ended.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_STATE_STOP = "stop";

  /**
   * The gesture state from the leap motion which says the gesture is still in
   * motion.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_STATE_UPDATE = "update";

  /**
   * The type of a gesture from the leap motion.
   */
  public static final String LEAPMOTION_NAME_GESTURE_TYPE = "type";

  /**
   * The circle gesture type from the leap motion.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_TYPE_CIRCLE = "circle";

  /**
   * The swipe gesture type from the leap motion.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_TYPE_SWIPE = "swipe";

  /**
   * The key tap gesture type from the leap motion.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_TYPE_KEYTAP = "keyTap";

  /**
   * The screen tap gesture type from the leap motion.
   */
  public static final String LEAPMOTION_VALUE_GESTURE_TYPE_SCREENTAP = "screenTap";

  /**
   * The hands array from the leap motion.
   */
  public static final String LEAPMOTION_NAME_HANDS = "hands";

  /**
   * The ID of a hand in the hands array.
   */
  public static final String LEAPMOTION_NAME_HAND_ID = "id";

  /**
   * The direction of a hand.
   */
  public static final String LEAPMOTION_NAME_HAND_DIRECTION = "direction";

  /**
   * The position of the palm of a hand.
   */
  public static final String LEAPMOTION_NAME_HAND_PALM_POSITION = "palmPosition";

  /**
   * The normal of the palm of a hand.
   */
  public static final String LEAPMOTION_NAME_HAND_PALM_NORMAL = "palmNormal";

  /**
   * The velocity of the palm of a hand.
   */
  public static final String LEAPMOTION_NAME_HAND_PALM_VELOCITY = "palmVelocity";

  /**
   * The center of the sphere the hand can hold.
   */
  public static final String LEAPMOTION_NAME_HAND_SPHERE_CENTER = "sphereCenter";

  /**
   * The radius of the sphere the hand can hold.
   */
  public static final String LEAPMOTION_NAME_HAND_SPHERE_RADIUS = "sphereRadius";

  /**
   * The pointables array from the leap motion.
   */
  public static final String LEAPMOTION_NAME_POINTABLES = "pointables";

  /**
   * The ID of a pointable in the pointables array.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_ID = "id";

  /**
   * The ID of the hand containing the pointable in the pointables array.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_HANDID = "handId";

  /**
   * The direction of a pointable in the pointables array. An array giving the
   * unit vector of the position.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_DIRECTION = "direction";

  /**
   * The length of a pointable in the pointables array. In millimeters.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_LENGTH = "length";

  /**
   * The coordinates relative to the Leap Motion origin of the tip of a
   * pointable in the pointables array. In millimeters.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TIP_POSITION = "tipPosition";

  /**
   * The stabilized coordinates relative to the Leap Motion origin of the tip of
   * a pointable in the pointables array. In millimeters.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_STABILIZED_TIP_POSITION =
      "stabilizedTipPosition";

  /**
   * The velocity of the tip of a pointable in the pointables array. In
   * millimeters per second.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TIP_VELOCITY = "tipVelocity";

  /**
   * {@code true} if the pointable is thought to be a tool and not a finger.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TOOL = "tool";

  /**
   * The amount of time the pointable has been visible to the Leap Motion. In
   * seconds.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TIME_VISIBLE = "timeVisible";

  /**
   * How close the tip of the pointable is from the touch surface. It is a value
   * from -1 to 1 where values greater than 0 give a distance above the touch
   * surface and a value less than 0 give a distance through the surface. 0
   * means the tip is in the touch zone.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TOUCH_DISTANCE = "touchDistance";

  /**
   * What zone is the pointable in with respect to the touch zone.
   */
  public static final String LEAPMOTION_NAME_POINTABLE_TOUCH_ZONE = "touchZone";

  /**
   * The pointable is far from the touch zone.
   */
  public static final String LEAPMOTION_VALUE_POINTABLE_TOUCH_ZONE_NONE = "none";

  /**
   * The pointable is close to, but not through, the touch zone.
   */
  public static final String LEAPMOTION_VALUE_POINTABLE_TOUCH_ZONE_HOVERING = "hovering";

  /**
   * The pointable is touching or is through the touch zone.
   */
  public static final String LEAPMOTION_VALUEE_POINTABLE_TOUCH_ZONE_TOUCHING = "touching";

  /**
   * Enable getting all events, even if another client has focus.
   */
  public static final String LEAPMOTION_NAME_CONFIGURATION_ENABLE_BACKGROUND = "background";

  /**
   * Enable receiving gestures.
   */
  public static final String LEAPMOTION_NAME_CONFIGURATION_ENABLE_GESTURES = "enableGestures";

  /**
   * Map of Leap Motion gesture states to the service gesture states.
   */
  public static final Map<String, GestureState> LEAP_MOTION_TO_GESTURE_STATES;

  static {
    Map<String, GestureState> states = new HashMap<>();
    states.put(LEAPMOTION_VALUE_GESTURE_STATE_START, GestureState.START);
    states.put(LEAPMOTION_VALUE_GESTURE_STATE_UPDATE, GestureState.UPDATE);
    states.put(LEAPMOTION_VALUE_GESTURE_STATE_STOP, GestureState.STOP);

    LEAP_MOTION_TO_GESTURE_STATES = Collections.unmodifiableMap(states);
  }

  /**
   * The host the websocket server for leapd is running on.
   */
  private final String leapdWebsocketHost;

  /**
   * The port the websocket server for leapd is listening on.
   */
  private final int leapdWebsocketPort;

  /**
   * Websocket client for talking to the leapd server.
   */
  private WebSocketClient<Map<String, Object>> webSocketClient;

  /**
   * Listeners for pointable events.
   */
  private final List<GesturePointableListener> pointableListeners = Lists.newCopyOnWriteArrayList();

  /**
   * Listeners for hand events.
   */
  private final List<GestureHandListener> handListeners = Lists.newCopyOnWriteArrayList();

  /**
   * Listeners for gesture events.
   */
  private final List<GestureListener> gestureListeners = Lists.newCopyOnWriteArrayList();

  /**
   * The space environment to use.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * The client's logger.
   */
  private final ExtendedLog log;

  /**
   * Construct a new leapd connection client on the default leapd websocket host
   * {@link #LEAPD_WEBSOCKET_HOST_DEFAULT} and port
   * {@link #LEAPD_WEBSOCKET_PORT_DEFAULT}.
   *
   * @param spaceEnvironment
   *          the space environment to use
   * @param log
   *          the client logger to use
   */
  public LeapMotionGestureEndpoint(SmartSpacesEnvironment spaceEnvironment, ExtendedLog log) {
    this(LEAPD_WEBSOCKET_HOST_DEFAULT, LEAPD_WEBSOCKET_PORT_DEFAULT, spaceEnvironment, log);
  }

  /**
   * Construct a new leapd connection client.
   *
   * @param leapdWebsocketHost
   *          host the leapd server is speaking on
   * @param leapdWebsocketPort
   *          port the leapd server is speaking on
   * @param spaceEnvironment
   *          the space environment to use
   * @param log
   *          the client logger to use
   */
  public LeapMotionGestureEndpoint(String leapdWebsocketHost, int leapdWebsocketPort,
      SmartSpacesEnvironment spaceEnvironment, ExtendedLog log) {
    this.leapdWebsocketHost = leapdWebsocketHost;
    this.leapdWebsocketPort = leapdWebsocketPort;
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void startup() {
    WebSocketClientService service =
        spaceEnvironment.getServiceRegistry().getRequiredService(
            WebSocketClientService.SERVICE_NAME);

    webSocketClient =
        service.newWebSocketClient("ws://" + leapdWebsocketHost + ":" + leapdWebsocketPort
            + "/v4.json", new WebSocketMessageHandler<Map<String, Object>>() {
          @Override
          public void onNewMessage(Map<String, Object> message) {
            handleGestureData(message);
          }

          @Override
          public void onConnect() {
            handleLeapdConnect();
          }

          @Override
          public void onClose() {
            // TODO Auto-generated method stub

          }
        }, new MapStringMessageCodec(), log);
    webSocketClient.startup();
  }

  @Override
  public void shutdown() {
    if (webSocketClient != null) {
      webSocketClient.shutdown();
      webSocketClient = null;
    }
  }

  @Override
  public void addPointableListener(GesturePointableListener listener) {
    pointableListeners.add(listener);
  }

  @Override
  public void removePointableListener(GesturePointableListener listener) {
    pointableListeners.remove(listener);
  }

  @Override
  public void addHandListener(GestureHandListener listener) {
    handListeners.add(listener);
  }

  @Override
  public void removeHandListener(GestureHandListener listener) {
    handListeners.remove(listener);
  }

  @Override
  public void addGestureListener(GestureListener listener) {
    gestureListeners.add(listener);
  }

  @Override
  public void removeGestureListener(GestureListener listener) {
    gestureListeners.remove(listener);
  }

  /**
   * Handle the initial connection to leapd.
   */
  private void handleLeapdConnect() {
    Map<String, Object> message = new HashMap<>();
    message.put(LEAPMOTION_NAME_CONFIGURATION_ENABLE_GESTURES, true);
    message.put(LEAPMOTION_NAME_CONFIGURATION_ENABLE_BACKGROUND, true);
    webSocketClient.sendMessage(message);
  }

  /**
   * Data has come in from the Leap Motion. Handle it.
   *
   * @param data
   *          the data from the Leap Motion
   */
  private void handleGestureData(Map<String, Object> message) {
    if (message != null) {
      DynamicObject gmessage = new StandardDynamicObjectNavigator(message);
      processPointables(gmessage);
      processHands(gmessage);
      processGestures(gmessage);
    }
  }

  /**
   * Process any pointables in the data.
   *
   * @param gmessage
   *          the data from the Leap Motion
   */
  private void processPointables(DynamicObject gmessage) {
    if (!pointableListeners.isEmpty() && gmessage.containsProperty(LEAPMOTION_NAME_POINTABLES)) {
      gmessage.down(LEAPMOTION_NAME_POINTABLES);
      int numPointables = gmessage.getSize();
      if (numPointables > 0) {
        processPointables(gmessage, numPointables);
      }
      gmessage.up();
    }
  }

  /**
   * There are pointables, so extract their data and send it to the listeners.
   *
   * @param gmessage
   *          the Leap Motion data
   * @param numPointables
   *          the number of pointables
   */
  private void processPointables(DynamicObject gmessage, int numPointables) {
    Map<String, GesturePointable> pointables = getPointables(gmessage, numPointables);

    for (GesturePointableListener listener : pointableListeners) {
      try {
        listener.onGesturePointables(pointables);
      } catch (Exception e) {
        log.error("Error while handling pointable listener callback", e);
      }
    }
  }

  /**
   * Get the data for all pointables.
   *
   * @param gmessage
   *          data from the Leap Motion
   * @param numPointables
   *          the number of pointables
   *
   * @return a map from pointable IDs to pointables
   */
  private Map<String, GesturePointable> getPointables(DynamicObject gmessage, int numPointables) {
    Map<String, GesturePointable> pointables = new HashMap<>();

    for (int pos = 0; pos < numPointables; pos++) {
      gmessage.down(pos);

      gmessage.down(LEAPMOTION_NAME_POINTABLE_DIRECTION);
      Vector3 direction = new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_POINTABLE_TIP_POSITION);
      Vector3 tipPosition = new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_POINTABLE_TIP_VELOCITY);
      Vector3 tipVelocity = new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      GesturePointable pointable =
          new GesturePointable(gmessage.getInteger(LEAPMOTION_NAME_POINTABLE_ID).toString(),
              tipPosition, direction, tipVelocity,
              gmessage.getDouble(LEAPMOTION_NAME_POINTABLE_LENGTH),
              gmessage.getBoolean(LEAPMOTION_NAME_POINTABLE_TOOL));

      pointables.put(pointable.getId(), pointable);

      gmessage.up();
    }
    return pointables;
  }

  /**
   * Process any hands in the data.
   *
   * @param gmessage
   *          the data from the Leap Motion
   */
  private void processHands(DynamicObject gmessage) {
    if (!handListeners.isEmpty() && gmessage.containsProperty(LEAPMOTION_NAME_HANDS)) {
      gmessage.down(LEAPMOTION_NAME_HANDS);
      int numHands = gmessage.getSize();
      if (numHands > 0) {
        processHands(gmessage, numHands);
      }
      gmessage.up();
    }
  }

  /**
   * There are hands, so extract their data and send it to the listeners.
   *
   * @param gmessage
   *          the Leap Motion data
   * @param numHands
   *          the number of hands
   */
  private void processHands(DynamicObject gmessage, int numHands) {
    Map<String, GestureHand> hands = getHands(gmessage, numHands);

    for (GestureHandListener listener : handListeners) {
      try {
        listener.onGestureHands(hands);
      } catch (Exception e) {
        log.error("Error while handling hand listener callback", e);
      }
    }
  }

  /**
   * Get the data for all hands.
   *
   * @param gmessage
   *          data from the Leap Motion
   * @param numHands
   *          the number of hands
   *
   * @return a map from hand IDs to hands
   */
  private Map<String, GestureHand> getHands(DynamicObject gmessage, int numHands) {
    Map<String, GestureHand> hands = new HashMap<>();

    for (int pos = 0; pos < numHands; pos++) {
      gmessage.down(pos);

      gmessage.down(LEAPMOTION_NAME_HAND_DIRECTION);
      Vector3 direction = new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_HAND_PALM_POSITION);
      Vector3 palmPosition =
          new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_HAND_PALM_NORMAL);
      Vector3 palmNormal = new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_HAND_PALM_VELOCITY);
      Vector3 palmVelocity =
          new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      gmessage.down(LEAPMOTION_NAME_HAND_SPHERE_CENTER);
      Vector3 sphereCenter =
          new Vector3(gmessage.getDouble(0), gmessage.getDouble(1), gmessage.getDouble(2));
      gmessage.up();

      GestureHand hand =
          new GestureHand(gmessage.getInteger(LEAPMOTION_NAME_HAND_ID).toString(), palmPosition,
              palmVelocity, palmNormal, direction, sphereCenter,
              gmessage.getDouble(LEAPMOTION_NAME_HAND_SPHERE_RADIUS));

      hands.put(hand.getId(), hand);

      gmessage.up();
    }

    return hands;
  }

  /**
   * Process any gestures in the data.
   *
   * @param gmessage
   *          the data from the Leap Motion
   */
  private void processGestures(DynamicObject gmessage) {
    if (!gestureListeners.isEmpty() && gmessage.containsProperty(LEAPMOTION_NAME_GESTURES)) {
      gmessage.down(LEAPMOTION_NAME_GESTURES);
      int numGestures = gmessage.getSize();
      if (numGestures > 0) {
        processGestures(gmessage, numGestures);
      }
      gmessage.up();
    }
  }

  /**
   * There are gestures, so extract their data and send it to the listeners.
   *
   * @param gmessage
   *          the Leap Motion data
   * @param numGestures
   *          the number of gestures
   */
  private void processGestures(DynamicObject gmessage, int numGestures) {
    Map<String, Gesture> gestures = getGestures(gmessage, numGestures);

    for (GestureListener listener : gestureListeners) {
      try {
        listener.onGestures(gestures);
      } catch (Exception e) {
        log.error("Error while handling gesture listener callback", e);
      }
    }
  }

  /**
   * Get the data for all gestures.
   *
   * @param gmessage
   *          data from the Leap Motion
   * @param numGestures
   *          the number of gestures
   *
   * @return the map of gesture IDs to gestures
   */
  private Map<String, Gesture> getGestures(DynamicObject gmessage, int numGestures) {
    Map<String, Gesture> gestures = new HashMap<>();

    for (int pos = 0; pos < numGestures; pos++) {
      gmessage.down(pos);

      Gesture gesture =
          new Gesture(gmessage.getInteger(LEAPMOTION_NAME_GESTURE_ID).toString(),
              gmessage.getString(LEAPMOTION_NAME_GESTURE_TYPE),
              LEAP_MOTION_TO_GESTURE_STATES.get(gmessage.getString(LEAPMOTION_NAME_GESTURE_STATE)),
              gmessage.getDouble(LEAPMOTION_NAME_GESTURE_DURATION));

      gestures.put(gesture.getId(), gesture);

      gmessage.up();
    }

    return gestures;
  }
}
