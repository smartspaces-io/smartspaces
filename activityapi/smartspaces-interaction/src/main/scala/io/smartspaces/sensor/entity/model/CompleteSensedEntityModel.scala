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

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.entity.SensedEntityDescription
import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.sensor.entity.SensorRegistry
import io.smartspaces.sensor.entity.SensorSensedEntityAssociation
import io.smartspaces.sensor.processing.SensorProcessingEventEmitter

/**
 * A collection of sensed entity models.
 *
 * @author Keith M. Hughes
 */
trait CompleteSensedEntityModel {
  
  /**
   * Logger to be used with the model.
   */
  val log: ExtendedLog
  
  /**
   * The event emitter.
   * 
   * TODO(keith): Maybe hand this in some how with method calls?
   */
  val eventEmitter: SensorProcessingEventEmitter

  /**
   * Prepare the collection.
   *
   * <p>
   * This will include building sensing models that current exist.
   */
  def prepare(): Unit
  
  /**
   * Add in a new sensor entity into the collection.
   *
   * @param entityDescription
   *          the new description
   */
  def addNewSensorEntity(entityDescription: SensorEntityDescription): Unit
  
  /**
   * Add in a new sensed entity into the collection.
   *
   * @param entityDescription
   *          the new description
   */
  def addNewSensedEntity(entityDescription: SensedEntityDescription): Unit
  
  /*
   * Associate a sensor model with the sensed item.
   *
   * @param association
   * 		the association to add
   */
  def associateSensorWithSensed(association: SensorSensedEntityAssociation): Unit

  /**
   * Get the sensor entity model for a given entity ID.
   *
   * @param id
   *          the ID of the entity
   *
   * @return the model
   */
  def getSensorEntityModelById(id: String): Option[SensorEntityModel]

  /**
   * Get the sensor entity model for a given entity external ID.
   *
   * @param externalId
   *          the external ID of the entity
   *
   * @return the model
   */
  def getSensorEntityModelByExternalId(externalId: String): Option[SensorEntityModel]

  /**
   * Get all sensor entity models in the collection.
   *
   * @return the models
   */
  def getAllSensorEntityModels(): List[SensorEntityModel]

  /**
   * Get the sensed entity model for a given entity ID.
   *
   * @param id
   *          the ID of the entity
   *
   * @return the model
   */
  def getSensedEntityModelById(id: String): Option[SensedEntityModel]

  /**
   * Get the sensed entity model for a given entity external ID.
   *
   * @param externalId
   *          the external ID of the entity
   *
   * @return the model
   */
  def getSensedEntityModelByExternalId(externalId: String): Option[SensedEntityModel]

  /**
   * Get all sensed entity models in the collection.
   *
   * @return the models
   */
  def getAllSensedEntityModels(): List[SensedEntityModel]

  /**
   * Get the model for a given physical space entity ID.
   *
   * @param id
   *          the external ID of the entity
   *
   * @return the model
   */
  def getPhysicalSpaceSensedEntityModelById(id: String): Option[PhysicalSpaceSensedEntityModel]

  /**
   * Get the model for a given physical space entity external ID.
   *
   * @param externalId
   *          the external ID of the entity
   *
   * @return the model
   */
  def getPhysicalSpaceSensedEntityModelByExternalId(externalId: String): Option[PhysicalSpaceSensedEntityModel]

  /**
   * Get all physical space models in the collection.
   *
   * @return the models
   */
  def getAllPhysicalSpaceSensedEntityModels(): List[PhysicalSpaceSensedEntityModel]

  /**
   * Get the model for a given person entity ID.
   *
   * @param id
   *          the ID of the entity
   *
   * @return the model
   */
  def getPersonSensedEntityModelById(id: String): Option[PersonSensedEntityModel]

  /**
   * Get the model for a given person entity external ID.
   *
   * @param externalId
   *          the external ID of the entity
   *
   * @return the model
   */
  def getPersonSensedEntityModelByExternalId(externalId: String): Option[PersonSensedEntityModel]

  /**
   * Get all person models in the collection.
   *
   * @return the models
   */
  def getAllPersonSensedEntityModels(): List[PersonSensedEntityModel]

  /**
   * Get the model for a given marked entity ID.
   *
   * @param markerId
   *          the marker ID associated with the entity
   *
   * @return the model
   */
  def getMarkedSensedEntityModelByMarkerId(markerId: String): Option[PersonSensedEntityModel]

  /**
   * Check all models for  things like going offline.
   */
  def checkModels(): Unit

  /**
   * The sensor registry for the collection.
   */
  val sensorRegistry: SensorRegistry

  /**
   * Perform an operations within a read transaction.
   *
   * <p>
   * Multiple readers can run at the same time.
   *
   * @param transaction
   *          the code to run inside the transaction
   */
  def doVoidReadTransaction(transaction: () => Unit): Unit

  /**
   * Perform an operations within a write transaction.
   *
   * <p>
   * Only one writer can run at a time.
   *
   * @param transaction
   *          the code to run inside the transaction
   */
  def doVoidWriteTransaction(transaction: () => Unit): Unit

  /**
   * Perform an operations within a read transaction.
   *
   * <p>
   * Only one writer can run at a time.
   *
   * @param transaction
   *          the code to run inside the transaction
   *
   * @returns the result of the transaction
   */
  def doReadTransaction[T](transaction: () => T): T

  /**
   * Perform an operations within a write transaction.
   *
   * <p>
   * Only one writer can run at a time.
   *
   * @param transaction
   *          the code to run inside the transaction
   *
   * @returns the result of the transaction
   */
  def doWriteTransaction[T](transaction: () => T): T
}