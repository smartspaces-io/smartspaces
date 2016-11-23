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

package io.smartspaces.container.control.message.activity.ros;

import io.smartspaces.container.control.message.activity.LiveActivityDeleteRequest;

import smartspaces_msgs.LiveActivityDeleteRequestMessage;

/**
 * A message translator between ROS messages and the internal messages for live
 * activity deletion.
 *
 * @author Keith M. Hughes
 */
public class RosLiveActivityDeleteMessageTranslator {

  /**
   * The value that the detail should have in the ROS message for live activity
   * deletion responses if there is no detail.
   */
  public static final String CONTAINER_LIVE_ACTIVITY_DELETE_RESPONSE_DETAIL_NONE = "";

  /**
   * Deserialize a ROS message for deleting a live activity to its internal
   * request.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the internal delete message
   */
  public static LiveActivityDeleteRequest deserializeLiveActivityDeleteRequest(
      LiveActivityDeleteRequestMessage rosMessage) {
    return new LiveActivityDeleteRequest(rosMessage.getUuid(), rosMessage.getIdentifyingName(),
        rosMessage.getVersion(),
        rosMessage.getForce() == LiveActivityDeleteRequestMessage.FORCE_TRUE);
  }

  /**
   * Serialize an internal live activity delete request into its ROS
   * counterpart.
   *
   * @param request
   *          the request to serialize
   * @param rosMessage
   *          the ROS message to serialize into
   */
  public static void serializeLiveActivityDeleteRequest(LiveActivityDeleteRequest request,
      LiveActivityDeleteRequestMessage rosMessage) {
    rosMessage.setUuid(request.getUuid());
    rosMessage.setIdentifyingName(request.getIdentifyingName());
    rosMessage.setVersion(request.getVersion());
    rosMessage.setForce(request.isForce() ? LiveActivityDeleteRequestMessage.FORCE_TRUE
        : LiveActivityDeleteRequestMessage.FORCE_FALSE);
  }
}
