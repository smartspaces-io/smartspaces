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

package io.smartspaces.sensor.processing

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.entity.SimplePhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.entity.SimpleSensorEntityDescription
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SimpleSensedEntityModel
import io.smartspaces.sensor.entity.model.SimpleSensorEntityModel
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite
import io.smartspaces.sensor.messaging.output.StandardSensorMessageBuilder
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.SensorEntityDescription
import io.smartspaces.sensor.messaging.output.StandardCompositeSensorMessageBuilder

/**
 * Tests for the {@link StandardSensedEntitySensorHandler}.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntitySensorHandlerTest extends JUnitSuite {

  var handler: StandardSensedEntitySensorHandler = null

  @Mock var allModels: CompleteSensedEntityModel = null

  @Mock var sensorProcessor: SensorProcessor = null

  @Mock var unknownSensedEntityHandler: UnknownSensedEntityHandler = null

  @Mock var sensedEntitySensorListener: SensedEntitySensorMessageHandler = null

  @Mock var log: ExtendedLog = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    handler = new StandardSensedEntitySensorHandler(allModels, unknownSensedEntityHandler, log)
    handler.sensorProcessor = sensorProcessor
    handler.addSensedEntitySensorMessageHandler(sensedEntitySensorListener)
  }

  /**
   * Test that an unknown sensor gets handled by the registered unknown sensor
   * handler.
   */
  @Test def testUnknownSensor(): Unit = {
    val sensorId = "foo"
    val timestamp: Long = 1000

    Mockito.when(allModels.getSensorEntityModelByExternalId(sensorId)).thenReturn(None)

    val builder = StandardSensorMessageBuilder.newMeasurementMessage(sensorId)

    handler.handleSensorMessage(timestamp, builder.messageBuilder.toDynamicObject())

    Mockito.verify(unknownSensedEntityHandler, Mockito.times(1)).handleUnknownSensor(sensorId)
  }

  /**
   * Test that a known sensor calls the listener properly for a single message.
   */
  @Test def testKnownSensorSingleMessage(): Unit = {
    val sensorId = "foo"
    val timestamp: Long = 1000

    val sensorDescription: SensorEntityDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorDescription.active).thenReturn(true)
    
    val sensorModel: SensorEntityModel = Mockito.mock(classOf[SensorEntityModel])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)
    
    Mockito.when(allModels.getSensorEntityModelByExternalId(sensorId)).thenReturn(Some(sensorModel))

    val builder = StandardSensorMessageBuilder.newMeasurementMessage(sensorId)

    handler.handleSensorMessage(timestamp, builder.messageBuilder.toDynamicObject())

    Mockito.verify(unknownSensedEntityHandler, Mockito.times(0)).handleUnknownSensor(sensorId)

    // TODO(keith): Determine a refactoring so that the listener calls can be checked.
    Mockito.verify(allModels, Mockito.times(1)).doVoidWriteTransaction(Matchers.any())
    //    Mockito.verify(sensedEntitySensorListener, Mockito.times(1)).handleSensorData(handler,
    //        timestamp, sensorModel, sensedEntityModel, data)
  }

  /**
   * Test that a known sensor calls the listener properly for a composite message.
   */
  @Test def testKnownSensorCompositeMessage(): Unit = {
    val timestamp: Long = 1000

    val sensorId1 = "foo1"
    val sensorDescription1: SensorEntityDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorDescription1.active).thenReturn(true)
    
    val sensorModel1: SensorEntityModel = Mockito.mock(classOf[SensorEntityModel])
    Mockito.when(sensorModel1.sensorEntityDescription).thenReturn(sensorDescription1)
    
    Mockito.when(allModels.getSensorEntityModelByExternalId(sensorId1)).thenReturn(Some(sensorModel1))

    val sensorId2 = "foo2"
    val sensorDescription2: SensorEntityDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorDescription2.active).thenReturn(true)
    
    val sensorModel2: SensorEntityModel = Mockito.mock(classOf[SensorEntityModel])
    Mockito.when(sensorModel2.sensorEntityDescription).thenReturn(sensorDescription2)
    
    Mockito.when(allModels.getSensorEntityModelByExternalId(sensorId2)).thenReturn(Some(sensorModel2))

    val builder = StandardCompositeSensorMessageBuilder.newCompositeMessage()
    builder.newMeasurementMessage(sensorId1)
    builder.newMeasurementMessage(sensorId2)

    handler.handleSensorMessage(timestamp, builder.messageBuilder.toDynamicObject())

    Mockito.verify(unknownSensedEntityHandler, Mockito.times(0)).handleUnknownSensor(sensorId1)

    // TODO(keith): Determine a refactoring so that the listener calls can be checked.
    Mockito.verify(allModels, Mockito.times(2)).doVoidWriteTransaction(Matchers.any())
    //    Mockito.verify(sensedEntitySensorListener, Mockito.times(1)).handleSensorData(handler,
    //        timestamp, sensorModel, sensedEntityModel, data)
  }

}
