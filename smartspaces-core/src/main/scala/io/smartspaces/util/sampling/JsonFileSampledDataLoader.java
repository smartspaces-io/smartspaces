/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.util.sampling;

import io.smartspaces.util.data.dynamic.DynamicObject;
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectNavigator;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link SampledDataLoader} which uses JSON files for storage.
 *
 * @author Keith M. Hughes
 */
public class JsonFileSampledDataLoader implements SampledDataLoader {

  /**
   * The property name giving the sames array.
   */
  public static final String PROPERTY_NAME_SAMPLES_ARRAY = "samples";

  /**
   * The JSON mapper to use for serializing and deserializing the data.
   */
  private static final JsonMapper JSON_MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The file support to use for this class.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void save(File dataFile, Map<String, int[]> data) {
    DynamicObjectBuilder builder = new StandardDynamicObjectBuilder();

    for (Entry<String, int[]> entry : data.entrySet()) {
      builder.newObject(entry.getKey());

      builder.newArray(PROPERTY_NAME_SAMPLES_ARRAY);

      for (int sample : entry.getValue()) {
        builder.add(sample);
      }

      // From array of samples
      builder.up();

      // From object
      builder.up();
    }

    fileSupport.writeFile(dataFile, JSON_MAPPER.toString(builder.toMap()));
  }

  @Override
  public void save(File dataFile, SampledDataCollection data) {
    save(dataFile, data.getData());
  }

  @Override
  public void load(File dataFile, Map<String, int[]> data) {
    data.clear();
    DynamicObject nav =
        new StandardDynamicObjectNavigator(JSON_MAPPER.parseObject(fileSupport.readFile(dataFile)));

    for (String name : nav.getProperties()) {
      nav.down(name).down(PROPERTY_NAME_SAMPLES_ARRAY);

      int numberSamples = nav.getSize();
      int[] samples = new int[numberSamples];
      for (int j = 0; j < numberSamples; j++) {
        samples[j] = nav.getInteger(j);
      }

      nav.up().up();

      data.put(name, samples);
    }
  }

  @Override
  public void load(File dataFile, SampledDataCollection data) {
    load(dataFile, data.getData());
  }
}
