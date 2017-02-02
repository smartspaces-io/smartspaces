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

package io.smartspaces.sensor.entity.model.query

import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.value.converter.ObjectConverter
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedValue

/**
 * The standard processor for queries against a sensor model.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelQueryProcessor(private val allModels: CompleteSensedEntityModel) extends SensedEntityModelQueryProcessor {

  override def getAllValuesForSensedEntity(sensedEntityId: String): Option[List[SensedValue[Any]]] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getSensedEntityModel(sensedEntityId)
      if (model.isDefined) {
        Option(model.get.getAllSensedValues())
      } else {
        None
      }
    }
  }

  override def getAllValuesForMeasurementType(measurementTypeId: String): List[SensedValue[Any]] = {
    allModels.doReadTransaction { () =>
      for (sensedEntityModel <- allModels.getAllSensedEntityModels(); sensedValue <- sensedEntityModel.getAllSensedValues(); if sensedValue.valueType.externalId == measurementTypeId)
        yield sensedValue
    }
  }

  override def getOccupantsOfPhysicalLocation(physicalLocationId: String): Option[Set[PersonSensedEntityModel]] = {
    allModels.doReadTransaction { () =>
      val model = allModels.getPhysicalSpaceSensedEntityModel(physicalLocationId)
      if (model.isDefined) {
        Option(model.get.getOccupants)
      } else {
        None
      }
    }
  }

  override def getAllPhysicalLocations[T](converter: ObjectConverter[List[PhysicalSpaceSensedEntityModel], T]): T = {
    allModels.doReadTransaction { () =>
      val models = allModels.getAllPhysicalSpaceSensedEntityModels()

      converter.convert(models)
    }
  }
}