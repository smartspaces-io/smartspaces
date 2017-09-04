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

/**
 * Tests for the {@link StandardSensedEntityModelProcessor}.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelProcessorTest extends JUnitSuite {

  var processor: StandardSensedEntityModelProcessor = _

  @Mock var completeSensedEntityModel: CompleteSensedEntityModel = _

  @Mock var log: ExtendedLog = _

  @Mock var handler: SensedEntitySensorHandler = _
  
  @Mock var managedScope: ManagedScope = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    processor = new StandardSensedEntityModelProcessor(completeSensedEntityModel, managedScope, log)
  }

  /**
   * Test using the sensor value processor when can't find the sensed entity
   * model.
   */
  @Test def testModelNoValueUpdate(): Unit = {
    val sensorValueProcessor: SensorValueProcessor = Mockito.mock(classOf[SensorValueProcessor])
    val sensorValueType = "sensor.type"
    Mockito.when(sensorValueProcessor.sensorValueType).thenReturn(sensorValueType)

    processor.addSensorValueProcessor(sensorValueProcessor)

    val builder = new StandardDynamicObjectBuilder()

    builder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    val data = builder.toDynamicObject()

    val timestamp: Long = 10000
    val sensorDetail = new SimpleSensorDetail("1", "foo", "foo", "foo", None, None)
    val channelDetail =
      new SimpleSensorChannelDetail(sensorDetail, "test", "glorp", "norp", null, null)
    sensorDetail.addSensorChannelDetail(channelDetail)

    val sensor =
      new SimpleSensorEntityDescription("1", "foo", "foo", "foo", Option.apply(sensorDetail), None, None)
    val sensorModel = new SimpleSensorEntityModel(sensor, completeSensedEntityModel, 0)

    val sensedEntity =
      new SimplePhysicalSpaceSensedEntityDescription("2", "foo", "foo", "foo", None)
    val sensedEntityModel =
      new SimpleSensedEntityModel(sensedEntity, completeSensedEntityModel)

    processor.handleNewSensorData(handler, timestamp, sensorModel, sensedEntityModel, data)

    Mockito.verify(sensorValueProcessor, Mockito.never()).processData(Matchers.anyLong(),
      Matchers.any(classOf[SensorEntityModel]), Matchers.any(classOf[SensedEntityModel]),
      Matchers.any(classOf[SensorValueProcessorContext]), Matchers.any(classOf[String]), Matchers.any(classOf[DynamicObject]))
  }

  /**
   * Test using the sensor value processor.
   */
  @Test def testModelValueUpdate(): Unit = {
    val sensorValueProcessor = Mockito.mock(classOf[SensorValueProcessor])
    val sensorValueType = "sensor.type"
    Mockito.when(sensorValueProcessor.sensorValueType).thenReturn(sensorValueType)

    processor.addSensorValueProcessor(sensorValueProcessor)

    val measurementType =
      new SimpleMeasurementTypeDescription("foo", sensorValueType, null, null, null, null, null)

    val builder = new StandardDynamicObjectBuilder()

    builder.newObject(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)
    val channelId = "test"
    builder.newObject(channelId)
    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_TYPE, sensorValueType)

    val timestamp: Long = 10000
    val sensorDetail = new SimpleSensorDetail("1", "foo", "foo", "foo", None, None)
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

    Mockito.when(completeSensedEntityModel.getSensedEntityModelByExternalId(sensedEntity.externalId))
      .thenReturn(Option(sensedEntityModel))

    val data = builder.toDynamicObject()
    processor.handleNewSensorData(handler, timestamp, sensorModel, sensedEntityModel, data)

    Mockito.verify(sensorValueProcessor, Mockito.times(1)).processData(timestamp, sensorModel,
      sensedEntityModel, processor.processorContext, channelDetail.id, data)
  }
}
