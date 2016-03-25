package io.smartspaces.util.messaging.mqtt;

import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttCallback;

/**
 * A collection of MQTT publishers for a given message topic.
 *
 * @author Keith M. Hughes
 */
public interface MqttSubscribers {

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
  void addSubscribers(MqttBrokerDescription mqttBroker, Set<String> topicNames,
      MqttCallback callback);

  /**
   * Shut down all subscribers.
   */
  void shutdown();
}