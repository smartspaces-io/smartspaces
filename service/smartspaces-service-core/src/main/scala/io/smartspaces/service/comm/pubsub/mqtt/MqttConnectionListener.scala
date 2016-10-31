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

package io.smartspaces.service.comm.pubsub.mqtt

/**
 * A listener for MQTT connection events.
 *
 * @author Keith M. Hughes
 */
trait MqttConnectionListener {

  /**
   * A connection was successful.
   *
   * @param endpoint
   *       the endpoint that had a successful connection
   * @param reconnect
   *       {@code true} if this was a reconnection
   */
  def onMqttConnectionSuccessful(endpoint: MqttCommunicationEndpoint, reconnect: Boolean): Unit

  /**
   * A connection was not successful.
   *
   * @param endpoint
   *       the endpoint that failed
   */
  def onMqttConnectionFailure(endpoint: MqttCommunicationEndpoint): Unit
  
  /**
   * The connection to the broker was lost.
   *
   * @param endpoint
   *       the endpoint that had a successful connection
   */
  def onMqttConnectionLost(endpoint: MqttCommunicationEndpoint): Unit
}


/**
 * A listener for MQTT connection events that supplies default methods.
 *
 * @author Keith M. Hughes
 */
trait MqttConnectionListenerSupport extends MqttConnectionListener {

  override def onMqttConnectionSuccessful(endpoint: MqttCommunicationEndpoint, reconnect: Boolean): Unit = { }

  override def onMqttConnectionFailure(endpoint: MqttCommunicationEndpoint): Unit = { }
  
  override def onMqttConnectionLost(endpoint: MqttCommunicationEndpoint): Unit = { }
}