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

package io.smartspaces.activity.behavior.routing

import java.util.Map
import io.smartspaces.activity.component.route.MessageRouterActivityComponent
import io.smartspaces.activity.behavior.ros.StandardActivityRos
import io.smartspaces.messaging.route.RoutableInputMessageListener
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.util.data.json.StandardJsonMapper
import io.smartspaces.activity.behavior.general.StandardActivityJson

/**
 * An activity behavior for Routing support.
 *
 * <p>
 * This behavior uses the registered {@link MessageRouterActivityComponent.COMPONENT_NAME} activity component.
 *
 * @author Keith M. Hughes
 */
trait StandardActivityRouting extends StandardActivityRos with StandardActivityJson with RoutingActivityBehavior {

  /**
   * The JSON mapper.
   */
  private val MAPPER: JsonMapper = StandardJsonMapper.INSTANCE

  /**
   * Router for input and output messages.
   */
  private var router: MessageRouterActivityComponent = null

  abstract override def commonActivitySetup(): Unit = {
    super.commonActivitySetup();

    router = addActivityComponent(MessageRouterActivityComponent.COMPONENT_NAME)
    router.setRoutableInputMessageListener(new RoutableInputMessageListener() {
      override def onNewRoutableInputMessage(channelName: String, message: Map[String, Object]): Unit = {
        handleRoutableInputMessage(channelName, message);
      }
    });
  }

  /**
   * Handle a new input message.
   *
   * @param channelName
   *          the name of the channel
   * @param message
   *          the generic message
   */
  private def handleRoutableInputMessage(channelName: String, message: Map[String, Object]): Unit = {
    try {
      callOnNewInputMessage(channelName, message)
    } catch {
      case e: Exception => getLog().error("Could not process input message", e)
    }
  }

  override def onNewInputMessage(channelName: String, message: Map[String, Object]): Unit = {
    // Default is to do nothing.
  }

  override def sendOutputMessage(channelName: String, message: Map[String, Object]): Unit = {
    try {
      router.writeOutputMessage(channelName, message)
    } catch {
      case e: Throwable => getLog().error(
        String.format("Could not write message on route output channel %s", channelName), e)
    }
  }

  override def sendOutputMessage(channelName: String, message: DynamicObjectBuilder): Unit = {
    sendOutputMessage(channelName, message.toMap())
  }

  /**
   * Call the {@link #onNewInputMessage(String, Map)} method.
   *
   * @param channelName
   *          the name of the channel
   * @param message
   *          the message
   */
  private def callOnNewInputMessage(channelName: String, message: Map[String, Object]): Unit = {
    val invocation = getExecutionContext().enterMethod()

    try {
      onNewInputMessage(channelName, message)
    } finally {
      getExecutionContext().exitMethod(invocation)
    }
  }

  protected override def getRouterActivityComponent(): MessageRouterActivityComponent = {
    return router
  }
}