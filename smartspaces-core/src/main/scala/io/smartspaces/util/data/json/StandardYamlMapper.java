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

package io.smartspaces.util.data.json;

import io.smartspaces.SmartSpacesException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * The standard mapper from YAML objects.
 * 
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
 */
public class StandardYamlMapper implements YamlMapper {

  /**
   * A global mapper everyone can use.
   */
  public static final YamlMapper INSTANCE = new StandardYamlMapper();

  /**
   * The JSON mapper.
   */
  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper(new YAMLFactory());
  }

  @Override
  public Map<String, Object> parseObject(InputStream stream) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> object = MAPPER.readValue(stream, Map.class);
      return object;
    } catch (Exception e) {
      throw new SmartSpacesException("Could not parse YAML input stream", e);
    }
  }

  @Override
  public Map<String, Object> parseObject(String obj) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> object = MAPPER.readValue(obj, Map.class);
      return object;
    } catch (Exception e) {
      throw new SmartSpacesException("Could not parse YAML string", e);
    }
  }

}
