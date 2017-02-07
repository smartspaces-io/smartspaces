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

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.scope.ManagedScope
import io.smartspaces.sensor.entity.InMemorySensorRegistry
import io.smartspaces.sensor.entity.SensorDescriptionImporter
import io.smartspaces.sensor.entity.SensorRegistry
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.StandardCompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.query.SensedEntityModelQueryProcessor
import io.smartspaces.sensor.entity.model.query.StandardSensedEntityModelQueryProcessor
import io.smartspaces.sensor.input.MqttSensorInput
import io.smartspaces.sensor.input.SensorInput
import io.smartspaces.sensor.input.StandardMqttSensorInput
import io.smartspaces.sensor.processing.ContinuousValueSensorValueProcessor
import io.smartspaces.sensor.processing.SensedEntitySensorHandler
import io.smartspaces.sensor.processing.SensedEntitySensorListener
import io.smartspaces.sensor.processing.SensorProcessor
import io.smartspaces.sensor.processing.SimpleMarkerSensorValueProcessor
import io.smartspaces.sensor.processing.StandardBleProximitySensorValueProcessor
import io.smartspaces.sensor.processing.StandardFilePersistenceSensorHandler
import io.smartspaces.sensor.processing.StandardFilePersistenceSensorInput
import io.smartspaces.sensor.processing.StandardSensedEntityModelProcessor
import io.smartspaces.sensor.processing.StandardSensedEntitySensorHandler
import io.smartspaces.sensor.processing.StandardSensorProcessor
import io.smartspaces.sensor.processing.StandardUnknownSensedEntityHandler
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.time.TimeFrequency
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

import java.io.File

/**
 * The sensor integration layer.
 *
 * @author Keith M. Hughes
 */
class StandardSensorIntegrator(private val spaceEnvironment: SmartSpacesEnvironment, private val managedScope: ManagedScope, private val log: ExtendedLog) extends SensorIntegrator with IdempotentManagedResource {

  /**
   * The sensor registry for the integrator.
   */
  private var sensorRegistry: SensorRegistry = null

  /**
   * The complete set of models of sensors and sensed entities.
   */
  private var completeSensedEntityModel: CompleteSensedEntityModel = null

  /**
   * The processor for queries against the models.
   */
  private var _queryProcessor: SensedEntityModelQueryProcessor = null

  /**
   * The description importer
   */
  var descriptionImporter: SensorDescriptionImporter = null

  /**
   * The sensor processor for the integrator
   */
  private var sensorProcessor: SensorProcessor = null

  def queryProcessor: SensedEntityModelQueryProcessor = _queryProcessor

  override def onStartup(): Unit = {
    sensorRegistry = new InMemorySensorRegistry()

    descriptionImporter.importDescriptions(sensorRegistry)

    completeSensedEntityModel =
      new StandardCompleteSensedEntityModel(sensorRegistry, log, spaceEnvironment)
    completeSensedEntityModel.prepare()

    _queryProcessor = new StandardSensedEntityModelQueryProcessor(completeSensedEntityModel)

    sensorProcessor = new StandardSensorProcessor(log)

    val sampleFile = new File("/var/tmp/sensordata.json")
    val liveData = true
    val sampleRecord = false

    var persistedSensorInput: StandardFilePersistenceSensorInput = null

    if (liveData) {

      if (sampleRecord) {
        val persistenceHandler = new StandardFilePersistenceSensorHandler(sampleFile);
        sensorProcessor.addSensorHandler(persistenceHandler);
      }
    } else {
      persistedSensorInput = new StandardFilePersistenceSensorInput(sampleFile);
      sensorProcessor.addSensorInput(persistedSensorInput);
    }

    val unknownSensedEntityHandler = new StandardUnknownSensedEntityHandler();

    val sensorHandler =
      new StandardSensedEntitySensorHandler(completeSensedEntityModel, unknownSensedEntityHandler, log)
    sensorRegistry.getSensorSensedEntityAssociations.foreach((association) =>
      sensorHandler.associateSensorWithEntity(association.sensor, association.sensedEntity))

    sensorHandler.addSensedEntitySensorListener(new SensedEntitySensorListener() {

      override def handleSensorData(handler: SensedEntitySensorHandler, timestamp: Long,
        sensor: SensorEntityModel, sensedEntity: SensedEntityModel,
        data: DynamicObject): Unit = {
        log.formatInfo("Got data at %s from sensor %s for entity %s: %s", timestamp.toString, sensor,
          sensedEntity, data.asMap())

      }
    });

    val modelProcessor =
      new StandardSensedEntityModelProcessor(completeSensedEntityModel, log)
    modelProcessor.addSensorValueProcessor(new StandardBleProximitySensorValueProcessor())
    modelProcessor.addSensorValueProcessor(new SimpleMarkerSensorValueProcessor())
    sensorRegistry.getAllMeasurementTypes().filter(_.valueType == "double").foreach {
      measurementType => modelProcessor.addSensorValueProcessor(new ContinuousValueSensorValueProcessor(measurementType))
    }

    sensorHandler.addSensedEntitySensorListener(modelProcessor)

    sensorProcessor.addSensorHandler(sensorHandler)

    managedScope.managedResources.addResource(sensorProcessor)

    val sensorCheckupTask = managedScope.managedTasks.scheduleAtFixedRate(new Runnable() {
      override def run(): Unit = {
        completeSensedEntityModel.checkModels()
      }
    }, TimeFrequency.timesPerHour(60.0), false)

    //    if (liveData) {
    //      if (sampleRecord) {
    //        // Recording
    //        SmartSpacesUtilities.delay(1000L * 60 * 2 * 10)
    //        //spaceEnvironment.shutdown()
    //
    //      }
    //    } else {
    //      // Playing back
    //      val latch = new CountDownLatch(1);
    //      val playableSensorInput = persistedSensorInput;
    //      spaceEnvironment.getExecutorService().submit(new Runnable() {
    //
    //        override def run(): Unit = {
    //          playableSensorInput.play()
    //          latch.countDown()
    //        }
    //      })
    //
    //      latch.await()
    //
    //      //spaceEnvironment.shutdown()
    //    } 
  }

  override def addMqttSensorInput(mqttBrokerDecription: MqttBrokerDescription, clientId: String): MqttSensorInput = {
    log.formatInfo("MQTT Broker URL %s", mqttBrokerDecription.brokerAddress)
    var mqttSensorInput = new StandardMqttSensorInput(mqttBrokerDecription,
      clientId, spaceEnvironment, log)
    sensorProcessor.addSensorInput(mqttSensorInput)

    return mqttSensorInput
  }

  override def addSensorInput(sensorInput: SensorInput): Unit = {
    sensorProcessor.addSensorInput(sensorInput)
  }
}