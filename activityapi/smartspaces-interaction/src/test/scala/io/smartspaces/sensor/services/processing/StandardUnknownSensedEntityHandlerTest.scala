/*
 * Copyright (C) 2016 Keith M. Hughes
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
import org.scalatest.junit.JUnitSuite

/**
 * Tests for {@link StandardUnknownSensedEntityHandler}.
 *
 * @author Keith M. Hughes
 */
class StandardUnknownSensedEntityHandlerTest extends JUnitSuite {
  var handler: StandardUnknownSensedEntityHandler = null

  @Before def setup(): Unit = {
    handler = new StandardUnknownSensedEntityHandler()
  }

  /**
   * Ensure that is empty when starts
   */
  @Test def testEmptyWhenStarts(): Unit = {
    assertTrue(handler.getAllUnknownSensorIds().isEmpty)
  }

  /**
   * Test that when adds, all added are all that are there.
   */
  @Test def testAdd(): Unit = {
    val sensorId1 = "foo1"
    handler.handleUnknownSensor(sensorId1)

    val sensorId2 = "foo2"
    handler.handleUnknownSensor(sensorId2)

    val result = Set(sensorId1, sensorId2)
    assertEquals(result, handler.getAllUnknownSensorIds())
  }

  /**
   * Test that when adds, duplicates are not added..
   */
  @Test def testAddSame(): Unit = {
    val sensorId1 = "foo1"
    handler.handleUnknownSensor(sensorId1)
    handler.handleUnknownSensor(sensorId1)

    val sensorId2 = "foo2"
    handler.handleUnknownSensor(sensorId2)

    val result = Set(sensorId1, sensorId2)
    assertEquals(result, handler.getAllUnknownSensorIds())
  }

  /**
   * Test removing one of the sensors.
   */
  @Test def testRemoval(): Unit = {
    val sensorId1 = "foo1"
    handler.handleUnknownSensor(sensorId1)

    val sensorId2 = "foo2"
    handler.handleUnknownSensor(sensorId2)

    handler.removeUnknownSensorId(sensorId1)

    val result = Set(sensorId2)
    assertEquals(result, handler.getAllUnknownSensorIds())
  }
}
