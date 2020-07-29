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
import io.smartspaces.sensor.domain.SensorTypeDescription
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.scalatest.junit.JUnitSuite

/**
 * A set of tests for the entity description support.
 *
 * @author Keith M. Hughes
 */
class EntityDescriptionSupportTests extends JUnitSuite {

  @Test def testCheckIdFullExpansion(): Unit = {
    val sourceSensorChannelIds = Set("foo", "bar", "bletch", "spam")
    val result = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "*")

    assertEquals(sourceSensorChannelIds, result)
  }

  @Test def testCheckIdPartialFilter(): Unit = {
    val sourceSensorChannelIds = Set("foo", "bar", "bletch", "spam")

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "bar")
    assertEquals(Set("bar"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "bar:bletch")
    assertEquals(Set("bar", "bletch"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "bar:bletch:glorp")
    assertEquals(Set("bar", "bletch"), result3.toSet)
  }

  @Test def testCheckIdPartialNegativeFilter(): Unit = {
    val sourceSensorChannelIds = Set("foo", "bar", "bletch", "spam")

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "-bar")
    assertEquals(Set("foo", "bletch", "spam"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "-bar:bletch")
    assertEquals(Set("foo", "spam"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromDescriptionAndSource(sourceSensorChannelIds, "-bar:bletch:glorp")
    assertEquals(Set("foo", "spam"), result3.toSet)
  }

  @Test def testCheckDetailFullExpansion(): Unit = {
    val channels = getChannels()

    val result = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels, "*")

    assertEquals(Set("foo", "bar", "bletch", "spam"), result.toSet)
  }

  @Test def testCheckDetailPartialFilter(): Unit = {
    val channels = getChannels()

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels,"bar")
    assertEquals(Set("bar"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels,"bar:bletch")
    assertEquals(Set("bar", "bletch"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels,"bar:bletch:glorp")
    assertEquals(Set("bar", "bletch"), result3.toSet)
  }

  @Test def testCheckDetailPartialNegativeFilter(): Unit = {
    val channels = getChannels()

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels,"-bar")
    assertEquals(Set("foo", "bletch", "spam"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels, "-bar:bletch")
    assertEquals(Set("foo", "spam"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromSensorChannelDetailDescription(
      channels,"-bar:bletch:glorp")
    assertEquals(Set("foo", "spam"), result3.toSet)
  }

  @Test def testCheckTypeFullExpansion(): Unit = {
    val sensorType = getSensorType()

    val result = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType, "*")

    assertEquals(Set("foo", "bar", "bletch", "spam"), result.toSet)
  }

  @Test def testCheckTypePartialFilter(): Unit = {
    val sensorType = getSensorType()

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType,"bar")
    assertEquals(Set("bar"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType,"bar:bletch")
    assertEquals(Set("bar", "bletch"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType,"bar:bletch:glorp")
    assertEquals(Set("bar", "bletch"), result3.toSet)
  }

  @Test def testCheckTypePartialNegativeFilter(): Unit = {
    val sensorType = getSensorType()

    val result1 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType,"-bar")
    assertEquals(Set("foo", "bletch", "spam"), result1.toSet)

    val result2 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType, "-bar:bletch")
    assertEquals(Set("foo", "spam"), result2.toSet)

    val result3 = EntityDescriptionSupport.getSensorChannelIdsFromSensorTypeDescription(
      sensorType,"-bar:bletch:glorp")
    assertEquals(Set("foo", "spam"), result3.toSet)
  }

  def getChannels(): Iterable[SensorChannelDetailDescription] = {
    val sourceSensorChannelFoo = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelFoo.channelId).thenReturn("foo")

    val sourceSensorChannelBar = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelBar.channelId).thenReturn("bar")

    val sourceSensorChannelBletch = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelBletch.channelId).thenReturn("bletch")

    val sourceSensorChannelSpam = Mockito.mock(classOf[SensorChannelDetailDescription])
    Mockito.when(sourceSensorChannelSpam.channelId).thenReturn("spam")

    List(sourceSensorChannelFoo, sourceSensorChannelBar, sourceSensorChannelBletch, sourceSensorChannelSpam)
  }

  def getSensorType(): SensorTypeDescription = {
    val channels = getChannels()
    val sensorType = Mockito.mock(classOf[SensorTypeDescription])

    Mockito.when(sensorType.supportedChannelDetails).thenReturn(channels)

    sensorType
  }
}
