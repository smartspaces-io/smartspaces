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

import io.smartspaces.sensor.messaging.input.SensorInput
import io.smartspaces.util.SmartSpacesUtilities
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.util.data.json.StandardJsonMapper
import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl

import java.io.File
import java.util.List
import java.util.Map

import scala.collection.JavaConversions.asScalaBuffer

/**
 * A sensor input that reads the output of
 * {@link StandardFilePersistenceSensorHandler}.
 *
 * @author Keith M. Hughes
 */
class StandardFilePersistenceSensorInput(private val inputFile: File) extends SensorInput {

  /**
   * The file support for writing data.
   */
  private val fileSupport: FileSupport = FileSupportImpl.INSTANCE

  /**
   * The JSON mapper for serializing data.
   */
  private val jsonMapper: JsonMapper = StandardJsonMapper.INSTANCE

  /**
   * The sensor processor being run under.
   */
  private var sensorProcessor: SensorProcessor = null

  override def startup(): Unit = {
    // Nothing to do.
  }

  override def shutdown(): Unit = {
    // Nothing to do.
  }

  override def setSensorProcessor(sensorProcessor: SensorProcessor): Unit = {
    this.sensorProcessor = sensorProcessor
  }

  /**
   * Play back the data.
   */
  def play(): Unit = {
    val completeData = jsonMapper.parseObject(fileSupport.readFile(inputFile))

    val samples: List[Map[String, Object]] = completeData
      .get(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLES).asInstanceOf[List[Map[String, Object]]]

    var lastTimestamp: Long = 0;
    samples.foreach((sample) => {
      val newTimestamp =
        sample.get(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLE_TIMESTAMP).asInstanceOf[Long]

      if (lastTimestamp != 0) {
        SmartSpacesUtilities.delay(newTimestamp - lastTimestamp)
      }
      lastTimestamp = newTimestamp;

      val sampleData = sample.get(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLE_SAMPLE).asInstanceOf[Map[String, Object]]

      sensorProcessor.processSensorData(newTimestamp, new StandardDynamicObjectNavigator(sampleData))
    })
  }
}
