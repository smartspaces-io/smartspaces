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

package io.smartspaces.sensor.model

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.services.domain.SensorInstanceRegistry
import io.smartspaces.sensor.services.processing.SensorProcessingEventEmitter
import io.smartspaces.sensor.services.processing.value.SensorValueProcessorRegistry
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.time.provider.SettableTimeProvider

/**
 * Test the {@link #StandardCompleteSensedEntityModel}.
 *
 * @author Keith M. Hughes
 */
class StandardCompleteSensedEntityModelTest extends JUnitSuite {
  var allModels: StandardCompleteSensedEntityModel = _

  @Mock var sensorValueProcessorRegistry: SensorValueProcessorRegistry = _

  @Mock var sensorRegistry: SensorInstanceRegistry = _

  @Mock var eventObservableRegistry: EventObservableRegistry = _

  @Mock var eventEmitter: SensorProcessingEventEmitter = _

  @Mock var log: ExtendedLog = null

  @Mock var spaceEnvironment: SmartSpacesEnvironment = _

  val timeProvider = new SettableTimeProvider

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    Mockito.when(spaceEnvironment.getTimeProvider).thenReturn(timeProvider)
    Mockito.when(spaceEnvironment.getEventObservableRegistry).thenReturn(eventObservableRegistry)

    allModels = new StandardCompleteSensedEntityModel(
      sensorValueProcessorRegistry, sensorRegistry, eventEmitter, log, spaceEnvironment)
  }

  /**
   * Test that a simple sensor model happens and that no event is emitted.
   */
  @Test def testModelCheck(): Unit = {
    val channelModel1 = Mockito.mock(classOf[SensorChannelEntityModel])
    val channelModel2 = Mockito.mock(classOf[SensorChannelEntityModel])

    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensorDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)
    Mockito.when(sensorModel.getAllSensorChannelModels()).thenReturn(List(channelModel1, channelModel2))

    val checkTime = 12345l
    Mockito.when(sensorModel.checkIfOfflineTransition(checkTime)).thenReturn(false)

    val externalId = "foo"

    Mockito.when(sensorDescription.externalId).thenReturn(externalId)
    Mockito.when(sensorDescription.active).thenReturn(true)

    timeProvider.setCurrentTime(10000)

    allModels.registerSensorModel(sensorModel)

    timeProvider.setCurrentTime(checkTime)

    allModels.performModelCheck()

    Mockito.verify(sensorModel).checkIfOfflineTransition(checkTime)
    Mockito.verify(channelModel1).checkIfOfflineTransition(checkTime)
    Mockito.verify(channelModel2).checkIfOfflineTransition(checkTime)
  }

  /**
   * Test that a simple sensor model happens and that the sensor is offline. Channels should not be checked.
   */
  @Test def testModelCheckSensorOffline(): Unit = {
    val channelModel1 = Mockito.mock(classOf[SensorChannelEntityModel])
    val channelModel2 = Mockito.mock(classOf[SensorChannelEntityModel])

    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensorDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)
    Mockito.when(sensorModel.getAllSensorChannelModels()).thenReturn(List(channelModel1, channelModel2))

    val checkTime = 12345l
    Mockito.when(sensorModel.checkIfOfflineTransition(checkTime)).thenReturn(true)

    val externalId = "foo"

    Mockito.when(sensorDescription.externalId).thenReturn(externalId)
    Mockito.when(sensorDescription.active).thenReturn(true)

    timeProvider.setCurrentTime(10000)

    allModels.registerSensorModel(sensorModel)

    timeProvider.setCurrentTime(checkTime)

    allModels.performModelCheck()

    Mockito.verify(sensorModel).checkIfOfflineTransition(checkTime)
    Mockito.verify(channelModel1, Mockito.times(0)).checkIfOfflineTransition(checkTime)
    Mockito.verify(channelModel2, Mockito.times(0)).checkIfOfflineTransition(checkTime)
  }
}