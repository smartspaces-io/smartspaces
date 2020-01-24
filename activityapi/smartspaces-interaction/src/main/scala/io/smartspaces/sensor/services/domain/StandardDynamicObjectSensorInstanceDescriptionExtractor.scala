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

package io.smartspaces.sensor.services.domain

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.SensorDescriptionConstants
import io.smartspaces.sensor.domain.SensorTypeDescription
import io.smartspaces.sensor.domain.SimpleMarkerEntityDescription
import io.smartspaces.sensor.domain.SimplePersonSensedEntityDescription
import io.smartspaces.sensor.domain.SimplePhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.domain.SimpleSensorEntityDescription
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.DynamicObject.ArrayDynamicObjectEntry
import io.smartspaces.util.data.dynamic.DynamicObject.ObjectDynamicObjectEntry

/**
 * A dynamic object-based sensor instance description importer.
 *
 * @author Keith M. Hughes
 */
class StandardDynamicObjectSensorInstanceDescriptionExtractor(
  sensorCommonRegistry: SensorCommonRegistry,
  log: ExtendedLog) extends SensorInstanceDescriptionExtractor[DynamicObject] {

  /**
   * The ID to be given to entities.
   *
   * TODO(keith): This should be created in the registry.
   */
  private var id: Integer = 0

  override def extractDescriptions(
    data: DynamicObject,
    sensorRegistry: SensorInstanceRegistry): SensorInstanceDescriptionExtractor[DynamicObject] = {
    getSensors(sensorRegistry, data)
    getPeople(sensorRegistry, data)
    getMarkers(sensorRegistry, data)
    getPhysicalLocations(sensorRegistry, data)

    getSensorSensedEntityAssociations(sensorRegistry, data)
    getMarkerAssociations(sensorRegistry, data)
    getEntityConfigurations(sensorRegistry, data)

    return this
  }

  /**
   * Get all the people data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getPeople(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_PEOPLE)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_PEOPLE)

      data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        sensorRegistry.registerSensedEntity(new SimplePersonSensedEntityDescription(
          getNextId(),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          Option(itemData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION))))
      })
      data.up()
    }
  }

  /**
   * Get all the sensor data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getSensors(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_SENSORS)

    data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => breakable {
      val itemData = entry.down()

      var sensorType: Option[SensorTypeDescription] = None
      var sensorTypeId = itemData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_TYPE)
      if (sensorTypeId != null) {
        sensorType = sensorCommonRegistry.getSensorTypeByExternalId(sensorTypeId)
        if (sensorType.isEmpty) {
          // TODO(keith): Some sort of error.
          break
        }
      } else {
        // TODO(keith): Some sort of error.
        break
      }

      val sensorUpdateTimeLimit: Option[Long] = {
        val updateTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_STATE_UPDATE_TIME_LIMIT)
        if (updateTimeLimitValue != null) {
          Some(updateTimeLimitValue)
        } else {
          if (sensorType.isDefined) {
            sensorType.get.sensorUpdateTimeLimit
          } else {
            None
          }
        }
      }

      val sensorHeartbeatUpdateTimeLimit: Option[Long] = {
        val updateHeartbeatTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_HEARTBEAT_UPDATE_TIME_LIMIT)
        if (updateHeartbeatTimeLimitValue != null) {
          Some(updateHeartbeatTimeLimitValue)
        } else {
          if (sensorType.isDefined) {
            sensorType.get.sensorHeartbeatUpdateTimeLimit
          } else {
            None
          }
        }
      }

      val entity = new SimpleSensorEntityDescription(
        getNextId(),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        Option(itemData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
        sensorType.get,
        itemData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_SOURCE),
        sensorUpdateTimeLimit,
        sensorHeartbeatUpdateTimeLimit)

      entity.active = itemData.getBoolean(SensorDescriptionConstants.SECTION_FIELD_SENSORS_ACTIVE, SensorDescriptionConstants.SECTION_FIELD_DEFAULT_VALUE_SENSORS_ACTIVE)

      sensorRegistry.registerSensor(entity)
    })
    data.up()
  }

  /**
   * Get all the marker data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getMarkers(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_MARKERS)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_MARKERS)

      data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        sensorRegistry.registerMarker(new SimpleMarkerEntityDescription(
          getNextId(),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          Option(itemData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_MARKER_SOURCE),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_MARKER_TYPE),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_MARKER_ID)))
      })
      data.up()
    }
  }

  /**
   * Get all the physical location data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getPhysicalLocations(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_PHYSICAL_SPACES)

    data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
      val itemData = entry.down()

      val containedIn: Set[String] = if (itemData.containsProperty(SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_CONTAINED_IN)) {
        val elements: java.util.List[String] = itemData.down(SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_CONTAINED_IN).asList()
        itemData.up
        elements.asScala.toSet
      } else {
        Set()
      }

      val directlyConnectedTo: Set[String] = if (itemData.containsProperty(SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_DIRECTLY_CONNECTED_TO)) {
        val elements: java.util.List[String] = itemData.down(SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_DIRECTLY_CONNECTED_TO).asList()
        itemData.up
        elements.asScala.toSet
      } else {
        Set()
      }

      sensorRegistry.registerSensedEntity(new SimplePhysicalSpaceSensedEntityDescription(
        getNextId(),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        Option(itemData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
        Option(itemData.getString(SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_PHYSICAL_SPACE_TYPE)),
        containedIn, directlyConnectedTo))
    })

    data.up()
  }

  /**
   * Get all the sensor/sensed entity association data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getSensorSensedEntityAssociations(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_SENSOR_ASSOCIATIONS)

    data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
      val itemData = entry.down()

      val sensorExternalId = itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSOR)
      val sensedExternalId = itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSED)
      val sensorChannelIds = itemData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSOR_CHANNEL_IDS, "*")

      val sensor = sensorRegistry.getSensorByExternalId(sensorExternalId)
      if (sensor.isDefined) {

        val updateTimeLimit: Option[Long] = {
          val updateTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_STATE_UPDATE_TIME_LIMIT)
          if (updateTimeLimitValue != null) {
            Some(updateTimeLimitValue)
          } else {
            None
          }
        }

        val heartbeatUpdateTimeLimit: Option[Long] = {
          val heartbeatTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_HEARTBEAT_UPDATE_TIME_LIMIT)
          if (heartbeatTimeLimitValue != null) {
            Some(heartbeatTimeLimitValue)
          } else {
            None
          }
        }

        val channelIds = EntityDescriptionSupport.getSensorChannelIdsFromSensorDescription(sensor.get, sensorChannelIds)

        channelIds.foreach {
          sensorRegistry.associateSensorWithSensedEntity(
            sensorExternalId, _, sensedExternalId,
            updateTimeLimit, heartbeatUpdateTimeLimit)
        }
      } else {
        log.warn(s"Could not find sensor with ID ${sensorExternalId} in sensor association")
      }
    })
    data.up()
  }

  /**
   * Get all the marker/marked entity association data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getMarkerAssociations(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_MARKERS) && data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_MARKER_ASSOCIATIONS)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_MARKER_ASSOCIATIONS)

      data.getArrayEntries().asScala.foreach((entry: ArrayDynamicObjectEntry) => {
        val itemData = entry.down()

        sensorRegistry.associateMarkerWithMarkedEntity(
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_MARKER_ASSOCIATION_MARKER),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_MARKER_ASSOCIATION_MARKED))
      })

      data.up()
    }
  }

  /**
   * Get all the entity configuration data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  def getEntityConfigurations(sensorRegistry: SensorInstanceRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_CONFIGURATIONS)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_CONFIGURATIONS)

      data.getObjectEntries().asScala.foreach((entry: ObjectDynamicObjectEntry) => {
        val entityId = entry.getProperty()

        val configurationData = entry.getValue().down(entityId)
        if (configurationData.isObject) {
          sensorRegistry.addConfigurationData(entityId, configurationData.asMap().asScala.toMap)
        }
      })

      data.up()
    }
  }

  /**
   * Get the next "database" ID.
   *
   * @return the next ID.
   */
  private def getNextId(): String = {
    id = id + 1
    return Integer.toString(id)
  }
}
