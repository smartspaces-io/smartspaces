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

import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint;
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService;
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener;
import io.smartspaces.system.StandaloneSmartSpacesEnvironment;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription;

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * A service for getting MQTT communication endpoints implemented with Paho.
 * 
 * @author Keith M. Hughes
 */
public class PahoMqttCommunicationEndpointService extends BaseSupportedService
    implements MqttCommunicationEndpointService {

  public static void main(String[] args) throws Exception {
    StandaloneSmartSpacesEnvironment spaceEnvironment =
        StandaloneSmartSpacesEnvironment.newStandaloneSmartSpacesEnvironment();
    spaceEnvironment.getServiceRegistry().registerService(new PahoMqttCommunicationEndpointService());
    
    MqttCommunicationEndpointService service = spaceEnvironment.getServiceRegistry().getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME);
    MqttCommunicationEndpoint endpoint =
        service.newMqttCommunicationEndpoint(new MqttBrokerDescription("tcp://192.168.188.109:1883"),
            "/mqtt/publisher/async", spaceEnvironment.getLog());
    endpoint.startup();

    Thread.sleep(2000);

    endpoint.addSubscriberListener(new MqttSubscriberListener() {

      @Override
      public void handleMessage(MqttCommunicationEndpoint endpoint, String topicName,
          byte[] payload) {
        Map<String, Object> message = StandardJsonMapper.INSTANCE.parseObject(new String(payload));
        endpoint.getLog().info(String.format("Got message on topic %s: %s", topicName, message));
      }
    });
    endpoint.subscribe("/home/sensor");

  }

  @Override
  public String getName() {
    return MqttCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public MqttCommunicationEndpoint newMqttCommunicationEndpoint(
      MqttBrokerDescription mqttBrokerDescription, String mqttClientId, Log log) {
    return new PahoMqttCommunicationEndpoint(mqttBrokerDescription, mqttClientId, log);
  }
}
