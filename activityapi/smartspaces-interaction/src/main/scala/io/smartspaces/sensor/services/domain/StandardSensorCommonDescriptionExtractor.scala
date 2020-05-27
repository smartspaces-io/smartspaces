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

package io.smartspaces.sensor.services.domain

import java.lang.{Long => JLong}

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.DataSourceAcquisitionModeCategoricalValue
import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.MeasurementUnitDescription
import io.smartspaces.sensor.domain.SensorChannelDetailDescription
import io.smartspaces.sensor.domain.SensorDescriptionConstants
import io.smartspaces.sensor.domain.SimpleDataSourceTypeDescription
import io.smartspaces.sensor.domain.SimpleMarkerTypeDescription
import io.smartspaces.sensor.domain.SimpleMeasurementTypeDescription
import io.smartspaces.sensor.domain.SimpleMeasurementUnitDescription
import io.smartspaces.sensor.domain.SimplePhysicalSpaceTypeDescription
import io.smartspaces.sensor.domain.SimpleSensorChannelDetailDescription
import io.smartspaces.sensor.domain.SimpleSensorTypeDescription
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.DynamicObject.ArrayDynamicObjectEntry

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

/**
 * A YAML-based sensor common description importer.
 *
 * @author Keith M. Hughes
 */
class StandardSensorCommonDescriptionExtractor(
  log: ExtendedLog) extends SensorCommonDescriptionExtractor {

  /**
   * The ID to be given to entities.
   *
   * TODO(keith): This should be created in the registry.
   */
  private var id: Integer = 0

  override def extractDescriptions(data: DynamicObject, sensorRegistry: SensorCommonRegistry): SensorCommonDescriptionExtractor = {
    log.info("Extracting common sensor description")

    getDataSourceTypes(sensorRegistry, data)
    getMeasurementTypes(sensorRegistry, data)
    getSensorTypes(sensorRegistry, data)
    getMarkerTypes(sensorRegistry, data)
    getPhysicalSpaceTypes(sensorRegistry, data)

    return this
  }
  /**
   * Get all the data source type data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getDataSourceTypes(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_DATA_SOURCE_TYPES)
    data.getArrayEntries().asScala.foreach((dataSourceEntry: ArrayDynamicObjectEntry) => breakable {
      val dataSourceData = dataSourceEntry.down()

      val dataSourceExternalId = dataSourceData.getRequiredString(
        SensorDescriptionConstants.SECTION_FIELD_DATA_SOURCES_EXTERNAL_ID)
      val dataSourceOriginProviderId = dataSourceData.getRequiredString(
        SensorDescriptionConstants.SECTION_FIELD_DATA_SOURCES_ORIGIN_PROVIDER_ID)
      val dataSourceInterfaceProviderId = dataSourceData.getRequiredString(
        SensorDescriptionConstants.SECTION_FIELD_DATA_SOURCES_INTERFACE_PROVIDER_ID)
      val dataSourceAcquisitionMode = DataSourceAcquisitionModeCategoricalValue.fromLabel(
        dataSourceData.getRequiredString(
          SensorDescriptionConstants.SECTION_FIELD_DATA_SOURCES_ACQUISITION_MODE)).get

      sensorRegistry.registerDataSourceType(SimpleDataSourceTypeDescription(
        dataSourceExternalId, dataSourceOriginProviderId, dataSourceInterfaceProviderId, dataSourceAcquisitionMode))
    })
    data.up
  }

  /**
   * Get all the measurement type data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getMeasurementTypes(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_MEASUREMENT_TYPES)

    data.getArrayEntries().asScala.foreach((measurementTypeEntry: ArrayDynamicObjectEntry) => {
      val measurementTypeData: DynamicObject = measurementTypeEntry.down()

      val measurementTypeId = measurementTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID)

      val valueType =
        measurementTypeData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPES_VALUE_TYPE)
      val measurementType = new SimpleMeasurementTypeDescription(
        getNextId(),
        measurementTypeId,
        measurementTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        Option(measurementTypeData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
        measurementTypeData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPES_PROCESSING_TYPE),
        valueType,
        null)

      if (!MeasurementTypeDescription.VALUE_TYPE_ID.equals(valueType) &&
        !valueType.startsWith(MeasurementTypeDescription.VALUE_TYPE_PREFIX_CATEGORICAL_VARIABLE)) {
        val defaultUnitId =
          measurementTypeData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPES_DEFAULT_UNIT)

        measurementTypeData.down(SensorDescriptionConstants.SECTION_HEADER_MEASUREMENT_TYPES_MEASUREMENT_UNITS)
        data.getArrayEntries().asScala.foreach((measurementUnitEntry: ArrayDynamicObjectEntry) => {
          val measurementUnitData = measurementUnitEntry.down()

          val measurementUnit =
            new SimpleMeasurementUnitDescription(measurementType, getNextId(),
              measurementUnitData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
              measurementUnitData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
              Option(measurementUnitData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)))

          measurementType.addMeasurementUnit(measurementUnit)

          measurementUnitData.up()
        })

        val measurementUnit = measurementType.getMeasurementUnit(defaultUnitId)
        if (measurementUnit.isDefined) {
          measurementType.defaultUnit = measurementUnit
        } else {
          log.error(s"Did not find default measurement unit with ID ${defaultUnitId} for measurement type ${measurementTypeId}")
        }
      }
      measurementTypeData.up()

      sensorRegistry.registerMeasurementType(measurementType)
    })
    data.up()
  }

  /**
   * Get all the sensor type data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getSensorTypes(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_SENSOR_TYPES)

    data.getArrayEntries().asScala.foreach((sensorDetailEntry) => {
      val sensorTypeData = sensorDetailEntry.down()

      val sensorTypeId = sensorTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID)

      val sensorUpdateTimeLimit: Option[Long] = {
        val sensorUpdateTimeLimitValue: JLong = sensorTypeData.getLong(
            SensorDescriptionConstants.SECTION_FIELD_STATE_UPDATE_TIME_LIMIT)
        if (sensorUpdateTimeLimitValue != null) {
          Some(sensorUpdateTimeLimitValue)
        } else {
          None
        }
      }

      val sensorHeartbeatUpdateTimeLimit: Option[Long] = {
        val sensorHeartbeatUpdateTimeLimitValue: JLong = sensorTypeData.getLong(SensorDescriptionConstants.SECTION_FIELD_HEARTBEAT_UPDATE_TIME_LIMIT)
        if (sensorHeartbeatUpdateTimeLimitValue != null) {
          Some(sensorHeartbeatUpdateTimeLimitValue)
        } else {
          None
        }
      }

      val sensorUsageCategory = Option(sensorTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_CATEGORY_USAGE))

      sensorTypeData.down(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_DATA_SOURCES)
      val numberDataSources = sensorTypeData.getSize
      val dataSources = for (pos <- 0 until numberDataSources) yield {
        sensorTypeData.getString(pos)
      }
      sensorTypeData.up

      val supportedChannelIds = sensorTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_SUPPORTED_CHANNEL_IDS, "*")

      val allSensorChannelsBuffer = ArrayBuffer[SensorChannelDetailDescription]()
      sensorTypeData.down(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_CHANNELS)
      data.getArrayEntries().asScala.foreach((channelDetailEntry: ArrayDynamicObjectEntry) => breakable {
        val channelDetailData = channelDetailEntry.down()

        val channelId = channelDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID)

        val measurementTypeId =
          channelDetailData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPE)
        val measurementType = sensorRegistry.getMeasurementTypeByExternalId(measurementTypeId)
        if (measurementType.isEmpty) {
          // TODO(keith): Some sort of error message
          break
        }

        var measurementUnit: Option[MeasurementUnitDescription] = None
        val measurementUnitId =
          channelDetailData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_CHANNELS_UNIT)
        if (measurementUnitId != null) {
          var measurementUnitOption = sensorRegistry.getMeasurementUnitByExternalId(measurementUnitId)
          if (measurementUnitOption.isEmpty) {
            // TODO(keith): Some sort of error message
            log.warn(s"Unknown measurement unit ID ${measurementUnitId} for sensor type ${sensorTypeId} channel ID ${channelId}")

            break
          } else {
            measurementUnit = measurementUnitOption
          }
        } else {
          // The default unit is used if none was specified
          measurementUnit = measurementType.get.defaultUnit
        }

        val updateTimeLimit: Option[Long] = {
          val updateTimeLimitValue: JLong = channelDetailData.getLong(
              SensorDescriptionConstants.SECTION_FIELD_STATE_UPDATE_TIME_LIMIT)
          if (updateTimeLimitValue != null) {
            Some(updateTimeLimitValue)
          } else {
            None
          }
        }

        val heartbeatUpdateTimeLimit: Option[Long] = {
          val heartbeatUpdateTimeLimitValue: JLong = channelDetailData.getLong(SensorDescriptionConstants.SECTION_FIELD_HEARTBEAT_UPDATE_TIME_LIMIT)
          if (heartbeatUpdateTimeLimitValue != null) {
            Some(heartbeatUpdateTimeLimitValue)
          } else {
            None
          }
        }

        val channelDetail = new SimpleSensorChannelDetailDescription(
          channelId,
          channelDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          Option(channelDetailData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
          measurementType.get, measurementUnit, updateTimeLimit, heartbeatUpdateTimeLimit)

        allSensorChannelsBuffer += channelDetail

        channelDetailData.up()
      })
      sensorTypeData.up

      val allSensorChannels = allSensorChannelsBuffer.toList
      val allSupportedSensorChannelsIds =
        EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(allSensorChannels, supportedChannelIds)
      val allSupportedSensorChannels = allSensorChannels.filter(
        channel => allSupportedSensorChannelsIds.exists(_ == channel.channelId))

      val sensorDetail = new SimpleSensorTypeDescription(
        getNextId(),
        sensorTypeId,
        sensorTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        Option(sensorTypeData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
        sensorUpdateTimeLimit, sensorHeartbeatUpdateTimeLimit,
        sensorUsageCategory,
        dataSources.toList,
        Option(sensorTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_NAME)),
        Option(sensorTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_MODEL)),
        supportedChannelIds, allSensorChannels, allSupportedSensorChannels)

      sensorRegistry.registerSensorType(sensorDetail)
    })
    data.up()
  }

  /**
   * Get all the marker type data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getMarkerTypes(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_MARKER_TYPES)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_MARKER_TYPES)

      data.getArrayEntries().asScala.foreach((markerDetailEntry) => {
        val markerTypeData = markerDetailEntry.down()

        val markerTypeId = markerTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID)

        val markerUsageCategory = Option(markerTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_CATEGORY_USAGE))

        markerTypeData.down(SensorDescriptionConstants.SECTION_FIELD_MARKER_TYPES_DATA_SOURCES)
        val numberDataSources = markerTypeData.getSize
        val dataSources = for (pos <- 0 until numberDataSources) yield {
          markerTypeData.getString(pos)
        }
        markerTypeData.up

        val measurementTypeId =
          markerTypeData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPE)
        val measurementType = sensorRegistry.getMeasurementTypeByExternalId(measurementTypeId)
        if (measurementType.isEmpty) {
          // TODO(keith): Some sort of error message
          break
        }

        val markerDetail = SimpleMarkerTypeDescription(
          getNextId(),
          markerTypeId,
          markerTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          Option(markerTypeData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)),
          markerUsageCategory,
          dataSources.toList,
          Option(markerTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_NAME)),
          Option(markerTypeData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_MODEL)),
          measurementType.get
        )

        sensorRegistry.registerMarkerType(markerDetail)
      })
      data.up()
    }
  }

  /**
   * Get all the physical space type data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getPhysicalSpaceTypes(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    if (data.containsProperty(SensorDescriptionConstants.SECTION_HEADER_PHYSICAL_SPACE_TYPES)) {
      data.down(SensorDescriptionConstants.SECTION_HEADER_PHYSICAL_SPACE_TYPES)

      data.getArrayEntries().asScala.foreach((physicalSpaceTypelEntry) => {
        val physicalSpaceTypeData = physicalSpaceTypelEntry.down()

        val physicalSpaceType = new SimplePhysicalSpaceTypeDescription(
          getNextId(),
          physicalSpaceTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          physicalSpaceTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          Option(physicalSpaceTypeData.getString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION)))

        sensorRegistry.registerPhysicalSpaceType(physicalSpaceType)
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
