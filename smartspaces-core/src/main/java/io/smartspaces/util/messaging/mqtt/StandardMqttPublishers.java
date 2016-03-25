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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.smartspaces.messaging.codec.MessageEncoder;

/**
 * A collection of MQTT publishers for a given message topic.
 * 
 * @param <T>
 *          the type of messages
 *
 * @author Keith M. Hughes
 */
public class StandardMqttPublishers<T> implements MqttPublishers<T> {

  /**
   * The clients for sending the messages.
   */
  private final List<MqttClientInformation> clients = new CopyOnWriteArrayList<>();

  /**
   * Node name for this collection.
   */
  private final String nodeName;

  /**
   * The message encoder for messages.
   */
  private final MessageEncoder<T, byte[]> messageEncoder;

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
  public StandardMqttPublishers(String nodeName, MessageEncoder<T, byte[]> messageEncoder,
      Log log) {
    this.nodeName = nodeName;
    this.messageEncoder = messageEncoder;
    this.log = log;
  }

  @Override
  public synchronized void addPublishers(MqttBrokerDescription mqttBroker, Set<String> topicNames) {
    // TODO(keith): Make this settable and configurable
    MqttClientPersistence persistence = new MemoryPersistence();

    log.debug(String.format("Adding publishers for topic names %s to MQTT master %s", topicNames,
        mqttBroker));

    for (String topicName : topicNames) {

      log.debug(String.format("Adding publisher topic %s", topicName));
      MqttAsyncClient client = null;

      try {
        // TODO(keith): Create map of MQTT masters to MqttClientinformation
        // object and have set of topics inside the client info.
        client = new MqttAsyncClient(mqttBroker.getBrokerAddress(), nodeName, persistence);
      } catch (MqttException e) {
        log.error(String.format("Failed adding publisher topic %s", topicName), e);
        continue;
      }

      log.debug(String.format("Added publisher topic %s", topicName));
      client.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          log.error("Lost connection to MQTT server", cause);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          log.info("Got MQTT delivery token " + token.getResponse());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          // Not needed since not subscribing.
        }
      });

      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(true);

      log.info("Connecting to broker: " + client.getServerURI());
      final CountDownLatch connectHappened = new CountDownLatch(1);
      boolean connectStarted = true;
      try {
        client.connect(options, new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken token) {
            log.info("Connect Listener has success on token " + token);
            connectHappened.countDown();
          }

          @Override
          public void onFailure(IMqttToken token, Throwable cause) {
            log.error("Connect Listener has failure on token " + token, cause);
            connectHappened.countDown();
          }
        });
      } catch (Throwable e) {
        log.error("MQTT connect failed", e);
        connectStarted = false;
      }

      if (connectStarted) {
        try {
          if (connectHappened.await(10000, TimeUnit.MILLISECONDS)) {
            clients.add(new MqttClientInformation(client, topicName));
          }
        } catch (InterruptedException e) {
          log.error("MQTT connect failed from interrupted wait for connection", e);
        }
      }
    }
  }

  @Override
  public synchronized void publishMessage(T message) {
    MqttMessage mqttMessage = new MqttMessage(messageEncoder.encode(message));
    mqttMessage.setQos(1);

    IMqttActionListener actionListener = new IMqttActionListener() {
      @Override
      public void onSuccess(IMqttToken token) {
        log.info("MQTT message sent successfully");
      }

      @Override
      public void onFailure(IMqttToken token, Throwable throwable) {
        log.error("MQTT message failed");
      }
    };

    for (MqttClientInformation client : clients) {
      try {
        client.getMqttClient().publish(client.getTopicName(), mqttMessage, null, actionListener);
      } catch (Throwable e) {
        log.error("MQTT message publish failed", e);
      }
    }
  }

  @Override
  public synchronized void shutdown() {
    for (MqttClientInformation client : clients) {
      try {
        client.getMqttClient().disconnect();
      } catch (MqttException e) {
        log.error("MQTT could not disconnect from client", e);
      }
    }
  }

  /**
   * Information about an MQTT connection.
   * 
   * @author Keith M. Hughes
   */
  public static class MqttClientInformation {

    /**
     * The MQTT client.
     */
    private MqttAsyncClient client;

    /**
     * The topic name to be written to.
     */
    private String topicName;

    /**
     * Construct a new client info object.
     * 
     * @param client
     *          the MQTT client
     * @param topicName
     *          the topic name
     */
    public MqttClientInformation(MqttAsyncClient client, String topicName) {
      this.client = client;
      this.topicName = topicName;
    }

    /**
     * Get the client to be written to.
     * 
     * @return the client to be written to
     */
    public MqttAsyncClient getMqttClient() {
      return client;
    }

    /**
     * Get the topic name that will be written to on the client.
     * 
     * @return the topic name
     */
    public String getTopicName() {
      return topicName;
    }
  }
}
