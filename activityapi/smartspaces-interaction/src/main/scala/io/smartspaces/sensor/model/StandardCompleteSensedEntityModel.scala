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

package io.smartspaces.sensor.model

import java.util.concurrent.locks.ReentrantReadWriteLock

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.DataSourceAcquisitionModeCategoricalValueInstances
import io.smartspaces.sensor.domain.PersonSensedEntityDescription
import io.smartspaces.sensor.domain.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.domain.SensedEntityDescription
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.domain.SensorSensedEntityAssociationDescription
import io.smartspaces.sensor.services.domain.SensorInstanceRegistry
import io.smartspaces.sensor.services.processing.SensorProcessingEventEmitter
import io.smartspaces.sensor.services.processing.value.SensorValueProcessorRegistry
import io.smartspaces.system.SmartSpacesEnvironment

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * A collection of sensed entity models.
 *
 * @author Keith M. Hughes
 */
class StandardCompleteSensedEntityModel(
  private val sensorValueProcessorRegistry: SensorValueProcessorRegistry,
  override val sensorRegistry: SensorInstanceRegistry,
  override val eventEmitter: SensorProcessingEventEmitter,
  override val log: ExtendedLog,
  private val spaceEnvironment: SmartSpacesEnvironment) extends CompleteSensedEntityModel {

  /**
   * Map of IDs to their sensor entity models.
   */
  private val idToSensorEntityModels: Map[String, SensorEntityModel] = new HashMap

  /**
   * Map of external IDs to their sensor entity models.
   */
  private val externalIdToSensorEntityModels: Map[String, SensorEntityModel] = new HashMap

  /**
   * Map of IDs to their sensed entity models.
   */
  private val idToSensedEntityModels: Map[String, SensedEntityModel] = new HashMap

  /**
   * Map of external IDs to their sensed entity models.
   */
  private val externalIdToSensedEntityModels: Map[String, SensedEntityModel] = new HashMap

  /**
   * Map of physical space entity IDs to their models.
   */
  private val idToPhysicalSpaceModels: Map[String, PhysicalSpaceSensedEntityModel] = new HashMap

  /**
   * Map of physical space entity external IDs to their models.
   */
  private val externalIdToPhysicalSpaceModels: Map[String, PhysicalSpaceSensedEntityModel] = new HashMap

  /**
   * Map of person entity IDs to their models.
   */
  private val idToPersonModels: Map[String, PersonSensedEntityModel] = new HashMap

  /**
   * Map of person entity external IDs to their models.
   */
  private val externalIdToPersonModels: Map[String, PersonSensedEntityModel] = new HashMap

  /**
   * Map of marker IDs to their models.
   */
  private val markerIdToPersonModels: Map[String, PersonSensedEntityModel] = new HashMap

  /**
   * The readwrite lock for read/write transactions.
   */
  private val readWriteLock = new ReentrantReadWriteLock

  override def prepare(): Unit = {
    createModelsFromDescriptions()
  }

  /**
   * Create all models from the descriptions in the registry.
   */
  private def createModelsFromDescriptions(): Unit = {
    sensorRegistry.getAllSensorEntities.foreach(addNewSensorEntity(_))

    sensorRegistry.getAllSensedEntities.foreach(addNewSensedEntity(_))

    sensorRegistry
      .getMarkerMarkedEntityAssociations.foreach((association) =>
        markerIdToPersonModels.put(
          association.marker.markerId,
          externalIdToPersonModels.get(association.markable.externalId).get))

    sensorRegistry
      .getSensorSensedEntityAssociations.foreach(associateSensorWithSensed(_))
  }

  override def addNewSensorEntity(entityDescription: SensorEntityDescription): Unit = {
    registerSensorModel(new SimpleSensorEntityModel(entityDescription, this, spaceEnvironment.getTimeProvider.getCurrentTime))
  }

  /**
   * Register a sensor model.
   *
   * <p>
   * This is exposed for testing.
   */
  private[model] def registerSensorModel(model: SensorEntityModel): Unit = {
    idToSensorEntityModels.put(model.sensorEntityDescription.id, model)
    externalIdToSensorEntityModels.put(model.sensorEntityDescription.externalId, model)
  }

  override def addNewSensedEntity(entityDescription: SensedEntityDescription): Unit = {
    val externalId = entityDescription.externalId
    val id = entityDescription.id

    var model: SensedEntityModel = null
    if (entityDescription.isInstanceOf[PhysicalSpaceSensedEntityDescription]) {
      model = new SimplePhysicalSpaceSensedEntityModel(
        entityDescription.asInstanceOf[PhysicalSpaceSensedEntityDescription], this)
      idToPhysicalSpaceModels.put(id, model.asInstanceOf[PhysicalSpaceSensedEntityModel])
      externalIdToPhysicalSpaceModels.put(externalId, model.asInstanceOf[PhysicalSpaceSensedEntityModel])
    } else if (entityDescription.isInstanceOf[PersonSensedEntityDescription]) {
      model = new SimplePersonSensedEntityModel(
        entityDescription.asInstanceOf[PersonSensedEntityDescription],
        this)
      idToPersonModels.put(id, model.asInstanceOf[PersonSensedEntityModel])
      externalIdToPersonModels.put(externalId, model.asInstanceOf[PersonSensedEntityModel])
    } else {
      model = new SimpleSensedEntityModel(entityDescription, this)
    }

    idToSensedEntityModels.put(id, model)
    externalIdToSensedEntityModels.put(externalId, model)
  }

  override def associateSensorWithSensed(association: SensorSensedEntityAssociationDescription): Unit = {
    val sensorModel = externalIdToSensorEntityModels.get(association.sensor.externalId)
    val sensedModel = externalIdToSensedEntityModels.get(association.sensedEntity.externalId)

    val sensedMeasurementType = association.sensorChannelDetail.measurementType

    // TODO(keith): Place the processor directly in the sensor channel model
    val sensorValueProcessor = sensorValueProcessorRegistry.getSensorValueProcessor(sensedMeasurementType.externalId)
    if (sensorValueProcessor.isDefined) {
      val channelModel = new SimpleSensorChannelEntityModel(
        association.sensorChannelDetail,
        sensorModel.get,
        sensedModel.get,
        sensorValueProcessor.get,
        this,
        sensorModel.get.timestampItemCreation)

      sensorModel.get.addSensorChannelModel(channelModel)
      sensedModel.get.addSensorChannelModel(channelModel)
    } else {
      log.warn(s"could not find sensor value processor for measurement type ${sensedMeasurementType.externalId} for sensor association")
    }
  }

  override def getSensorEntityModelById(id: String): Option[SensorEntityModel] = {
    idToSensorEntityModels.get(id)
  }

  override def getSensorEntityModelByExternalId(externalId: String): Option[SensorEntityModel] = {
    externalIdToSensorEntityModels.get(externalId)
  }

  override def getAllSensorEntityModels(): Iterable[SensorEntityModel] = {
    externalIdToSensorEntityModels.values
  }

  override def getAllSensorEntityModelsForMeasurementTypeExternalId(measurementTypeExternalId: String): Iterable[SensorEntityModel] = {
    externalIdToSensorEntityModels.values.filter(_.hasMeasurementType(measurementTypeExternalId))
  }

  override def getAllSensorEntityModelsForAcquisitionMode(
    acquisitionMode: DataSourceAcquisitionModeCategoricalValueInstances.DataSourceAcquisitionModeCategoricalValueInstance): Iterable[SensorEntityModel] = {
    externalIdToSensorEntityModels.values.filter(
      _.sensorEntityDescription.dataSource.acquisitionMode == acquisitionMode)
  }

  override def getAllSensorEntityModelsForSensorTypeExternalId(sensorTypeExternalId: String): Iterable[SensorEntityModel] = {
    externalIdToSensorEntityModels.values.filter(_.sensorEntityDescription.externalId == sensorTypeExternalId)
  }

  override def getAllSensorChannelEntityModelsForMeasurementTypeExternalId(measurementTypeExternalId: String): Iterable[SensorChannelEntityModel] = {
    externalIdToSensorEntityModels.values.flatMap(_.getMeasurementTypeChannels(measurementTypeExternalId))
  }

  override def getSensedEntityModelById(id: String): Option[SensedEntityModel] = {
    idToSensedEntityModels.get(id)
  }

  override def getSensedEntityModelByExternalId(externalId: String): Option[SensedEntityModel] = {
    externalIdToSensedEntityModels.get(externalId)
  }

  override def getAllSensedEntityModels(): Iterable[SensedEntityModel] = {
    externalIdToSensedEntityModels.values
  }

  override def getPhysicalSpaceSensedEntityModelById(id: String): Option[PhysicalSpaceSensedEntityModel] = {
    idToPhysicalSpaceModels.get(id)
  }

  override def getPhysicalSpaceSensedEntityModelByExternalId(externalId: String): Option[PhysicalSpaceSensedEntityModel] = {
    externalIdToPhysicalSpaceModels.get(externalId)
  }

  override def getAllPhysicalSpaceSensedEntityModels(): Iterable[PhysicalSpaceSensedEntityModel] = {
    externalIdToPhysicalSpaceModels.values
  }

  override def getAllPhysicalSpaceSensedEntityModelsForPhysicalSpaceTypeExternalId(physicalSpaceTypeExternalId: String): Iterable[PhysicalSpaceSensedEntityModel] = {
    externalIdToPhysicalSpaceModels.values.filter { model =>
      val pstype = model.sensedEntityDescription.physicalSpaceType
      pstype.isDefined && pstype.get == physicalSpaceTypeExternalId
    }
  }

  override def getPersonSensedEntityModelById(id: String): Option[PersonSensedEntityModel] = {
    idToPersonModels.get(id)
  }

  override def getPersonSensedEntityModelByExternalId(externalId: String): Option[PersonSensedEntityModel] = {
    externalIdToPersonModels.get(externalId)
  }

  override def getAllPersonSensedEntityModels(): Iterable[PersonSensedEntityModel] = {
    externalIdToPersonModels.values
  }

  override def getMarkedSensedEntityModelByMarkerId(markerId: String): Option[PersonSensedEntityModel] = {
    markerIdToPersonModels.get(markerId)
  }

  override def checkModels(): Unit = {
    doVoidWriteTransaction { () =>
      performModelCheck()
    }
  }

  /**
   * Perform all model checks.
   */
  private[model] def performModelCheck(): Unit = {
    val currentTime = spaceEnvironment.getTimeProvider.getCurrentTime

    log.debug(s"Performing sensor model check at ${currentTime}")

    getAllSensorEntityModels().filter(_.sensorEntityDescription.active).foreach { sensor =>
      val sensorOffline = sensor.checkIfOfflineTransition(currentTime)

      if (!sensorOffline) {
        sensor.getAllSensorChannelModels().foreach { channel =>
          channel.checkIfOfflineTransition(currentTime)
        }
      }
    }
  }

  override def doVoidReadTransaction(transaction: () => Unit): Unit = {
    readWriteLock.readLock().lock()

    try {
      transaction()
    } finally {
      readWriteLock.readLock().unlock()
    }
  }

  override def doVoidWriteTransaction(transaction: () => Unit): Unit = {
    readWriteLock.writeLock().lock()

    try {
      transaction()
    } finally {
      readWriteLock.writeLock().unlock()
    }
  }

  override def doReadTransaction[T](transaction: () => T): T = {
    readWriteLock.readLock().lock()

    try {
      transaction()
    } finally {
      readWriteLock.readLock().unlock()
    }
  }

  override def doWriteTransaction[T](transaction: () => T): T = {
    readWriteLock.writeLock().lock()

    try {
      transaction()
    } finally {
      readWriteLock.writeLock().unlock()
    }
  }
}
