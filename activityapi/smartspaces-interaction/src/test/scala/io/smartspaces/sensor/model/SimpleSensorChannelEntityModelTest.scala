/*
 * Copyright (C) 2018 Keith M. Hughes
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
import io.smartspaces.sensor.domain.SensorTypeDescription
import io.smartspaces.sensor.domain.SensorEntityDescription
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.sensor.services.processing.SensorProcessingEventEmitter
import io.smartspaces.sensor.event.SensorOnlineEvent
import io.smartspaces.sensor.domain.SensedEntityDescription
import io.smartspaces.sensor.domain.SensorChannelDetailDescription
import io.smartspaces.sensor.services.processing.value.SensorValueProcessor
import io.smartspaces.sensor.event.SensorChannelOnlineEvent
import io.smartspaces.sensor.event.SensorChannelOfflineEvent

/**
 * Tests for the SimpleSensorChannelEntityModel.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorChannelEntityModelTest extends JUnitSuite {
  var model: SimpleSensorChannelEntityModel = _

  @Mock var sensorChannelDetail: SensorChannelDetailDescription = _
  @Mock var sensorEntityModel: SensorEntityModel = _
  @Mock var sensorEntityDescription: SensorEntityDescription = _

  @Mock var sensedEntityModel: SensedEntityModel = _
  @Mock var sensedEntityDescription: SensedEntityDescription = _

  @Mock var sensorValueProcessor: SensorValueProcessor = _

  @Mock var allModels: CompleteSensedEntityModel = _

  @Mock var eventEmitter: SensorProcessingEventEmitter = _

  @Mock var sensorDetail: SensorTypeDescription = _

  val modelCreationTime = 123456l

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    Mockito.when(allModels.eventEmitter).thenReturn(eventEmitter)

    Mockito.when(sensorEntityDescription.sensorType).thenReturn(sensorDetail)

    model = new SimpleSensorChannelEntityModel(
      sensorChannelDetail, sensorEntityModel, sensedEntityModel, sensorValueProcessor, allModels, modelCreationTime)
  }

  /**
   * Test to see if offline happens when there has been no update time.
   */
  @Test def testOfflineFromInitialUpdateTimeout(): Unit = {
    val timeoutTime = 1000l

    Mockito.when(sensorChannelDetail.stateUpdateTimeLimit).thenReturn(Some(timeoutTime))
    Mockito.when(sensorChannelDetail.heartbeatUpdateTimeLimit).thenReturn(None)

    //The model starts offline until proven online
    Assert.assertFalse(model.online)

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

    val argumentCaptor = ArgumentCaptor.forClass(classOf[SensorChannelOfflineEvent])
    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastSensorChannelOfflineEvent(argumentCaptor.capture())
    Assert.assertEquals(model, argumentCaptor.getValue.sensorChannelModel)
    Assert.assertEquals(offlineTime, argumentCaptor.getValue.timestampOffline)
  }

  /**
   * Test a model update and confirm that it makes the sensor model online, and properly
   * records the value and when it happened.
   *
   */
  @Test def testValueUpdate(): Unit = {
    val value = Mockito.mock(classOf[SensedValue[Double]])

    val timestampCurrent = 10000l

    model.setOnline(false)
    model.setOfflineSignaled(true)

    Assert.assertTrue(model.timestampLastStateUpdate.isEmpty)

    model.updateSensedValue(value, timestampCurrent)

    Assert.assertEquals(timestampCurrent, model.timestampLastStateUpdate.get)
    Assert.assertTrue(model.online)

    val onlineEventArgumentCaptor = ArgumentCaptor.forClass(classOf[SensorChannelOnlineEvent])
    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastSensorChannelOnlineEvent(onlineEventArgumentCaptor.capture())
    Assert.assertEquals(model, onlineEventArgumentCaptor.getValue.sensorChannelModel)
    Assert.assertEquals(timestampCurrent, onlineEventArgumentCaptor.getValue.timestampOnline)

    Mockito.verify(sensorEntityModel, Mockito.times(1)).updateSensedValue(value, timestampCurrent)
    Mockito.verify(sensedEntityModel, Mockito.times(1)).updateSensedValue(value, timestampCurrent)
  }
}
