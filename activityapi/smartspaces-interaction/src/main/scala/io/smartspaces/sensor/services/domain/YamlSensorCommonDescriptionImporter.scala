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

import java.util.Map
import scala.collection.JavaConverters._
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.MeasurementUnitDescription
import io.smartspaces.sensor.domain.SensorDescriptionConstants
import io.smartspaces.sensor.domain.SimpleMeasurementTypeDescription
import io.smartspaces.sensor.domain.SimpleMeasurementUnitDescription
import io.smartspaces.sensor.domain.SimplePhysicalSpaceTypeDescription
import io.smartspaces.sensor.domain.SimpleSensorChannelDetailDescription
import io.smartspaces.sensor.domain.SimpleSensorDetailDescription
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.DynamicObject.ArrayDynamicObjectEntry
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator

/**
 * A YAML-based sensor common description importer.
 *
 * @author Keith M. Hughes
 */
class YamlSensorCommonDescriptionImporter(configuration: Map[String, Object], log: ExtendedLog) extends SensorCommonDescriptionImporter {

  /**
   * The ID to be given to entities.
   *
   * TODO(keith): This should be created in the registry.
   */
  private var id: Integer = 0

  override def importDescriptions(sensorRegistry: SensorCommonRegistry): SensorCommonDescriptionImporter = {
    val data: DynamicObject = new StandardDynamicObjectNavigator(configuration)

    getMeasurementTypes(sensorRegistry, data)
    getSensorDetails(sensorRegistry, data)
    getPhysicalSpaceTypes(sensorRegistry, data)

    return this
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

      val valueType =
        measurementTypeData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_MEASUREMENT_TYPES_VALUE_TYPE)
      val measurementType = new SimpleMeasurementTypeDescription(
        getNextId(),
        measurementTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        measurementTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        measurementTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION),
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
              measurementUnitData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION))

          measurementType.addMeasurementUnit(measurementUnit)

          measurementUnitData.up()
        })

        val measurementUnit = measurementType.getMeasurementUnit(defaultUnitId)
        if (measurementUnit.isDefined) {
          measurementType.defaultUnit = measurementUnit.get
        } else {
          // Need an error message
        }
      }
      measurementTypeData.up()

      sensorRegistry.registerMeasurementType(measurementType)
    })
    data.up()
  }

  /**
   * Get all the sensor details data.
   *
   * @param sensorRegistry
   *          the sensor registry to store the data in
   * @param data
   *          the data read from the input stream
   */
  private def getSensorDetails(sensorRegistry: SensorCommonRegistry, data: DynamicObject): Unit = {
    data.down(SensorDescriptionConstants.SECTION_HEADER_SENSOR_DETAILS)

    data.getArrayEntries().asScala.foreach((sensorDetailEntry) => {
      val sensorDetailData = sensorDetailEntry.down()

      var sensorUpdateTimeLimit: Option[Long] = None
      val sensorUpdateTimeLimitValue: java.lang.Long = sensorDetailData.getLong(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_UPDATE_TIME_LIMIT)
      if (sensorUpdateTimeLimitValue != null) {
        sensorUpdateTimeLimit = Option(sensorUpdateTimeLimitValue)
      }

      var sensorHeartbeatUpdateTimeLimit: Option[Long] = None
      val sensorHeartbeatUpdateTimeLimitValue: java.lang.Long = sensorDetailData.getLong(SensorDescriptionConstants.SECTION_FIELD_SENSORS_SENSOR_HEARTBEAT_UPDATE_TIME_LIMIT)
      if (sensorHeartbeatUpdateTimeLimitValue != null) {
        sensorHeartbeatUpdateTimeLimit = Option(sensorHeartbeatUpdateTimeLimitValue)
      }

      val sensorUsageCategory = Option(sensorDetailData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_DETAILS_CATEGORY_USAGE))

      val sensorDetail = new SimpleSensorDetailDescription(
        getNextId(),
        sensorDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
        sensorDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
        sensorDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION),
        sensorUpdateTimeLimit, sensorHeartbeatUpdateTimeLimit, sensorUsageCategory)

      sensorDetailData.down(SensorDescriptionConstants.SECTION_FIELD_SENSOR_DETAILS_CHANNELS)
      data.getArrayEntries().asScala.foreach((channelDetailEntry: ArrayDynamicObjectEntry) => breakable {
        val channelDetailData = channelDetailEntry.down()

        val measurementTypeId =
          channelDetailData.getRequiredString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_DETAILS_CHANNELS_TYPE)
        val measurementType = sensorRegistry.getMeasurementTypeByExternalId(measurementTypeId)
        if (measurementType.isEmpty) {
          // TODO(keith): Some sort of error message
          break
        }

        var measurementUnit: MeasurementUnitDescription = null
        val measurementUnitId =
          channelDetailData.getString(SensorDescriptionConstants.SECTION_FIELD_SENSOR_DETAILS_CHANNELS_UNIT)
        if (measurementUnitId != null) {
          var measurementUnitOption = sensorRegistry.getMeasurementUnitByExternalId(measurementUnitId)
          if (measurementUnitOption.isEmpty) {
            // TODO(keith): Some sort of error message

            break
          } else {
            measurementUnit = measurementUnitOption.get
          }
        } else {
          // The default unit is used if none was specified
          measurementUnit = measurementType.get.defaultUnit
        }

        val channelDetail = new SimpleSensorChannelDetailDescription(
          sensorDetail,
          channelDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID),
          channelDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_NAME),
          channelDetailData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION),
          measurementType.get, measurementUnit)

        sensorDetail.addSensorChannelDetail(channelDetail)

        channelDetailData.up()
      })

      sensorRegistry.registerSensorDetail(sensorDetail)
    })
    data.up()
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
          physicalSpaceTypeData.getRequiredString(SensorDescriptionConstants.ENTITY_DESCRIPTION_FIELD_DESCRIPTION))

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
