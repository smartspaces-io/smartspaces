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

package io.smartspaces.sensor.domain

/**
 * A collection of constants for working with sensor descriptions.
 * 
 * @author Keith M. Hughes
 */
object SensorDescriptionConstants {
  
  /**
   * The field in all entity descriptions for the entity ID.
   */
  val ENTITY_DESCRIPTION_FIELD_EXTERNAL_ID = "externalId"

  /**
   * The field in all entity descriptions for the entity name.
   */
  val ENTITY_DESCRIPTION_FIELD_NAME = "name"

  /**
   * The field in all entity descriptions for the entity description.
   */
  val ENTITY_DESCRIPTION_FIELD_DESCRIPTION = "description"

  /**
   * The section header for the measurement types section of the file.
   */
  val SECTION_HEADER_MEASUREMENT_TYPES = "measurementTypes"

  /**
   * The measurement type section field for the processing type for the measurement.
   */
  val SECTION_FIELD_MEASUREMENT_TYPES_PROCESSING_TYPE = "processingType"

  /**
   * The measurement type section field for the value type for the measurement.
   */
  val SECTION_FIELD_MEASUREMENT_TYPES_VALUE_TYPE = "valueType"

  /**
   * The measurement type section field for the default unit for the
   * measurement.
   */
  val SECTION_FIELD_MEASUREMENT_TYPES_DEFAULT_UNIT = "defaultUnit"

  /**
   * The measurement type section field for the aliases for the measurement.
   */
  val SECTION_FIELD_MEASUREMENT_TYPES_ALIASES = "aliases"

  /**
   * The section header for the measurement units in the measurement type
   * entries.
   */
  val SECTION_HEADER_MEASUREMENT_TYPES_MEASUREMENT_UNITS =
    "measurementUnits"

  /**
   * The section header for the sensor types entries.
   */
  val SECTION_HEADER_SENSOR_TYPES = "sensorTypes"

  /**
   * The field for the category usage for a sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_CATEGORY_USAGE = "categoryUsage"

  /**
   * The field for the data source for a sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_DATA_SOURCE = "dataSource"

  /**
   * The sensor types section field for the acquisition mode of the sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_SENSOR_ACQUISITION_MODE = "acquisitionMode"

  /**
   * The sensor types section field for the manufacturer name of the sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_NAME = "sensorManufacturerName"

  /**
   * The sensor types section field for the manufacturer model of the sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_SENSOR_MANUFACTURER_MODEL = "sensorManufacturerModel"

  /**
   * The sensor types section field for the IDs of supported channels for the sensor type.
   */
  val SECTION_FIELD_SENSOR_TYPES_SUPPORTED_CHANNEL_IDS = "supportedChannelIds"

  /**
   * The sensor types section field for the channels for the sensor type.
   * 
   * These needn't be supported channels. All are enumerated whether supported or not
   */
  val SECTION_FIELD_SENSOR_TYPES_CHANNELS = "channels"

  /**
   * The section field for the measurement type of a sensor channel.
   */
  val SECTION_FIELD_SENSOR_TYPES_CHANNELS_TYPE = "type"

  /**
   * The section field for the measurement unit of a sensor channel.
   */
  val SECTION_FIELD_SENSOR_TYPES_CHANNELS_UNIT = "unit"

  /**
   * The section header for the physical space types entries.
   */
  val SECTION_HEADER_PHYSICAL_SPACE_TYPES = "physicalSpaceTypes"

  /**
   * The section header for the people section of the file.
   */
  val SECTION_HEADER_PEOPLE = "people"

  /**
   * The section header for the sensor section of the file.
   */
  val SECTION_HEADER_SENSORS = "sensors"

  /**
   * The section field for the the sensor type of a sensor.
   */
  val SECTION_FIELD_SENSORS_SENSOR_TYPE = "sensorType"

  /**
   * The section field for the source of a sensor. The sourse is where the data comes from, e.g.
   * an external provider.
   */
  val SECTION_FIELD_SENSORS_SENSOR_SOURCE = "sensorSource"

  /**
   * The section field for the update time limit for a sensor.
   */
  val SECTION_FIELD_SENSORS_SENSOR_UPDATE_TIME_LIMIT = "sensorUpdateTimeLimit"

  /**
   * The section field for the heartbeat time limit for a sensor.
   */
  val SECTION_FIELD_SENSORS_SENSOR_HEARTBEAT_UPDATE_TIME_LIMIT = "sensorHeartbeatUpdateTimeLimit"

  /**
   * The section field for whether a sensor is to be considered active or not.
   */
  val SECTION_FIELD_SENSORS_ACTIVE = "active"

  /**
   * The section field default value for whether a sensor is to be considered active or not.
   */
  val SECTION_FIELD_DEFAULT_VALUE_SENSORS_ACTIVE = true

  /**
   * The section header for the physical space section of the file.
   */
  val SECTION_HEADER_PHYSICAL_SPACES = "physicalSpaces"

  /**
   * The physical space details section field for the channels for the physical space type.
   */
  val SECTION_FIELD_PHYSICAL_SPACE_DETAILS_PHYSICAL_SPACE_TYPE = "physicalSpaceType"

  /**
   * The physical space details section field for the containing physical spaces.
   */
  val SECTION_FIELD_PHYSICAL_SPACE_DETAILS_CONTAINED_IN = "containedIn"

  /**
   * The physical space details section field for the directly connected physical spaces.
   */
  val SECTION_FIELD_PHYSICAL_SPACE_DETAILS_DIRECTLY_CONNECTED_TO = "directlyConnectedTo"

  /**
   * The section header for the marker section of the file.
   */
  val SECTION_HEADER_MARKERS = "markers"

  /**
   * The field in a marker entity description for the marker ID.
   */
  val ENTITY_DESCRIPTION_FIELD_MARKER_ID = "markerId"

  /**
   * The field in a marker entity description for the marker source.
   */
  val ENTITY_DESCRIPTION_FIELD_MARKER_SOURCE = "markerSource"

  /**
   * The field in a marker entity description for the marker type.
   */
  val ENTITY_DESCRIPTION_FIELD_MARKER_TYPE = "markerType"

  /**
   * The section header for the marker association of the file.
   */
  val SECTION_HEADER_MARKER_ASSOCIATIONS = "markerAssociations"

  /**
   * The field in a marker association for the marker ID.
   */
  val ENTITY_DESCRIPTION_FIELD_MARKER_ASSOCIATION_MARKER = "marker"

  /**
   * The field in a marker association for the marked item ID.
   */
  val ENTITY_DESCRIPTION_FIELD_MARKER_ASSOCIATION_MARKED = "marked"

  /**
   * The section header for the sensor association section of the file.
   */
  val SECTION_HEADER_SENSOR_ASSOCIATIONS = "sensorAssociations"

  /**
   * The field in a sensor association for the sensor ID.
   */
  val ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSOR = "sensor"

  /**
   * The field in a sensor association for the sensed item ID.
   */
  val ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSED = "sensed"

  /**
   * The field in a sensor association for the channel IDs.
   */
  val ENTITY_DESCRIPTION_FIELD_SENSOR_ASSOCIATION_SENSOR_CHANNEL_IDS = "sensorChannelIds"

  /**
   * The section header for the configuration section of the file.
   */
  val SECTION_HEADER_CONFIGURATIONS = "configurations"

}