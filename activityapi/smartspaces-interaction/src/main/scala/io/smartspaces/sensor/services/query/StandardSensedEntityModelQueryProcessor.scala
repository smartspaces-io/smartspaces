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

package io.smartspaces.sensor.services.query

import io.smartspaces.messaging.codec.MessageEncoder
import io.smartspaces.sensor.model.CompleteSensedEntityModel
import io.smartspaces.sensor.model.PersonSensedEntityModel
import io.smartspaces.sensor.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.model.SensedValue
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.services.processing.UnknownMarkerHandler
import io.smartspaces.sensor.services.processing.UnknownSensedEntityHandler

/**
 * The standard processor for queries against a sensor model.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelQueryProcessor(private val allModels: CompleteSensedEntityModel,
    private val unknownMarkerHandler: UnknownMarkerHandler, private val unknownSensedEntityHander: UnknownSensedEntityHandler) extends SensedEntityModelQueryProcessor {

  override def getAllValuesForSensedEntity(sensedEntityExternalId: String): Option[Iterable[SensedValue[Any]]] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getSensedEntityModelByExternalId(sensedEntityExternalId)
      if (model.isDefined) {
        Option(model.get.getAllSensedValues())
      } else {
        None
      }
    }
  }

  override def getAllValuesForMeasurementType(measurementTypeExternalId: String): Iterable[SensedValue[Any]] = {
    allModels.doReadTransaction { () =>
      for (
        sensedEntityModel <- allModels.getAllSensedEntityModels(); sensedValue <- sensedEntityModel.getAllSensedValues();
        if sensedValue.measurementTypeDescription.externalId == measurementTypeExternalId
      ) yield sensedValue
    }
  }

  override def getOccupantsOfPhysicalSpace(physicalLocationExternalId: String): Option[Set[PersonSensedEntityModel]] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getPhysicalSpaceSensedEntityModelByExternalId(physicalLocationExternalId)
      if (model.isDefined) {
        Option(model.get.getOccupants)
      } else {
        None
      }
    }
  }

  override def getPhysicalSpace[T](id: String, encoder: MessageEncoder[PhysicalSpaceSensedEntityModel, T]): Option[T] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getPhysicalSpaceSensedEntityModelById(id)

      if (model.isDefined) {
        Some(encoder.encode(model.get))
      } else {
        None
      }
    }
  }

  override def getAllPhysicalSpaces[T](encoder: MessageEncoder[Iterable[PhysicalSpaceSensedEntityModel], T]): T = {
    allModels.doReadTransaction { () =>
      val models = allModels.getAllPhysicalSpaceSensedEntityModels()

      encoder.encode(models)
    }
  }

  override def getPerson[T](id: String, encoder: MessageEncoder[PersonSensedEntityModel, T]): Option[T] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getPersonSensedEntityModelById(id)

      if (model.isDefined) {
        Some(encoder.encode(model.get))
      } else {
        None
      }
    }
  }

  override def getAllPeople[T](encoder: MessageEncoder[Iterable[PersonSensedEntityModel], T]): T = {
    allModels.doReadTransaction { () =>
      val models = allModels.getAllPersonSensedEntityModels()

      encoder.encode(models)
    }
  }

  override def getSensor[T](id: String, encoder: MessageEncoder[SensorEntityModel, T]): Option[T] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getSensorEntityModelById(id)

      if (model.isDefined) {
        Some(encoder.encode(model.get))
      } else {
        None
      }
    }
  }

  override def getAllSensors[T](encoder: MessageEncoder[Iterable[SensorEntityModel], T]): T = {
    allModels.doReadTransaction { () =>
      val models = allModels.getAllSensorEntityModels()

      encoder.encode(models)
    }
  }

  override def getAllUnknownMarkerIds(): Set[String] = {
    allModels.doReadTransaction { () =>
      unknownMarkerHandler.getAllUnknownMarkerIds()
    }
  }

  override def getAllUnknownSensorIds(): Set[String] = {
    allModels.doReadTransaction { () =>
      unknownSensedEntityHander.getAllUnknownSensorIds()
    }
  }
}