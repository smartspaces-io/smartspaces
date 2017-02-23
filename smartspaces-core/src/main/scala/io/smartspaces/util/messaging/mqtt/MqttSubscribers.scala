package io.smartspaces.util.messaging.mqtt

import java.util.Set

import org.eclipse.paho.client.mqttv3.MqttCallback

/**
 * A collection of MQTT subscribers that should all receive the same message.
 *
 * @author Keith M. Hughes
 */
trait MqttSubscribers {

  /**
   * Add a collection of subscriber topics to the collection.
   *
   * @param mqttBroker
   *          the MQTT broker for the topic names
   * @param topicNames
   *          the topic names to be subscribed to from the master
   * @param callback
   *          the callback for all message responses
   */
  def addSubscribers(mqttBroker: MqttBrokerDescription, topicNames: Set[String],
    callback: MqttCallback): Unit

  /**
   * Shut down all subscribers.
   */
  def shutdown(): Unit
}