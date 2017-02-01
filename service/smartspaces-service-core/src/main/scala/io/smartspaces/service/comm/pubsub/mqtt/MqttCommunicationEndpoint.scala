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

package io.smartspaces.service.comm.pubsub.mqtt;

import io.smartspaces.resource.managed.ManagedResource
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

import org.apache.commons.logging.Log
import io.smartspaces.util.messaging.mqtt.MqttPublisherDescription
import io.smartspaces.util.messaging.mqtt.MqttSubscriberDescription

/**
 * An endpoint for MQTT communications.
 *
 * @author Keith M. Hughes
 */
object MqttCommunicationEndpoint {

  /**
   * The 0 quality of service.
   */
  val QOS_0 = 0

  /**
   * The 1 quality of service.
   */
  val QOS_1 = 1

  /**
   * The 2 quality of service.
   */
  val QOS_2 = 2

  /**
   * The separator between components of a topic name.
   */
  val TOPIC_SEPARATOR = "/"
}

/**
 * An endpoint for MQTT communications.
 *
 * @author Keith M. Hughes
 */
trait MqttCommunicationEndpoint extends ManagedResource {

  /**
   * Get the description of the MQTT broker.
   *
   * @return the description of the MQTT broker
   */
  def getMqttBrokerDescription(): MqttBrokerDescription

  /**
   * Get the MQTT client ID.
   *
   * @return the client ID
   */
  def getMqttClientId(): String

  /**
   * Add in a connection listener.
   *
   * @param listener
   * 				the listener to add
   *
   * @return the endpoint
   */
  def addConnectionListener(listener: MqttConnectionListener): MqttCommunicationEndpoint

  /**
   * Subscribe to the given topic.
   *
   * @param subscriberDescription
   *          the topic to subscribe to
   * @param qos
   * 					the QoS level of the topic
   * @param autoreconnect
   *          reconnect automatically if the connection is lost to the broker
   * @param listener
   * 					the listener for messages on this topic
   */
  def subscribe(subscriberDescription: MqttSubscriberDescription, listener: MqttSubscriberListener): MqttCommunicationEndpoint

  /**
   * Subscribe to the given topic.
   *
   * @param topicName
   *          the topic to subscribe to
   * @param qos
   * 					the QoS level of the topic
   * @param autoreconnect
   *          reconnect automatically if the connection is lost to the broker
   * @param listener
   * 					the listener for messages on this topic
   */
  def subscribe(topicName: String, qos: Int, autoreconnect: Boolean, listener: MqttSubscriberListener): MqttCommunicationEndpoint

  /**
   * Create an MQTT message publisher.
   *
   * @param publisherDescription
   * 	      the publisher description
   */
  def createMessagePublisher(publisherDescription: MqttPublisherDescription): MqttPublisher

  /**
   * Create an MQTT message publisher.
   *
   * @param mqttTopicName
   *       the name of the MQTT topic
   * @param qos
   * 				the default Quality of Service for the name
   * @param retain
   * 	      {@code true} if messages should be retained by default
   */
  def createMessagePublisher(mqttTopicName: String, qos: Int, retain: Boolean): MqttPublisher

  /**
   * Is the client connected?
   *
   * @return {@code true} if connected
   */
  def isConnected(): Boolean

  /**
   * Get the log for the endpoint.
   *
   * @return the log
   */
  def getLog(): Log
}
