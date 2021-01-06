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

package io.smartspaces.sensor.services.processing

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.scope.ManagedScope
import io.smartspaces.sensor.domain.MarkerEntityDescription
import io.smartspaces.sensor.domain.PersonSensedEntityDescription
import io.smartspaces.sensor.services.domain.SensorInstanceRegistry
import io.smartspaces.sensor.messaging.messages.SensorMessages
import io.smartspaces.sensor.model.CompleteSensedEntityModel
import io.smartspaces.sensor.model.PersonSensedEntityModel
import io.smartspaces.sensor.model.PhysicalSpaceLocatableSensedEntityModel
import io.smartspaces.sensor.model.PhysicalSpaceSensedEntityModel
import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.model.updater.LocationChangeModelUpdater
import io.smartspaces.sensor.services.processing.value.SensorValueProcessorContext
import io.smartspaces.sensor.services.processing.value.SimpleMarkerSensorValueProcessor
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.sensor.model.SensorChannelEntityModel
import org.scalatest.junit.JUnitSuite

/**
 * A test for the simple marker sensor value processor.
 *
 * @author Keith M. Hughes
 */
class SimpleMarkerSensorValueProcessorTest extends JUnitSuite {

  var processor: SimpleMarkerSensorValueProcessor = _

  @Mock var completeSensedEntityModel: CompleteSensedEntityModel = _

  @Mock var unknownMarkerHandler: UnknownMarkerHandler = _

  @Mock var log: ExtendedLog = _

  @Mock var sensorRegistry: SensorInstanceRegistry = _
  
  @Mock var modelUpdater: LocationChangeModelUpdater = _
  
  @Mock var managedScope: ManagedScope = _

  var context: SensorValueProcessorContext = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    processor = new SimpleMarkerSensorValueProcessor(unknownMarkerHandler, modelUpdater)

    Mockito.when(completeSensedEntityModel.sensorRegistry).thenReturn(sensorRegistry)

    context = new SensorValueProcessorContext(completeSensedEntityModel, managedScope, log)
  }

  /**
   * Test that an unknown marker gets handled by the registered unknown marker
   * handler.
   */
  @Test def testUnknownMarker(): Unit = {
    val markerId = "foo"
    val measurementTimestamp: Long = 1000
    val sensorMessageReceivedTimestamp: Long = 1001
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, markerId)

    Mockito.when(sensorRegistry.getMarkerEntityByMarkerId(markerId)).thenReturn(None)

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])
    val sensedEntityModel = Mockito.mock(classOf[PhysicalSpaceLocatableSensedEntityModel])

    processor.processData(measurementTimestamp, sensorMessageReceivedTimestamp, sensorChannelModel, context, "channelId", builder.toDynamicObject())

    Mockito.verify(unknownMarkerHandler, Mockito.times(1)).handleUnknownMarker(markerId, measurementTimestamp)
  }

  /**
   * Test that a known marker gets handled properly.
   */
  @Test def testKnownMarker(): Unit = {
    val markerId = "foo"
    val measurementTimestamp: Long = 1000
    val sensorMessageReceivedTimestamp: Long = 1001
    val builder = new StandardDynamicObjectBuilder()

    builder.setProperty(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA_VALUE, markerId)

    val markerEntity = Mockito.mock(classOf[MarkerEntityDescription])
    Mockito.when(sensorRegistry.getMarkerEntityByMarkerId(markerId)).thenReturn(Some(markerEntity))

    val personDescription = Mockito.mock(classOf[PersonSensedEntityDescription])
    val personEntity = Mockito.mock(classOf[PersonSensedEntityModel])
    Mockito.when(personEntity.sensedEntityDescription).thenReturn(personDescription)
    Mockito.when(completeSensedEntityModel.getMarkedSensedEntityModelByMarkerId(markerId)).thenReturn(Some(personEntity))

    val sensorChannelModel = Mockito.mock(classOf[SensorChannelEntityModel])
    val sensorModel = Mockito.mock(classOf[SensorEntityModel])
    val sensedEntityModel = Mockito.mock(classOf[PhysicalSpaceSensedEntityModel])
    
    Mockito.when(sensorChannelModel.sensorModel).thenReturn(sensorModel)
    Mockito.when(sensorChannelModel.sensedEntityModel).thenReturn(sensedEntityModel)

    processor.processData(measurementTimestamp, sensorMessageReceivedTimestamp, sensorChannelModel, context, "channelId", builder.toDynamicObject())

    Mockito.verify(unknownMarkerHandler, Mockito.times(0)).handleUnknownMarker(markerId, measurementTimestamp)
    
    Mockito.verify(sensorChannelModel, Mockito.times(1)).stateUpdated(measurementTimestamp)
    Mockito.verify(sensorModel, Mockito.times(1)).stateUpdated(measurementTimestamp)
    
    modelUpdater.updateLocation(sensedEntityModel, personEntity, measurementTimestamp, sensorMessageReceivedTimestamp)
  }
}