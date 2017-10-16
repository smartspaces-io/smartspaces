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

package io.smartspaces.sensor.processing

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.util.data.json.JsonMapper
import io.smartspaces.util.data.json.StandardJsonMapper
import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl

import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

/**
 * A standard file sensor handler that persists the data in a file.
 *
 * <p>
 * The data is stored in a JSON format.
 *
 * @author Keith M. Hughes
 */
object StandardFilePersistenceSensorHandler {
  
  /**
   * The field name for all samples. The field is a list.
   */
  val FIELD_NAME_SAMPLES = "samples"

  /**
   * The field name for the timestamp of a sample. The field is a long.
   */
  val FIELD_NAME_SAMPLE_TIMESTAMP = "timestamp"

  /**
   * The field name for the sample data of a sample. The field is a map.
   */
  val FIELD_NAME_SAMPLE_SAMPLE = "sample"
}

/**
 * A standard file sensor handler that persists the data in a file.
 *
 * <p>
 * The data is stored in a JSON format.
 *
 * @author Keith M. Hughes
 */
class StandardFilePersistenceSensorHandler(private val outputFile: File) extends SensorHandler {

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
  var sensorProcessor: SensorProcessor = null

  /**
   * The data to save.
   */
  private var dataToSave: Map[String, Object] = null

  /**
   * The array that will store the sample objects.
   */
  private var samples: List[Map[String, Object]] = null

  override def startup(): Unit = {
    dataToSave = new HashMap

    samples = new ArrayList
    dataToSave.put(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLES, samples)
  }

  override def shutdown(): Unit = {
    saveData()
  }

  override def handleSensorMessage(timestamp: Long, data: DynamicObject): Unit = {
    val sample: Map[String, Object] = new HashMap
    samples.add(sample)

    val l: java.lang.Long = timestamp
    sample.put(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLE_TIMESTAMP, l)
    sample.put(StandardFilePersistenceSensorHandler.FIELD_NAME_SAMPLE_SAMPLE, data.asMap())
  }

  /**
   * Save the data in the file.
   */
  private def saveData(): Unit = {
    val rendered = jsonMapper.toString(dataToSave);

    fileSupport.writeFile(outputFile, rendered)
  }
}
