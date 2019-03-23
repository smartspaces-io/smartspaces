/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.sensor.messaging.output

import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.sensor.messaging.messages.SensorMessages

import java.util.Map
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder

/**
 * A message builder for sensors.
 *
 * @author Keith M. Hughes
 */
trait SensorMessageBuilder {

  /**
   * The message builder.
   */
  val messageBuilder: DynamicObjectBuilder

  /**
   * Add in a timestamp for all channels in the message.
   *
   * @param timestamp
   *       the timestamp for the data
   *
   * @return this builder
   */
  def addTimestamp(timestamp: Long): SensorMessageBuilder

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any): SensorMessageBuilder

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param addition
   *       the additonal value
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, addition: Any): SensorMessageBuilder

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param timestamp
   *       timestamp for the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, timestamp: Long): SensorMessageBuilder

  /**
   * Add in data for a channel.
   *
   * @param channelId
   *       the ID of the channel
   * @param channelType
   *       the type of the channel data
   * @param value
   *       the value of the channel data
   * @param addition
   *       the additonal value
   * @param timestamp
   *       timestamp for the channel data
   *
   * @return this builder
   */
  def addChannelData(channelId: String, channelType: String, value: Any, addition: Any, timestamp: Long): SensorMessageBuilder

  /**
   * Turn the message into a map.
   *
   * @return the map for the massage
   */
  def toMap(): Map[String, Object]
}

/**
 * The standard sensor message builder.
 *
 * @author Keith M. Hughes
 */
object StandardSensorMessageBuilder {

  /**
   * Create a new measurement message.
   *
   * @param sensorId
   *            the sensor external ID
   *
   * @return the message builder
   */
  def newMeasurementMessage(sensorId: String): SensorMessageBuilder = {
    newMeasurementMessage(sensorId, None, None)
  }

  /**
   * Create a new measurement message.
   *
   * @param sensorId
   *            the sensor external ID
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *
   * @return the message builder
   */
  def newMeasurementMessage(
    sensorId: String,
    sender: Option[String],
    destination: Option[String]): SensorMessageBuilder = {
    new StandardSensorMessageBuilder(newDynamicObjectBuilder(
      sensorId,
      SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT,
      sender, destination))
  }

  /**
   * Create a new heartbeat message.
   *
   * @param sensorId
   *        the sensor external ID
   *
   * @return the message builder
   */
  def newHeartbeatMessage(sensorId: String): SensorMessageBuilder = {
    newHeartbeatMessage(sensorId, None, None)
  }

  /**
   * Create a new heartbeat message.
   *
   * @param sensorId
   *        the sensor external ID
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *
   * @return the message builder
   */
  def newHeartbeatMessage(
    sensorId: String,
    sender: Option[String],
    destination: Option[String]): SensorMessageBuilder = {
    new StandardSensorMessageBuilder(newDynamicObjectBuilder(
      sensorId,
      SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_HEARTBEAT,
      sender, destination))
  }

  /**
   * Add in a new sensor message to the composite builder.
   *
   * @param sensorId
   *             the sensor external ID
   * @param messageType
   *             the type of message
   */
  def addSensorMessage(sensorId: String, messageType: String): SensorMessageBuilder = {
    addSensorMessage(sensorId, messageType, None, None)
  }

  /**
   * Add in a new sensor message to the composite builder.
   *
   * @param sensorId
   *        the sensor external ID
   * @param messageType
   *        the type of message
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *        
   * @return the builder
   */
  def addSensorMessage(
    sensorId: String,
    messageType: String,
    sender: Option[String],
    destination: Option[String]): SensorMessageBuilder = {
    new StandardSensorMessageBuilder(newDynamicObjectBuilder(
        sensorId, messageType, sender, destination))
  }

  /**
   * Create a new dynamic object builder for a sensor message.
   *
   * @param sensorId
   *        the sensor external ID
   * @param messageType
   *        the type of message
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *        
   * @return the builder
   */
  private def newDynamicObjectBuilder(
    sensorId: String,
    messageType: String,
    sender: Option[String],
    destination: Option[String]): DynamicObjectBuilder = {
    val messageBuilder = new StandardDynamicObjectBuilder
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR, sensorId)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE, messageType)

    sender.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_SENDER,
        _)
    }

    destination.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_DESTINATION,
        _)
    }

    messageBuilder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    messageBuilder
  }
}

/**
 * The standard sensor message builder.
 *
 * @author Keith M. Hughes
 */
private class StandardSensorMessageBuilder(override val messageBuilder: DynamicObjectBuilder) extends SensorMessageBuilder {

  override def addTimestamp(timestamp: Long): SensorMessageBuilder = {
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)

    this
  }

  override def addChannelData(channelId: String, channelType: String, value: Any): SensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)

    messageBuilder.up

    this
  }

  override def addChannelData(channelId: String, channelType: String, value: Any, addition: Any): SensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION, addition)

    messageBuilder.up

    this
  }

  override def addChannelData(channelId: String, channelType: String, value: Any, timestamp: Long): SensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)

    messageBuilder.up

    this
  }

  override def addChannelData(channelId: String, channelType: String, value: Any, addition: Any, timestamp: Long): SensorMessageBuilder = {
    messageBuilder.newObject(channelId)

    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, channelType)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, value)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_ADDITION, addition)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TIMESTAMP, timestamp)

    messageBuilder.up

    this
  }

  override def toMap(): Map[String, Object] = {
    messageBuilder.toMap()
  }
}

/**
 * A message builder for building composite sensor messages.
 *
 * @author Keith M. Hughes
 */
trait CompositeSensorMessageBuilder extends SensorMessageBuilder {

  /**
   * Create a new measurement message.
   *
   * @param sensorId
   *            the sensor external ID
   *
   * @return the message builder
   */
  def newMeasurementMessage(sensorId: String): CompositeSensorMessageBuilder

  /**
   * Create a new measurement message.
   *
   * @param sensorId
   *            the sensor external ID
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *
   * @return the message builder
   */
  def newMeasurementMessage(sensorId: String, sender: Option[String], destination: Option[String]): CompositeSensorMessageBuilder

  /**
   * Create a new heartbeat message.
   *
   * @param sensorId
   *            the sensor external ID
   *
   * @return the message builder
   */
  def newHeartbeatMessage(sensorId: String): CompositeSensorMessageBuilder

  /**
   * Create a new heartbeat message.
   *
   * @param sensorId
   *            the sensor external ID
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   *
   * @return the message builder
   */
  def newHeartbeatMessage(sensorId: String, sender: Option[String], destination: Option[String]): CompositeSensorMessageBuilder

  /**
   * Add in a new sensor message to the composite builder.
   *
   * @param sensorId
   *             the sensor external ID
   * @param messageType
   *             the type of message
   */
  def addSensorMessage(sensorId: String, messageType: String): CompositeSensorMessageBuilder

  /**
   * Add in a new sensor message to the composite builder.
   *
   * @param sensorId
   *             the sensor external ID
   * @param messageType
   *             the type of message
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   */
  def addSensorMessage(sensorId: String, messageType: String, sender: Option[String], destination: Option[String]): CompositeSensorMessageBuilder
}

/**
 * A factory for composite message message builders.
 *
 * @author Keith M. Hughes
 */
object StandardCompositeSensorMessageBuilder {

  /**
   * Create a new compoite image.
   *
   * The builder will be in place for the first message.
   */
  def newCompositeMessage(): CompositeSensorMessageBuilder = {
    newCompositeMessage(None, None)
  }

  /**
   * Create a new compoite image.
   *
   * The builder will be in place for the first message.
   *
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   */
  def newCompositeMessage(sender: Option[String], destination: Option[String]): CompositeSensorMessageBuilder = {
    val messageBuilder = new StandardDynamicObjectBuilder
    messageBuilder.setProperty(
      SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE,
      SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_COMPOSITE)

    sender.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_SENDER,
        _)
    }

    destination.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_DESTINATION,
        _)
    }

    messageBuilder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    messageBuilder.newArray(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_MESSAGES)

    messageBuilder.pushMark

    new StandardCompositeSensorMessageBuilder(sender, destination, messageBuilder)
  }
}

/**
 * The standard implementation of composite sensor message builders.
 *
 * @author Keith M. Hughes
 */
private class StandardCompositeSensorMessageBuilder(
  defaultSender: Option[String], defaultDestination: Option[String],
  messageBuilder: DynamicObjectBuilder) extends StandardSensorMessageBuilder(messageBuilder) with CompositeSensorMessageBuilder {

  override def newMeasurementMessage(sensorId: String): CompositeSensorMessageBuilder = {
    newMeasurementMessage(sensorId, defaultSender, defaultDestination)
  }

  override def newMeasurementMessage(
    sensorId: String,
    sender: Option[String],
    destination: Option[String]): CompositeSensorMessageBuilder = {
    newSensorMessage(
      sensorId, SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_MEASUREMENT,
      sender, destination)

    this
  }

  override def newHeartbeatMessage(sensorId: String): CompositeSensorMessageBuilder = {
    newHeartbeatMessage(sensorId, defaultSender, defaultDestination)
  }

  override def newHeartbeatMessage(
    sensorId: String,
    sender: Option[String],
    destination: Option[String]): CompositeSensorMessageBuilder = {
    newSensorMessage(
      sensorId, SensorMessages.SENSOR_MESSAGE_FIELD_VALUE_MESSAGE_TYPE_HEARTBEAT,
      sender, destination)

    this
  }

  override def addSensorMessage(sensorId: String, messageType: String): CompositeSensorMessageBuilder = {
    addSensorMessage(sensorId, messageType, defaultSender, defaultDestination)
  }

  override def addSensorMessage(
    sensorId: String,
    messageType: String,
    sender: Option[String],
    destination: Option[String]): CompositeSensorMessageBuilder = {
    newSensorMessage(sensorId, messageType, sender, destination)

    this
  }

  /**
   * Add in a new sensor message to the composite message.
   *
   * @param sensorId
   *              the external ID of the sensor
   * @param messageType
   *              the type of this message
   * @param sender
   *        an optional sender
   * @param destination
   *        an optional destination
   */
  private def newSensorMessage(
    sensorId: String,
    messageType: String,
    sender: Option[String],
    destination: Option[String]): Unit = {
    messageBuilder.resetToMark(false)

    messageBuilder.newObject()
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR, sensorId)
    messageBuilder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_TYPE, messageType)

    sender.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_SENDER,
        _)
    }

    destination.foreach {
      messageBuilder.setProperty(
        SensorMessages.SENSOR_MESSAGE_FIELD_NAME_MESSAGE_DESTINATION,
        _)
    }

    messageBuilder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)
  }
}
