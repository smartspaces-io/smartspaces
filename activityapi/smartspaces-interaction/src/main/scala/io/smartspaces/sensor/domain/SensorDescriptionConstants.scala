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
   * The section header for the data source provider origin types section of the file.
   */
  val SECTION_HEADER_DATA_SOURCE_PROVIDER_ORIGIN_TYPES = "dataSourceProviderOriginTypes"

  /**
   * The data source origin provider section field for the external ID of a data source origin provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_ORIGIN_TYPES_EXTERNAL_ID= "externalId"

  /**
   * The data source origin provider section field for the name of a data source origin provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_ORIGIN_TYPES_NAME= "name"

  /**
   * The data source origin provider section field for the description of a data source origin provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_ORIGIN_TYPES_DESCRIPTION= "description"

  /**
   * The section header for the data source provider interface types section of the file.
   */
  val SECTION_HEADER_DATA_SOURCE_PROVIDER_INTERFACE_TYPES = "dataSourceProviderInterfaceTypes"

  /**
   * The data source interface provider section field for the external ID of a data source interface provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_INTERFACE_TYPES_EXTERNAL_ID= "externalId"

  /**
   * The data source interface provider section field for the name of a data source interface provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_INTERFACE_TYPES_NAME= "name"

  /**
   * The data source interface provider section field for the description of a data source interface provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_INTERFACE_TYPES_DESCRIPTION= "description"

  /**
   * The section header for the data source provider types section of the file.
   */
  val SECTION_HEADER_DATA_SOURCE_PROVIDER_TYPES = "dataSourceProviderTypes"

  /**
   * The data sources provider section field for the external ID of a data source provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_TYPES_EXTERNAL_ID = "externalId"

  /**
   * The data sources section field for the origin provider ID of a data source provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_TYPES_ORIGIN_PROVIDER_ID = "originProviderId"

  /**
   * The data sources section field for the interface provider ID of a data source provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_TYPES_INTERFACE_PROVIDER_ID = "interfaceProviderId"

  /**
   * The data sources section field for the acquisition mode of a data source provider.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_TYPES_ACQUISITION_MODE = "acquisitionMode"

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
   * The section header for the marker types section of the file.
   */
  val SECTION_HEADER_MARKER_TYPES = "markerTypes"

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
   * The measurement type section field for the the measurement type.
   */
  val SECTION_FIELD_MEASUREMENT_TYPE = "measurementType"

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
  val SECTION_FIELD_SENSOR_TYPES_DATA_SOURCE_PROVIDERS = "dataSourceProviders"

  /**
   * The sensor types section field for the origin provider ID of a sensor type data source.
   */
  val SECTION_FIELD_SENSOR_TYPES_SENSOR_DATA_SOURCE_EXTERNAL_ID = "externalId"

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
   * The section field for the origin provider of a data source. The origin is where the data comes from, e.g.
   * an external provider like SmartThings.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_PROVIDER_ID = "dataSourceProviderProviderId"

  /**
   * The section field for the acquisition ID of a data source provider. The ID is the one used by the interface provider
   * to refer to the sensor.
   */
  val SECTION_FIELD_DATA_SOURCE_PROVIDER_ACQUISITION_ID = "dataSourceProviderAcquisitionId"

  /**
   * The section field for the update time limit for a sensor or sensed.
   */
  val SECTION_FIELD_STATE_UPDATE_TIME_LIMIT = "stateUpdateTimeLimit"

  /**
   * The section field for the heartbeat time limit for a sensor or sensed.
   */
  val SECTION_FIELD_HEARTBEAT_UPDATE_TIME_LIMIT = "heartbeatUpdateTimeLimit"

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
   * The field for the data source providers for a marker type.
   */
  val SECTION_FIELD_MARKER_TYPES_DATA_SOURCE_PROVIDERS = "dataSourceProviders"

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