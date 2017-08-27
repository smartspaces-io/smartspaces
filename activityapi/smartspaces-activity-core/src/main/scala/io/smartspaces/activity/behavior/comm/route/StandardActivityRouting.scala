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

import io.smartspaces.activity.behavior.comm.ros.StandardActivityRos
import io.smartspaces.activity.behavior.general.StandardActivityJson
import io.smartspaces.activity.component.comm.route.MessageRouterActivityComponent
import io.smartspaces.messaging.route.RouteMessageListener
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.util.data.json.StandardJsonMapper

import java.util.Map

/**
 * An activity behavior for Routing support.
 *
 * <p>
 * This behavior uses the registered {@link MessageRouterActivityComponent.COMPONENT_NAME} activity component.
 *
 * @author Keith M. Hughes
 */
trait StandardActivityRouting extends /* StandardActivityRos with */ StandardActivityJson with RoutingActivityBehavior {

  /**
   * The JSON mapper.
   */
  private val MAPPER: JsonMapper = StandardJsonMapper.INSTANCE

  /**
   * Router for input and output messages.
   */
  protected var router: MessageRouterActivityComponent = null

  abstract override def commonActivitySetup(): Unit = {
    super.commonActivitySetup();

    router = addActivityComponent(MessageRouterActivityComponent.COMPONENT_NAME)
    router.setRoutableInputMessageListener(new RouteMessageListener() {
      override def onNewRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
        handleNewRouteMessage(channelId, message);
      }
    });
  }

  /**
   * Handle a new input message.
   *
   * @param channelId
   *          the name of the channel
   * @param message
   *          the generic message
   */
  private def handleNewRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
    try {
      callOnNewRouteMessage(channelId, message)
    } catch {
      case e: Throwable => getLog().error("Could not process new route message", e)
    }
  }

  override def onNewRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
    // Default is to do nothing.
  }

  override def sendRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
    try {
      router.sendMessage(channelId, message)
    } catch {
      case e: Throwable => getLog().error(
        s"Could not write message on route output channel ${channelId}", e)
    }
  }

  override def sendRouteMessage(channelId: String, message: DynamicObjectBuilder): Unit = {
    sendRouteMessage(channelId, message.toMap())
  }

  /**
   * Call the {@link #onNewInputMessage(String, Map)} method.
   *
   * @param channelId
   *          the ID of the channel
   * @param message
   *          the message
   */
  private def callOnNewRouteMessage(channelId: String, message: Map[String, Object]): Unit = {
    getLog().debug(s"In catch all message handler for channel ID ${channelId}")
    
    val invocation = getExecutionContext().enterMethod()

    try {
      onNewRouteMessage(channelId, message)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  protected override def getRouterActivityComponent(): MessageRouterActivityComponent = {
    return router
  }
}