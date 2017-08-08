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

package io.smartspaces.sensor.entity.model

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.entity.PhysicalSpaceSensedEntityDescription
import io.smartspaces.sensor.entity.model.event.PhysicalSpaceOccupancyLiveEvent
import io.smartspaces.sensor.processing.SensorProcessingEventEmitter

/**
 * Tests for the {@link SimplePhysicalSpaceSensedEntityModel}
 *
 * @author Keith M. Hughes
 */
class SimplePhysicalSpaceSensedEntityModelTest extends JUnitSuite {

  var model: SimplePhysicalSpaceSensedEntityModel = null

  @Mock var entityDescription: PhysicalSpaceSensedEntityDescription = null

  @Mock var completeSensedEntityModel: CompleteSensedEntityModel = null
  
  @Mock var eventEmitter: SensorProcessingEventEmitter = null

  @Mock var log: ExtendedLog = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    
    Mockito.when(completeSensedEntityModel.eventEmitter).thenReturn(eventEmitter)

    model = new SimplePhysicalSpaceSensedEntityModel(entityDescription, completeSensedEntityModel)
  }

  /**
   * Test that an event comes out of a single person entering a room even though
   * the model update happens twice.
   */
  @Test def testSinglePersonEnters(): Unit = {
    val personModel = Mockito.mock(classOf[PersonSensedEntityModel])

    model.occupantEntered(personModel, 100)
    model.occupantEntered(personModel, 100)

    val argumentCaptor =
      ArgumentCaptor.forClass(classOf[PhysicalSpaceOccupancyLiveEvent])

    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastOccupancyEvent(argumentCaptor.capture())

    val peopleEntered = Set(personModel)
    Assert.assertEquals(peopleEntered, argumentCaptor.getValue().entered)
    Assert.assertEquals(peopleEntered, model.getOccupants())
  }

  /**
   * st
   * Test that an event comes out of a single person entering a room and then
   * leaving twice.
   */
  @Test def testSinglePersonEntersAndExits(): Unit = {
    val personModel = Mockito.mock(classOf[PersonSensedEntityModel])

    model.occupantEntered(personModel, 100)
    model.occupantExited(personModel, 100)
    model.occupantExited(personModel, 100)

    val argumentCaptor =
      ArgumentCaptor.forClass(classOf[PhysicalSpaceOccupancyLiveEvent])

    // First call will be person entering.
    Mockito.verify(eventEmitter, Mockito.times(2)).broadcastOccupancyEvent(argumentCaptor.capture())

    val peopleEntered = Set(personModel)

    // The last event will be captured.
    val allValues = argumentCaptor.getAllValues()
    Assert.assertEquals(peopleEntered, allValues.get(1).exited)
    Assert.assertTrue(model.getOccupants().isEmpty)
  }
}
