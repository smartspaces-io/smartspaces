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
import io.smartspaces.sensor.entity.SimpleMeasurementTypeDescription
import io.smartspaces.sensor.entity.SimplePhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.entity.SimpleSensorChannelDetail
import io.smartspaces.sensor.entity.SimpleSensorDetail
import io.smartspaces.sensor.entity.SimpleSensorEntityDescription
import io.smartspaces.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sensor.entity.model.SensedEntityModel
import io.smartspaces.sensor.entity.model.SensorEntityModel
import io.smartspaces.sensor.entity.model.SimpleSensedEntityModel
import io.smartspaces.sensor.entity.model.SimpleSensorEntityModel
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.processing.value.SensorValueProcessor
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite
import io.smartspaces.scope.ManagedScope
import io.smartspaces.sensor.processing.value.SensorValueProcessorContext
import io.smartspaces.sensor.processing.value.StandardSensorValueProcessorRegistry
import io.smartspaces.sensor.entity.model.SimpleSensorChannelEntityModel
import io.smartspaces.sensor.messaging.output.StandardSensorMessageBuilder
import io.smartspaces.sensor.entity.SensorEntityDescription

/**
 * Tests for the {@link StandardSensedEntityModelProcessor}.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelProcessorTest extends JUnitSuite {

  var processor: StandardSensedEntityModelProcessor = _
  
  var sensorValueProcessorRegistry: StandardSensorValueProcessorRegistry = _

  @Mock var completeSensedEntityModel: CompleteSensedEntityModel = _

  @Mock var log: ExtendedLog = _

  @Mock var handler: SensedEntitySensorHandler = _
  
  @Mock var managedScope: ManagedScope = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    sensorValueProcessorRegistry = new StandardSensorValueProcessorRegistry(log)
    processor = new StandardSensedEntityModelProcessor(sensorValueProcessorRegistry, completeSensedEntityModel, managedScope, log)
  }

  /**
   * Test using the sensor value processor for a heartbeat message.
   */
  @Test def testHeartbeat(): Unit = {
    val sensorDescription: SensorEntityDescription = Mockito.mock(classOf[SensorEntityDescription])
    Mockito.when(sensorDescription.externalId).thenReturn("1")
    val sensorModel: SensorEntityModel = Mockito.mock(classOf[SensorEntityModel])
    Mockito.when(sensorModel.sensorEntityDescription).thenReturn(sensorDescription)

    val builder = StandardSensorMessageBuilder.newHeartbeatMessage("1")

    val data = builder.messageBuilder.toDynamicObject()

    val timestamp: Long = 10000

    processor.handleNewSensorMessage(handler, timestamp, sensorModel, data)
    
    Mockito.verify(sensorModel).updateHeartbeat(timestamp)
  }

  /**
   * Test using the sensor value processor.
   */
  @Test def testModelValueUpdate(): Unit = {
    val sensorValueProcessor = Mockito.mock(classOf[SensorValueProcessor])
    val sensorValueType = "sensor.type"
    Mockito.when(sensorValueProcessor.sensorValueType).thenReturn(sensorValueType)
    val sensorValue: Long = 1234

    sensorValueProcessorRegistry.addSensorValueProcessor(sensorValueProcessor)
    
    val measurementTimestamp: Long = 1000
    val sensorMessageReceivedTimestamp: Long = 1001

    val measurementType =
      new SimpleMeasurementTypeDescription("foo", sensorValueType, null, null, null, null, null)


    val sensorDetail = new SimpleSensorDetail("1", "foo", "foo", "foo", None, None)
    val channelId = "test"
    val channelDetail =
      new SimpleSensorChannelDetail(sensorDetail, channelId, "glorp", "norp", measurementType, null)
    sensorDetail.addSensorChannelDetail(channelDetail)

    val sensor =
      new SimpleSensorEntityDescription("2", "foo", "foo", "foo", Option(sensorDetail), None, None)
    val sensorModel = new SimpleSensorEntityModel(sensor, completeSensedEntityModel, 0)

    val sensedEntity =
      new SimplePhysicalSpaceSensedEntityDescription("1", "foo", "foo", "foo", None)
    val sensedEntityModel =
      new SimpleSensedEntityModel(sensedEntity, completeSensedEntityModel)

    val sensorChannelModel = new SimpleSensorChannelEntityModel(sensorModel, channelDetail, sensedEntityModel)
    sensorModel.addSensorChannelEntityModel(sensorChannelModel)

    val builder = StandardSensorMessageBuilder.newMeasurementMessage(sensor.externalId)

    builder.addChannelData(channelId, sensorValueType, sensorValue, measurementTimestamp)

    val data = builder.messageBuilder.toDynamicObject()
    processor.handleNewSensorMessage(handler, sensorMessageReceivedTimestamp, sensorModel, data)

    Mockito.verify(sensorValueProcessor, Mockito.times(1)).processData(
        measurementTimestamp, sensorMessageReceivedTimestamp, sensorModel,
        sensedEntityModel, processor.processorContext, channelDetail.channelId, data)
  }
}
