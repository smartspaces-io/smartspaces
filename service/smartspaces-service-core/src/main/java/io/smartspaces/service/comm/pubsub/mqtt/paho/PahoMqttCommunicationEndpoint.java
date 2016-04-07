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

package io.smartspaces.service.comm.pubsub.mqtt.paho;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint;
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;

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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * An MQTT communication endpoint implemented with Paho.
 * 
 * @author Keith M. Hughes
 */
public class PahoMqttCommunicationEndpoint implements MqttCommunicationEndpoint {

  /**
   * The description of the MQTT broker.
   */
  private MqttBrokerDescription mqttBrokerDescription;

  /*
   * The ID for the MQTT client.
   */
  private String mqttClientId;

  /**
   * The client persistence to use.
   */
  private MqttClientPersistence persistence;

  /**
   * The MQTT client.
   */
  private MqttAsyncClient mqttClient;

  /**
   * The collection of listeners for topic subscriptions.
   */
  private List<MqttSubscriberListener> subscriberListeners = new CopyOnWriteArrayList<>();

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * Construct a new endpoint.
   * 
   * @param mqttBrokerDescription
   *          the description of the MQTT broker
   * @param mqttClientId
   *          the ID for the MQTT client
   * @param log
   *          the log to use
   */
  PahoMqttCommunicationEndpoint(MqttBrokerDescription mqttBrokerDescription, String mqttClientId,
      Log log) {
    this.mqttBrokerDescription = mqttBrokerDescription;
    this.mqttClientId = mqttClientId;
    this.log = log;
  }

  @Override
  public void startup() {
    try {
      persistence = new MemoryPersistence();
      mqttClient =
          new MqttAsyncClient(mqttBrokerDescription.getBrokerAddress(), mqttClientId, persistence);

      mqttClient.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          log.error("Lost MQTT connection", cause);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          log.info("Got delivery token " + token.getResponse());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          handleMessageArrived(topic, message);
        }
      });

      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(true);

      log.info("Connecting to broker: " + mqttClient.getServerURI());
      final CountDownLatch connectHappened = new CountDownLatch(1);
      mqttClient.connect(options, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
          log.info("Connect Listener has success on token " + token);
          connectHappened.countDown();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable cause) {
          log.error("MQTT Connect Listener has failure on token " + token, cause);
          connectHappened.countDown();
        }
      });
    } catch (Throwable e) {
      throw new SmartSpacesException("Error when connecting to MQTT broker", e);
    }
  }

  @Override
  public void shutdown() {
    if (mqttClient != null && mqttClient.isConnected()) {
      try {
        mqttClient.disconnect();
      } catch (Throwable e) {
        log.error("Could not disconnect the MQTT client", e);
      }
      mqttClient = null;
    }
  }

  @Override
  public MqttBrokerDescription getMqttBrokerDescription() {
    return mqttBrokerDescription;
  }

  @Override
  public String getMqttClientId() {
    return mqttClientId;
  }

  @Override
  public MqttCommunicationEndpoint addSubscriberListener(MqttSubscriberListener listener) {
    subscriberListeners.add(listener);

    return this;
  }

  @Override
  public MqttCommunicationEndpoint subscribe(String topicName) {
    try {
      mqttClient.subscribe(topicName, 0);

      return this;
    } catch (MqttException e) {
      throw SmartSpacesException.newFormattedException(e, "Could not subscribe to MQTT topic %s",
          topicName);
    }
  }

  @Override
  public Log getLog() {
    return log;
  }

  /**
   * Handle a message that has come into the client.
   * 
   * @param topicName
   *          the topic the message came in on
   * @param message
   *          the message
   */
  private void handleMessageArrived(String topicName, MqttMessage message) {
    for (MqttSubscriberListener listener : subscriberListeners) {
      try {
        listener.handleMessage(this, topicName, message.getPayload());
      } catch (Throwable e) {
        log.error(String.format("Error while handling MQTT message on topic %s", topicName), e);
      }
    }
  }
}
