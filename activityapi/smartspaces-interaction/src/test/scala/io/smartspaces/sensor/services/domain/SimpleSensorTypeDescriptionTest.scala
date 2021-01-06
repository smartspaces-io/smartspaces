/*
 * Copyright (C) 2020 Keith M. Hughes
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

package io.smartspaces.sensor.services.domain

import io.smartspaces.sensor.domain.SensorChannelDetailDescription
import io.smartspaces.sensor.domain.SensorDescriptionConstants
import io.smartspaces.sensor.domain.SensorTypeDescription
import io.smartspaces.sensor.domain.SimpleSensorTypeDescription
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.scalatest.junit.JUnitSuite

/**
 * A set of tests for the simple sensor type description.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorTypeDescriptionTest {
  @Test def testSupportedChannels(): Unit = {
    val sourceSensorChannelFoo = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelFoo.channelId).thenReturn("foo")

    val sourceSensorChannelBar = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelBar.channelId).thenReturn("bar")

    val sourceSensorChannelBletch = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelBletch.channelId).thenReturn("bletch")

    val sourceSensorChannelSpam = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelSpam.channelId).thenReturn("spam")

    val channels = Set(sourceSensorChannelFoo, sourceSensorChannelBar, sourceSensorChannelBletch, sourceSensorChannelSpam)

    val sensorType = SimpleSensorTypeDescription(
      "id", "sensorTypeId", "name", None, None, None, None, List(), None, None,
      "-bar:bletch", channels)

    assertEquals(Set("foo", "spam"), sensorType.expandedSupportedChannelIds.toSet)

    assertEquals(Set(sourceSensorChannelFoo, sourceSensorChannelSpam), sensorType.supportedChannelDetails.toSet)

    assertEquals(Some(sourceSensorChannelFoo), sensorType.getSupportedSensorChannelDetail("foo"))
    assertEquals(Some(sourceSensorChannelSpam), sensorType.getSupportedSensorChannelDetail("spam"))
    assertEquals(None, sensorType.getSupportedSensorChannelDetail("bar"))
    assertEquals(None, sensorType.getSupportedSensorChannelDetail("bletch"))

    assertEquals(channels, sensorType.allChannelDetails.toSet)

    assertEquals(Some(sourceSensorChannelFoo), sensorType.getSensorChannelDetail("foo"))
    assertEquals(Some(sourceSensorChannelSpam), sensorType.getSensorChannelDetail("spam"))
    assertEquals(Some(sourceSensorChannelBar), sensorType.getSensorChannelDetail("bar"))
    assertEquals(Some(sourceSensorChannelBletch), sensorType.getSensorChannelDetail("bletch"))
  }
}
