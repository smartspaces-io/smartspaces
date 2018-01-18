/*
 * Copyright (C) 2017 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.smartspaces.sensor.event.UnknownEntitySeenEvent

/**
 * Tests for the standard unknown marker handler.
 *
 * @author Keith M. Hughes
 */
class StandardUnknownMarkerHandlerTest extends JUnitSuite {
  var handler: StandardUnknownMarkerHandler = null

  @Mock var eventEmitter: SensorProcessingEventEmitter = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    handler = new StandardUnknownMarkerHandler(eventEmitter)
  }

  /**
   * Ensure that is empty when starts
   */
  @Test def testEmptyWhenStarts(): Unit = {
    assertTrue(handler.getAllUnknownMarkerIds().isEmpty)
  }

  /**
   * Test that when adds, all added are all that are there.
   */
  @Test def testAdd(): Unit = {
    val argumentCaptor =
      ArgumentCaptor.forClass(classOf[UnknownEntitySeenEvent])

    val markerId1 = "foo1"
    val timestamp = 1000l
    handler.handleUnknownMarker(markerId1, timestamp)

    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastUnknownMarkerSeenEvent(argumentCaptor.capture())
    val event = argumentCaptor.getValue
    assertEquals(markerId1, event.entityId)
    assertEquals(timestamp, event.timestamp)

    val result = Set(markerId1)
    assertEquals(result, handler.getAllUnknownMarkerIds())
  }

  /**
   * Test that when adds, duplicates are not added.
   */
  @Test def testAddSame(): Unit = {
    val argumentCaptor =
      ArgumentCaptor.forClass(classOf[UnknownEntitySeenEvent])

    val markerId1 = "foo1"
    val timestamp = 1000l
    handler.handleUnknownMarker(markerId1, timestamp)
    handler.handleUnknownMarker(markerId1, 1001)

    Mockito.verify(eventEmitter, Mockito.times(1)).broadcastUnknownMarkerSeenEvent(argumentCaptor.capture())
    val event = argumentCaptor.getValue
    assertEquals(markerId1, event.entityId)
    assertEquals(timestamp, event.timestamp)

    val result = Set(markerId1)
    assertEquals(result, handler.getAllUnknownMarkerIds())
  }

  /**
   * Test that when multiple adds, all added are all that are there.
   */
  @Test def testAddMultiple(): Unit = {
    val argumentCaptor =
      ArgumentCaptor.forClass(classOf[UnknownEntitySeenEvent])

    val markerId1 = "foo1"
    val timestamp1 = 1000l
    handler.handleUnknownMarker(markerId1, timestamp1)

    val markerId2 = "foo2"
    val timestamp2 = 1001l
    handler.handleUnknownMarker(markerId2, timestamp2)

    Mockito.verify(eventEmitter, Mockito.times(2)).broadcastUnknownMarkerSeenEvent(argumentCaptor.capture())
    val events = argumentCaptor.getAllValues
    assertEquals(markerId1, events.get(0).entityId)
    assertEquals(timestamp1, events.get(0).timestamp)
    assertEquals(markerId2, events.get(1).entityId)
    assertEquals(timestamp2, events.get(1).timestamp)

    val result = Set(markerId1, markerId2)
    assertEquals(result, handler.getAllUnknownMarkerIds())
  }

  /**
   * Test that when add multiple, duplicates are not added.
   */
  @Test def testAddSameMultiple(): Unit = {
    val markerId1 = "foo1"
    handler.handleUnknownMarker(markerId1, 1000)
    handler.handleUnknownMarker(markerId1, 1001)

    val markerId2 = "foo2"
    handler.handleUnknownMarker(markerId2, 1002)

    val result = Set(markerId1, markerId2)
    assertEquals(result, handler.getAllUnknownMarkerIds())
  }

  /**
   * Test removing one of the markers.
   */
  @Test def testRemoval(): Unit = {
    val markerId1 = "foo1"
    handler.handleUnknownMarker(markerId1, 1000)

    val markerId2 = "foo2"
    handler.handleUnknownMarker(markerId2, 1001)

    handler.removeUnknownMarkerId(markerId1)

    val result = Set(markerId2)
    assertEquals(result, handler.getAllUnknownMarkerIds())
  }
}
