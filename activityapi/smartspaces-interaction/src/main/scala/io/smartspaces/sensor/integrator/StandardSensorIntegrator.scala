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

package io.smartspaces.sensor.integrator

import io.smartspaces.data.entity.StandardValueRegistry
import io.smartspaces.data.entity.ValueRegistry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.scope.ManagedScope
import io.smartspaces.sensor.entity.InMemorySensorCommonRegistry
import io.smartspaces.sensor.entity.InMemorySensorInstanceRegistry
import io.smartspaces.sensor.entity.MeasurementTypeDescription
import io.smartspaces.sensor.entity.SensorCommonDescriptionImporter
import io.smartspaces.sensor.entity.SensorCommonRegistry
import io.smartspaces.sensor.entity.SensorInstanceDescriptionImporter
import io.smartspaces.sensor.entity.SensorInstanceRegistry
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.StandardCompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.query.SensedEntityModelQueryProcessor
import io.smartspaces.sensor.entity.model.query.StandardSensedEntityModelQueryProcessor
import io.smartspaces.sensor.messaging.input.MqttSensorInput
import io.smartspaces.sensor.messaging.input.SensorInput
import io.smartspaces.sensor.messaging.input.StandardMqttSensorInput
import io.smartspaces.sensor.messaging.messages.StandardSensorData
import io.smartspaces.sensor.processing.SensedEntitySensorHandler
import io.smartspaces.sensor.processing.SensedEntitySensorMessageHandler
import io.smartspaces.sensor.processing.SensorProcessor
import io.smartspaces.sensor.processing.StandardSensedEntityModelProcessor
import io.smartspaces.sensor.processing.StandardSensedEntitySensorHandler
import io.smartspaces.sensor.processing.StandardSensorProcessingEventEmitter
import io.smartspaces.sensor.processing.StandardSensorProcessor
import io.smartspaces.sensor.processing.StandardUnknownMarkerHandler
import io.smartspaces.sensor.processing.StandardUnknownSensedEntityHandler
import io.smartspaces.sensor.processing.UnknownMarkerHandler
import io.smartspaces.sensor.processing.UnknownSensedEntityHandler
import io.smartspaces.sensor.processing.value.CategoricalValueSensorValueProcessor
import io.smartspaces.sensor.processing.value.NumericContinuousValueSensorValueProcessor
import io.smartspaces.sensor.processing.value.SensorValueProcessorRegistry
import io.smartspaces.sensor.processing.value.SimpleMarkerSensorValueProcessor
import io.smartspaces.sensor.processing.value.StandardBleProximitySensorValueProcessor
import io.smartspaces.sensor.processing.value.StandardSensorValueProcessorRegistry
import io.smartspaces.sensor.processing.value.StatefulMarkerSensorSensorValueProcessor
import io.smartspaces.sensor.value.entity.ActiveCategoricalValue
import io.smartspaces.sensor.value.entity.ContactCategoricalValue
import io.smartspaces.sensor.value.entity.MoistureCategoricalValue
import io.smartspaces.sensor.value.entity.PresenceCategoricalValue
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.time.TimeFrequency
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * The sensor integration layer.
 *
 * @author Keith M. Hughes
 */
class StandardSensorIntegrator(
    private val _sensorCommonRegistry: SensorCommonRegistry,
    private val _sensorInstanceRegistry: SensorInstanceRegistry,
    private val spaceEnvironment: SmartSpacesEnvironment, 
    private val managedScope: ManagedScope, 
    private val log: ExtendedLog) extends SensorIntegrator with IdempotentManagedResource {

  /**
   * The complete set of models of sensors and sensed entities.
   */
  private var _completeSensedEntityModel: CompleteSensedEntityModel = _

  /**
   * The processor for queries against the models.
   */
  private var _queryProcessor: SensedEntityModelQueryProcessor = _

  /**
   * The collection of event emitters.
   */
  val eventEmitter = new StandardSensorProcessingEventEmitter(spaceEnvironment, log)

  /**
   * The handler for unknown sensed entities.
   */
  val unknownSensedEntityHandler: UnknownSensedEntityHandler = new StandardUnknownSensedEntityHandler()

  /**
   * The handler for unknown markers.
   */
  val unknownMarkerHandler: UnknownMarkerHandler = new StandardUnknownMarkerHandler(eventEmitter)

  /**
   * The sensor processor for the integrator
   */
  private var sensorProcessor: SensorProcessor = _

  /**
   * The value registry with a collection of base values.
   */
  private val _valueRegistry: ValueRegistry = StandardValueRegistry.registerCategoricalValues(
    ContactCategoricalValue, PresenceCategoricalValue, ActiveCategoricalValue, MoistureCategoricalValue)

  /**
   * The registry for sensor value processors.
   */
  private var sensorValueProcessorRegistry: SensorValueProcessorRegistry = _

  override def valueRegistry: ValueRegistry = _valueRegistry

  override def queryProcessor: SensedEntityModelQueryProcessor = _queryProcessor

  override def sensorCommonRegistry: SensorCommonRegistry = _sensorCommonRegistry

  override def sensorInstanceRegistry: SensorInstanceRegistry = _sensorInstanceRegistry

  override def completeSensedEntityModel: CompleteSensedEntityModel = _completeSensedEntityModel

  override def onStartup(): Unit = {
    sensorValueProcessorRegistry = new StandardSensorValueProcessorRegistry(log)
    sensorValueProcessorRegistry.addSensorValueProcessor(new StandardBleProximitySensorValueProcessor())
    sensorValueProcessorRegistry.addSensorValueProcessor(new SimpleMarkerSensorValueProcessor(unknownMarkerHandler))

    val statefulMarkerMeasurementType = _sensorCommonRegistry.getMeasurementTypeByExternalId(StandardSensorData.MEASUREMENT_TYPE_MARKER_STATEFUL)
    if (statefulMarkerMeasurementType.isDefined) {
      sensorValueProcessorRegistry.addSensorValueProcessor(new StatefulMarkerSensorSensorValueProcessor(statefulMarkerMeasurementType.get, unknownMarkerHandler))
    } else {
      log.warn(s"Could not find stateful marker measurement type ${StandardSensorData.MEASUREMENT_TYPE_MARKER_STATEFUL}")
    }

    _sensorCommonRegistry.getAllMeasurementTypes.filter(_.processingType == StandardSensorData.MEASUREMENT_PROCESSING_TYPE_SIMPLE).
      foreach { measurementType =>
        measurementType.valueType match {
          case MeasurementTypeDescription.VALUE_TYPE_NUMERIC_CONTINUOUS =>
            sensorValueProcessorRegistry.addSensorValueProcessor(new NumericContinuousValueSensorValueProcessor(measurementType))
          case mtype if mtype.startsWith(MeasurementTypeDescription.VALUE_TYPE_PREFIX_CATEGORICAL_VARIABLE) =>
            val categoricalVariableName = mtype.substring(MeasurementTypeDescription.VALUE_TYPE_PREFIX_CATEGORICAL_VARIABLE.length())
            val categoricalVariable = _valueRegistry.getCategoricalValue(categoricalVariableName)
            if (categoricalVariable.isDefined) {
              sensorValueProcessorRegistry.addSensorValueProcessor(new CategoricalValueSensorValueProcessor(measurementType, categoricalVariable.get))
            } else {
              log.warn(s"Unknown categorical variable ${categoricalVariableName}")
            }
          case mtype =>
            log.warn(s"Unknown measurement type ${mtype}")
        }
      }

    _completeSensedEntityModel =
      new StandardCompleteSensedEntityModel(_sensorInstanceRegistry, eventEmitter, log, spaceEnvironment)
    _completeSensedEntityModel.prepare()

    _queryProcessor = new StandardSensedEntityModelQueryProcessor(completeSensedEntityModel, unknownMarkerHandler, unknownSensedEntityHandler)

    sensorProcessor = new StandardSensorProcessor(managedScope, log)

    val sensorHandler =
      new StandardSensedEntitySensorHandler(completeSensedEntityModel, unknownSensedEntityHandler, log)

    sensorHandler.addSensedEntitySensorMessageHandler(new SensedEntitySensorMessageHandler() {

      override def handleNewSensorMessage(handler: SensedEntitySensorHandler, timestamp: Long,
        sensor: SensorEntityModel,
        message: DynamicObject): Unit = {
        if (log.isDebugEnabled()) {
          log.debug(s"Got message at ${timestamp.toString} from sensor ${sensor}: ${message.asMap()}")
        }
      }
    })

    val modelProcessor =
      new StandardSensedEntityModelProcessor(sensorValueProcessorRegistry, completeSensedEntityModel, managedScope, log)

    sensorHandler.addSensedEntitySensorMessageHandler(modelProcessor)

    sensorProcessor.addSensorHandler(sensorHandler)

    managedScope.managedResources.addResource(sensorProcessor)

    val sensorCheckupTask = managedScope.managedTasks.scheduleAtFixedRate(new Runnable() {
      override def run(): Unit = {
        completeSensedEntityModel.checkModels()
      }
    }, TimeFrequency.timesPerHour(30.0), false)
  }

  override def addMqttSensorInput(mqttEndpoint: MqttCommunicationEndpoint): MqttSensorInput = {
    log.info(s"MQTT sensor input from MQTT broker at URL ${mqttEndpoint.getMqttBrokerDescription()}")
    var mqttSensorInput = new StandardMqttSensorInput(mqttEndpoint, spaceEnvironment, log)
    sensorProcessor.addSensorInput(mqttSensorInput)

    return mqttSensorInput
  }

  override def addSensorInput(sensorInput: SensorInput): Unit = {
    sensorProcessor.addSensorInput(sensorInput)
  }
}