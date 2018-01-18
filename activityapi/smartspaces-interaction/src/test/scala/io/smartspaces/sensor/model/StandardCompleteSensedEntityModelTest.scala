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

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.smartspaces.event.observable.EventObservableRegistry
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.services.domain.SensorInstanceRegistry
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.sensor.services.processing.SensorProcessingEventEmitter
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.time.provider.SettableTimeProvider

/**
 * Test the {@link #StandardCompleteSensedEntityModel}.
 *
 * @author Keith M. Hughes
 */
class StandardCompleteSensedEntityModelTest extends JUnitSuite {
  var allModels: StandardCompleteSensedEntityModel = null

  @Mock var sensorRegistry: SensorInstanceRegistry = null

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

  /**
   * Test that a simple sensor model happens and that no event is emitted.
   */
  @Test def testModelCheckNoEvent(): Unit = {
    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensorDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)

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
    
    
    val argumentCaptor = ArgumentCaptor.forClass(classOf[SensorOfflineEvent])
    Mockito.verify(eventEmitter, Mockito.times(0)).broadcastSensorOfflineEvent(argumentCaptor.capture())
  }

  /**
   * Test that a simple sensor model happens and that an event is emitted when the sensor goes offline
   */
  @Test def testModelCheckWithEvent(): Unit = {
    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensorDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)

    val checkTime = 12345l
    Mockito.when(sensorModel.checkIfOfflineTransition(checkTime)).thenReturn(true)

    val externalId = "foo"

    Mockito.when(sensorDescription.externalId).thenReturn(externalId)
    Mockito.when(sensorDescription.active).thenReturn(true)

    val offlineTime = 10000l
    timeProvider.setCurrentTime(offlineTime)

    allModels.registerSensorModel(sensorModel)

    timeProvider.setCurrentTime(checkTime)

    allModels.performModelCheck()

    Mockito.verify(sensorModel).checkIfOfflineTransition(checkTime)
    
    val argumentCaptor = ArgumentCaptor.forClass(classOf[SensorOfflineEvent])
    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastSensorOfflineEvent(argumentCaptor.capture())

    val event = argumentCaptor.getValue
    Assert.assertEquals(sensorModel, event.sensorModel)
    Assert.assertEquals(checkTime, event.timestamp)
  }
}