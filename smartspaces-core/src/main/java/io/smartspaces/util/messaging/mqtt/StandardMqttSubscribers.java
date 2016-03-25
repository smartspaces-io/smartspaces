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

package io.smartspaces.util.messaging.mqtt;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * A collection of MQTT subscribers for a given set of topics.
 *
 * @author Keith M. Hughes
 */
public class StandardMqttSubscribers implements MqttSubscribers {

  /**
   * The clients for sending the messages.
   */
  private final List<MqttClient> clients = new CopyOnWriteArrayList<>();

  /**
   * Node name for this collection.
   */
  private final String nodeName;

  /**
   * Logger for this collection.
   */
  private final Log log;

  /**
   * Construct a new publishers collection.
   *
   * @param nodeName
   *          the node name for the collection
   * @param messageEncode
   *          the encoder for messages
   * @param log
   *          the logger to use
   */
  public StandardMqttSubscribers(String nodeName, Log log) {
    this.nodeName = nodeName;
    this.log = log;
  }

  @Override
  public synchronized void addSubscribers(MqttBrokerDescription mqttBroker, Set<String> topicNames,
      MqttCallback callback) {
    // TODO(keith): Make this settable and configurable
    MqttClientPersistence persistence = new MemoryPersistence();

    log.debug(String.format("Adding subscribers for topic names %s to MQTT master %s", topicNames,
        mqttBroker));

    for (String topicName : topicNames) {

      log.debug(String.format("Adding subscriber topic %s", topicName));
      MqttClient client = null;

      try {
        // TODO(keith): Map topics to the particular MQTT client so when
        // transmitting, we get the correct client for that topic.
        client = new MqttClient(mqttBroker.getBrokerAddress(), nodeName, persistence);
      } catch (MqttException e) {
        log.error(String.format("Failed adding subscriber topic %s", topicName), e);
        continue;
      }

      log.debug(String.format("Added subscriber topic %s", topicName));
      client.setCallback(callback);

      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(true);
      // options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

      log.info("Connecting to broker: " + client.getServerURI());

      try {
        client.connect(options);

        client.subscribe(topicName);

        clients.add(client);
      } catch (Throwable e) {
        log.error("MQTT connect failed", e);
      }
    }
  }

  @Override
  public synchronized void shutdown() {
    for (MqttClient client : clients) {
      try {
        client.disconnect();
      } catch (MqttException e) {
        log.error("MQTT could not disconnect from client", e);
      }
    }
  }
}
