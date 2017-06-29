/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.activity.behavior.comm.route

import io.smartspaces.activity.behavior.comm.ros.RosActivityBehavior
import io.smartspaces.activity.behavior.general.JsonActivityBehavior
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.activity.component.comm.route.MessageRouterActivityComponent
import java.util.Map

/**
 * Routing behavior for an activity.
 * 
 * @author Keith M. Hughes
 */
trait RoutingActivityBehavior extends JsonActivityBehavior /* with RosActivityBehvior */ {

  /**
   * A new message is coming in.
   *
   * @param channelName
   *          name of the input channel the message came in on
   * @param message
   *          the message that came in
   */
  def onNewIncomingRouteMessage(channelName: String, message: Map[String, Object]): Unit

  /**
   * Send an output message.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to sRouend
   */
  def sendRouteMessage(channelName: String, message: Map[String, Object]): Unit

  /**
   * Send an output message from a {@link DynamicObjectBuilder}.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  def sendRouteMessage(channelName: String ,  message: DynamicObjectBuilder): Unit

  /**
   * Get the router for the activity.
   * 
   * @return the router for the activity
   */
  protected def getRouterActivityComponent(): MessageRouterActivityComponent
}