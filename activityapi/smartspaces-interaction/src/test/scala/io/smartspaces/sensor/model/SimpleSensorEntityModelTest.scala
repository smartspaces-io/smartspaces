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

import io.smartspaces.sensor.domain.MeasurementTypeDescription
import io.smartspaces.sensor.domain.SensorDetailDescription
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.sensor.services.processing.SensorProcessingEventEmitter

/**
 * Tests for the SimpleSensorEntityModel.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityModelTest extends JUnitSuite {
  var model: SimpleSensorEntityModel = null

  @Mock var sensorEntityDescription: SensorEntityDescription = null

  @Mock var allModels: CompleteSensedEntityModel = null
  
  @Mock var eventEmitter: SensorProcessingEventEmitter = null

  @Mock var sensorDetail: SensorDetailDescription = null

  val modelCreationTime = 123456l

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
   
    Mockito.when(allModels.eventEmitter).thenReturn(eventEmitter)

    Mockito.when(sensorEntityDescription.sensorDetail).thenReturn(Option(sensorDetail))

    model = new SimpleSensorEntityModel(sensorEntityDescription, allModels, modelCreationTime)
  }

  /**
   * Test to see if offline happens when there has been no update time.
   */
  @Test def testOfflineFromInitialUpdateTimeout(): Unit = {
    val timeoutTime = 1000l

    Mockito.when(sensorEntityDescription.sensorStateUpdateTimeLimit).thenReturn(Option(timeoutTime))
    Mockito.when(sensorEntityDescription.sensorHeartbeatUpdateTimeLimit).thenReturn(None)

    //The model starts offline until proven online
    Assert.assertFalse(model.online)

//    val argumentCaptor =
//      ArgumentCaptor.forClass(classOf[SensorOfflineEvent])

    // Model didn't change from short change
    val transition1 = model.checkIfOfflineTransition(modelCreationTime + timeoutTime / 2)
    Assert.assertFalse(transition1)
    Assert.assertFalse(model.online)
    //Mockito.verify(eventEmitter, Mockito.times(0)).broadcastSensorOfflineEvent(argumentCaptor.capture())

    // Now trigger it from no signal from model start.
    val offlineTime = modelCreationTime + timeoutTime + 1
    var transition2 = model.checkIfOfflineTransition(offlineTime)
    Assert.assertTrue(transition2)
    Assert.assertFalse(model.online)
  }

  /**
   * Test a model update and confirm that it makes the sensor model online, and properly
   * records the value and when it happened.
   *
   */
  @Test def testValueUpdate(): Unit = {
    val measurementTypeDescriptionExternalId = "glorp"
    val measurementTypeDescription = Mockito.mock(classOf[MeasurementTypeDescription])
    Mockito.when(measurementTypeDescription.externalId).thenReturn(measurementTypeDescriptionExternalId)

    val value = Mockito.mock(classOf[SensedValue[Double]])
    Mockito.when(value.measurementTypeDescription).thenReturn(measurementTypeDescription)

    val currentTime = 10000l

    model.setOnline(false)

    Assert.assertTrue(model.lastUpdateTime().isEmpty)

    model.updateSensedValue(value, currentTime)

    Assert.assertEquals(currentTime, model.lastUpdateTime().get)
    Assert.assertEquals(List(value), model.getAllSensedValues())
    Assert.assertTrue(model.online)
  }
}