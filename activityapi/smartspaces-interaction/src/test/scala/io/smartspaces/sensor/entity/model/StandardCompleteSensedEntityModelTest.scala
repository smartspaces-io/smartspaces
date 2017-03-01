/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
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

import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.sensor.entity.SensorRegistry
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.time.provider.SettableTimeProvider

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.MockitoAnnotations.Mock
import org.scalatest.junit.JUnitSuite
import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.sensor.processing.SensorProcessingEventEmitter

/**
 * Test the {@link #StandardCompleteSensedEntityModel}.
 *
 * @author Keith M. Hughes
 */
class StandardCompleteSensedEntityModelTest extends JUnitSuite {
  var allModels: StandardCompleteSensedEntityModel = null

  @Mock var sensorRegistry: SensorRegistry = null

  @Mock var eventObservableRegistry: EventObservableRegistry = null
  
  @Mock var eventEmitter: SensorProcessingEventEmitter = null

  @Mock var log: ExtendedLog = null

  @Mock var spaceEnvironment: SmartSpacesEnvironment = null

  val timeProvider = new SettableTimeProvider

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    Mockito.when(spaceEnvironment.getTimeProvider).thenReturn(timeProvider)
    Mockito.when(spaceEnvironment.getEventObservableRegistry).thenReturn(eventObservableRegistry)

    allModels = new StandardCompleteSensedEntityModel(sensorRegistry, eventEmitter, log, spaceEnvironment)
  }

  @Test def testModelUpdate(): Unit = {
    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensorDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)

    val externalId = "foo"

    Mockito.when(sensorDescription.externalId).thenReturn(externalId)
    Mockito.when(sensorDescription.active).thenReturn(true)

    timeProvider.setCurrentTime(10000)

    allModels.registerSensorModel(sensorModel)

    val checkTime = 12345l
    timeProvider.setCurrentTime(checkTime)

    allModels.performModelCheck()

    Mockito.verify(sensorModel).checkIfOfflineTransition(checkTime)
  }
}