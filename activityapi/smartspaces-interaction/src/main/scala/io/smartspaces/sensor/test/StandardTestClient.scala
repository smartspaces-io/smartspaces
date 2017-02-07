package io.smartspaces.sensor.test

import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService
import io.smartspaces.messaging.MessageWriter
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription
import com.google.common.base.Charsets
import io.smartspaces.sensor.messages.DynamicObjectByteArrayCodec
import io.smartspaces.service.comm.pubsub.mqtt.MqttPublisher
import io.smartspaces.resource.managed.IdempotentManagedResource

class StandardTestClient(private val spaceEnvironment: SmartSpacesEnvironment, private val mqttBrokerDescription: MqttBrokerDescription, private val mqttTopicName: String) extends IdempotentManagedResource {

  /**
   * The MQTT client endpoint.
   */
  private var mqttEndpoint: MqttCommunicationEndpoint = null
  
  private var mqttMessageWriter: MqttPublisher = null

  override def onStartup(): Unit = {
    val service: MqttCommunicationEndpointService = spaceEnvironment.getServiceRegistry()
      .getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME)
    mqttEndpoint = service.newMqttCommunicationEndpoint(mqttBrokerDescription, "testclient", spaceEnvironment.getLog)

    mqttEndpoint.startup()
    
    mqttMessageWriter = mqttEndpoint.createMessagePublisher(mqttTopicName, 0, false)
  }
  
  override def onShutdown(): Unit = {
    mqttEndpoint.shutdown()
  }

  def goToLivingRoom(): Unit = {
    publishMessage("""
{ "sensor": "/sensornode/ESP_8C8E1B", 
  "data": {   
    "marker": {     
      "type": "/sensor/marker",     
      "value": "nfc:04E17DDAC94880"}}}
""")
  }
 
  def goToKitchen(): Unit = {
    publishMessage("""
{ "sensor": "/sensornode/ESP_8C8E1C", 
  "data": {   
    "marker": {     
      "type": "/sensor/marker",     
      "value": "nfc:04E17DDAC94880"}}}
""")
  }
 
  def publishMessage(message: String): Unit = {
    spaceEnvironment.getLog.info(s"Publishing $message")
    mqttMessageWriter.writeMessage(message.getBytes(DynamicObjectByteArrayCodec.CHARSET_DEFAULT))
  }
}