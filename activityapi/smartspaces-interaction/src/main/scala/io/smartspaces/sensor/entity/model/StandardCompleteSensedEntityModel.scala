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

package io.smartspaces.sensor.entity.model

import io.smartspaces.event.observable.EventPublisherSubject
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.entity.PersonSensedEntityDescription
import io.smartspaces.sensor.entity.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.entity.SensedEntityDescription
import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.sensor.entity.SensorRegistry
import io.smartspaces.sensor.entity.SimpleSensorSensedEntityAssociation

import java.util.concurrent.locks.ReentrantReadWriteLock

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.sensor.entity.model.event.SensorOfflineEvent
import io.smartspaces.sensor.entity.model.event.PhysicalLocationOccupancyEvent
import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.event.observable.ObservableCreator

/**
 * A collection of sensed entity models.
 *
 * @author Keith M. Hughes
 */
class StandardCompleteSensedEntityModel(val sensorRegistry: SensorRegistry,
    override val log: ExtendedLog, private val spaceEnvironment: SmartSpacesEnvironment) extends CompleteSensedEntityModel {

  /**
   * Map of entity IDs to their sensor entity models.
   */
  private val idToSensorEntityModels: Map[String, SensorEntityModel] = new HashMap

  /**
   * Map of entity IDs to their sensed entity models.
   */
  private val idToSensedEntityModels: Map[String, SensedEntityModel] = new HashMap

  /**
   * Map of physical entity IDs to their models.
   */
  private val idToPhysicalSpaceModels: Map[String, PhysicalSpaceSensedEntityModel] = new HashMap

  /**
   * Map of person entity IDs to their models.
   */
  private val idToPersonModels: Map[String, PersonSensedEntityModel] = new HashMap

  /**
   * Map of marker IDs to their models.
   */
  private val markerIdToPersonModels: Map[String, PersonSensedEntityModel] = new HashMap

  /**
   * The creator for physical occupancy observables.
   */
  private val physicalLocationOccupancyEventCreator: ObservableCreator[EventPublisherSubject[PhysicalLocationOccupancyEvent]] =
    new ObservableCreator[EventPublisherSubject[PhysicalLocationOccupancyEvent]]() {
      override def newObservable(): EventPublisherSubject[PhysicalLocationOccupancyEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for physical location occupancy events
   */
  private var physicalLocationOccupancyEventSubject: EventPublisherSubject[PhysicalLocationOccupancyEvent] = null

  /**
   * The subject for sensor offline events
   */
  private var sensorOfflineEventSubject: EventPublisherSubject[SensorOfflineEvent] = null

  /**
   * The creator for sensor offline observables.
   */
  private val sensorOfflineEventCreator: ObservableCreator[EventPublisherSubject[SensorOfflineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorOfflineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorOfflineEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The readwrite lock for the
   */
  private val readWriteLock = new ReentrantReadWriteLock

  override def prepare(): Unit = {
    val eventObservableRegistry = spaceEnvironment.getEventObservableRegistry
    physicalLocationOccupancyEventSubject =
      eventObservableRegistry.getObservable(PhysicalLocationOccupancyEvent.EVENT_TYPE,
        physicalLocationOccupancyEventCreator)

    sensorOfflineEventSubject =
      eventObservableRegistry.getObservable(SensorOfflineEvent.EVENT_TYPE,
        sensorOfflineEventCreator)

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
        markerIdToPersonModels.put(association.marker.markerId,
          idToPersonModels.get(association.markable.externalId).get))

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
    idToSensorEntityModels.put(model.sensorEntityDescription.externalId, model)
  }

  override def addNewSensedEntity(entityDescription: SensedEntityDescription): Unit = {
    val externalId = entityDescription.externalId

    var model: SensedEntityModel = null
    if (entityDescription.isInstanceOf[PhysicalSpaceSensedEntityDescription]) {
      model = new SimplePhysicalSpaceSensedEntityModel(
        entityDescription.asInstanceOf[PhysicalSpaceSensedEntityDescription], this)
      idToPhysicalSpaceModels.put(externalId, model.asInstanceOf[PhysicalSpaceSensedEntityModel])
    } else if (entityDescription.isInstanceOf[PersonSensedEntityDescription]) {
      model = new SimplePersonSensedEntityModel(entityDescription.asInstanceOf[PersonSensedEntityDescription],
        this)
      idToPersonModels.put(externalId, model.asInstanceOf[PersonSensedEntityModel])
    } else {
      model = new SimpleSensedEntityModel(entityDescription, this)
    }

    idToSensedEntityModels.put(externalId, model)
  }

  override def associateSensorWithSensed(association: SimpleSensorSensedEntityAssociation): Unit = {
    val sensor = idToSensorEntityModels.get(association.sensor.externalId)
    val sensed = idToSensedEntityModels.get(association.sensedEntity.externalId)

    sensor.get.sensedEntityModel = sensed
    sensed.get.sensorEntityModel = sensor
  }

  override def getSensorEntityModel(externalId: String): Option[SensorEntityModel] = {
    idToSensorEntityModels.get(externalId)
  }

  override def getAllSensorEntityModels(): scala.collection.immutable.List[SensorEntityModel] = {
    idToSensorEntityModels.values.toList
  }

  override def getSensedEntityModel(externalId: String): Option[SensedEntityModel] = {
    idToSensedEntityModels.get(externalId)
  }

  override def getAllSensedEntityModels(): scala.collection.immutable.List[SensedEntityModel] = {
    idToSensedEntityModels.values.toList
  }

  override def getPhysicalSpaceSensedEntityModel(externalId: String): Option[PhysicalSpaceSensedEntityModel] = {
    idToPhysicalSpaceModels.get(externalId)
  }

  override def getAllPhysicalSpaceSensedEntityModels(): scala.collection.immutable.List[PhysicalSpaceSensedEntityModel] = {
    idToPhysicalSpaceModels.values.toList
  }

  override def getPersonSensedEntityModel(externalId: String): Option[PersonSensedEntityModel] = {
    idToPersonModels.get(externalId)
  }

  override def getAllPersonSensedEntityModels(): scala.collection.immutable.List[PersonSensedEntityModel] = {
    idToPersonModels.values.toList
  }

  override def getMarkedSensedEntityModel(markerId: String): Option[PersonSensedEntityModel] = {
    markerIdToPersonModels.get(markerId)
  }

  override def broadcastOccupanyEvent(event: PhysicalLocationOccupancyEvent): Unit = {
    physicalLocationOccupancyEventSubject.onNext(event)
  }

  override def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit = {
    log.warn("Broadcasting sensor offline event " + event.sensorModel.sensorEntityDescription)
    sensorOfflineEventSubject.onNext(event)
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

    log.formatInfo("Performing sensor model check at " + currentTime)

    getAllSensorEntityModels().filter(_.sensorEntityDescription.active).foreach {
      _.checkIfOfflineTransition(currentTime)
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
