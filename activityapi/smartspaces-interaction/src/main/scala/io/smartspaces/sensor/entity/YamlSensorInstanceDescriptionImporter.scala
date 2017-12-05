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

package io.smartspaces.sensor.entity

import java.util.Map

import scala.collection.JavaConverters._
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.DynamicObject.ArrayDynamicObjectEntry
import io.smartspaces.util.data.dynamic.DynamicObject.ObjectDynamicObjectEntry
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator

/**
 * A YAML-based sensor instance description importer.
 *
 * @author Keith M. Hughes
 */
class YamlSensorInstanceDescriptionImporter(sensorCommonRegistry: SensorCommonRegistry, configuration: Map[String, Object], log: ExtendedLog) extends SensorInstanceDescriptionImporter {

  /**
   * The ID to be given to entities.
   *
   * TODO(keith): This should be created in the registry.
   */
  private var id: Integer = 0

  override def importDescriptions(sensorRegistry: SensorInstanceRegistry): SensorInstanceDescriptionImporter = {
    val data: DynamicObject = new StandardDynamicObjectNavigator(configuration)

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

        sensorRegistry.registerSensedEntity(new SimplePersonSensedEntityDescription(getNextId(),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)))
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

      var sensorDetail: Option[SensorDetail] = None
      var sensorDetailId = itemData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_DETAIL)
      if (sensorDetailId != null) {
        sensorDetail = sensorCommonRegistry.getSensorDetailByExternalId(sensorDetailId)
        if (sensorDetail.isEmpty) {
          // TODO(keith): Some sort of error.
          break
        }
      }

      var sensorUpdateTimeLimit: Option[Long] = None
      val updateTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_UPDATE_TIME_LIMIT)
      if (updateTimeLimitValue != null) {
        sensorUpdateTimeLimit = Option(updateTimeLimitValue)
      } else {
        if (sensorDetail.isDefined) {
          sensorUpdateTimeLimit = sensorDetail.get.sensorUpdateTimeLimit
        } else {
          sensorUpdateTimeLimit = None
        }
      }

      var sensorHeartbeatUpdateTimeLimit: Option[Long] = None
      val updateHeartbeatTimeLimitValue = itemData.getLong(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_UPDATE_TIME_LIMIT)
      if (updateHeartbeatTimeLimitValue != null) {
        sensorHeartbeatUpdateTimeLimit = Option(updateHeartbeatTimeLimitValue)
      } else {
        if (sensorDetail.isDefined) {
          sensorHeartbeatUpdateTimeLimit = sensorDetail.get.sensorHeartbeatUpdateTimeLimit
        } else {
          sensorHeartbeatUpdateTimeLimit = None
        }
      }
      val entity = new SimpleSensorEntityDescription(getNextId(),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION), sensorDetail, sensorUpdateTimeLimit, sensorHeartbeatUpdateTimeLimit)

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

        sensorRegistry.registerMarker(new SimpleMarkerEntityDescription(getNextId(),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION),
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

      sensorRegistry.registerSensedEntity(new SimplePhysicalSpaceSensedEntityDescription(getNextId(),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        itemData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION),
        getOptionalString(itemData, SensorDescriptionConstants.SECTION_FIELD_PHYSICAL_SPACE_DETAILS_PHYSICAL_SPACE_TYPE), 
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
      
      val channelIds = if (sensorChannelIds == "*") {
          val sensor = sensorRegistry.getSensorByExternalId(sensorExternalId)
          if (sensor.isDefined) {
            sensor.get.sensorDetail.get.getAllSensorChannelDetails().map(_.channelId)
            
          } else {
            log.warn(s"Could not find sensor with ID ${sensorExternalId} in sensor association")
            return
          }
      } else {
          sensorChannelIds.split(':').toList
      }
      
      channelIds.foreach { sensorRegistry.associateSensorWithSensedEntity(sensorExternalId, _, sensedExternalId) }
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

  private def getOptionalString(itemData: DynamicObject, fieldName: String): Option[String] = {
    Option(itemData.getString(fieldName))
  }
}
