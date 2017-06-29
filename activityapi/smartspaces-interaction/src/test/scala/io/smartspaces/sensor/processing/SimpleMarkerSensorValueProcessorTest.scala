/*
 * Copyright (C) 2017 Keith M. Hughes
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
import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder

import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite
import io.smartspaces.sensor.messages.SensorMessages
import io.smartspaces.sensor.entity.model.PhysicalSpaceLocatableSensedEntityModel
import io.smartspaces.sensor.entity.SensorRegistry
import io.smartspaces.sensor.entity.MarkerEntityDescription
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.entity.PersonSensedEntityDescription
import io.smartspaces.sensor.entity.model.updater.LocationChangeModelUpdater
import io.smartspaces.sensor.processing.value.SimpleMarkerSensorValueProcessor

/**
 * A test for the simple marker sensor value processor.
 *
 * @author Keith M. Hughes
 */
class SimpleMarkerSensorValueProcessorTest {

  var processor: SimpleMarkerSensorValueProcessor = _

  @Mock var completeSensedEntityModel: CompleteSensedEntityModel = _

  @Mock var unknownMarkerHandler: UnknownMarkerHandler = _

  @Mock var log: ExtendedLog = _

  @Mock var sensorRegistry: SensorRegistry = _
  
  @Mock var modelUpdater: LocationChangeModelUpdater = _

  var context: SensorValueProcessorContext = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    processor = new SimpleMarkerSensorValueProcessor(unknownMarkerHandler, modelUpdater)

    Mockito.when(completeSensedEntityModel.sensorRegistry).thenReturn(sensorRegistry)

    context = new SensorValueProcessorContext(completeSensedEntityModel, log: ExtendedLog)
  }

  /**
   * Test that an unknown marker gets handled by the registered unknown marker
   * handler.
   */
  @Test def testUnknownMarker(): Unit = {
    val markerId = "foo"
    val timestamp: Long = 1000
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, markerId)

    Mockito.when(sensorRegistry.getMarkerEntityByMarkerId(markerId)).thenReturn(None)

    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensedEntityModel = Mockito.mock(classOf[PhysicalSpaceLocatableSensedEntityModel])

    processor.processData(timestamp, sensorModel, sensedEntityModel, context, builder.toDynamicObject())

    Mockito.verify(unknownMarkerHandler, Mockito.times(1)).handleUnknownMarker(markerId, timestamp)
  }

  /**
   * Test that a known marker gets handled properly.
   */
  @Test def testKnownMarker(): Unit = {
    val markerId = "foo"
    val timestamp: Long = 1000
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, markerId)

    val markerEntity = Mockito.mock(classOf[MarkerEntityDescription])
    Mockito.when(sensorRegistry.getMarkerEntityByMarkerId(markerId)).thenReturn(Some(markerEntity))

    val personDescription = Mockito.mock(classOf[PersonSensedEntityDescription])
    val personEntity = Mockito.mock(classOf[PersonSensedEntityModel])
    Mockito.when(personEntity.sensedEntityDescription).thenReturn(personDescription)
    Mockito.when(completeSensedEntityModel.getMarkedSensedEntityModelByMarkerId(markerId)).thenReturn(Some(personEntity))

    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensedEntityModel = Mockito.mock(classOf[PhysicalSpaceSensedEntityModel])

    processor.processData(timestamp, sensorModel, sensedEntityModel, context, builder.toDynamicObject())

    Mockito.verify(unknownMarkerHandler, Mockito.times(0)).handleUnknownMarker(markerId, timestamp)
    
    Mockito.verify(sensorModel, Mockito.times(1)).updateSensedValue(timestamp)
    
    modelUpdater.updateLocation(sensedEntityModel, personEntity, timestamp)
  }
}