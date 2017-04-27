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
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.sensor.messages.SensorMessages

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

  @Mock var sensedEntitySensorListener: SensedEntitySensorListener = null

  @Mock var log: ExtendedLog = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    handler = new StandardSensedEntitySensorHandler(allModels, unknownSensedEntityHandler, log)
    handler.sensorProcessor = sensorProcessor
    handler.addSensedEntitySensorListener(sensedEntitySensorListener)
  }

  /**
   * Test that an unknown sensor gets handled by the registered unknown sensor
   * handler.
   */
  @Test def testUnknownSensor(): Unit = {
    val sensorId = "foo"
    val timestamp: Long = 1000
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR, sensorId)

    handler.handleSensorData(timestamp, builder.toDynamicObject())

    Mockito.verify(unknownSensedEntityHandler, Mockito.times(1)).handleUnknownSensor(sensorId)
  }

  /**
   * Test that a known sensor calls the listener properly.
   */
  @Test def testKnownSensor(): Unit = {
    val sensorId = "foo"
    val sensor = new SimpleSensorEntityDescription("1", sensorId, "foo", "foo", null, None, None)
    val sensorModel = new SimpleSensorEntityModel(sensor, allModels, 0)

    val sensedEntity =
      new SimplePhysicalSpaceSensedEntityDescription("2", "foo", "foo", "foo")
    val sensedEntityModel = new SimpleSensedEntityModel(sensedEntity, allModels)

    Mockito.when(allModels.getSensorEntityModelByExternalId(sensor.externalId)).thenReturn(Option(sensorModel))
    Mockito.when(allModels.getSensedEntityModelByExternalId(sensedEntity.externalId)).thenReturn(Option(sensedEntityModel))
    handler.associateSensorWithEntity(sensor, sensedEntity)

    val timestamp: Long = 1000
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR, sensorId)

    val data = builder.toDynamicObject()
    handler.handleSensorData(timestamp, data)

    Mockito.verify(unknownSensedEntityHandler, Mockito.times(0)).handleUnknownSensor(sensorId)

    // TODO(keith): Determine a refactoring so that the listener calls can be checked.
    Mockito.verify(allModels, Mockito.times(1)).doVoidWriteTransaction(Matchers.any())
    //    Mockito.verify(sensedEntitySensorListener, Mockito.times(1)).handleSensorData(handler,
    //        timestamp, sensorModel, sensedEntityModel, data)
  }

}
