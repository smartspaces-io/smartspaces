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

package io.smartspaces.util.data.mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;

/**
 * The standard mapper from YAML objects.
 * 
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
 */
public class StandardYamlDataMapper implements YamlDataMapper {

  /**
   * A global mapper everyone can use.
   */
  public static final YamlDataMapper INSTANCE = new StandardYamlDataMapper();

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
  public Map<String, Object> parseObject(File file) throws SmartSpacesException {
    try (FileInputStream fos = new FileInputStream(file)) {
      return parseObject(fos);
    } catch (FileNotFoundException e) {
      throw new SimpleSmartSpacesException("YAML file not found " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new SimpleSmartSpacesException(
          "IO exception while reading YAML file " + file.getAbsolutePath(), e);
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

  @Override
  public Object parse(String object) throws SmartSpacesException {
    try {
      Object value = MAPPER.readValue(object, Object.class);
      return value;
    } catch (Throwable e) {
      throw new DataMapperSmartSpacesException("Could not parse YAML string", e);
    }
  }

  @Override
  public String toString(Object data) throws SmartSpacesException {
    try {
      return MAPPER.writeValueAsString(data);
    } catch (Throwable e) {
      throw new SmartSpacesException("Could not serialize map as YAML string", e);
    }
  }

  @Override
  public void toOutputStream(OutputStream out, Object data)
      throws SmartSpacesException {
    try {
      MAPPER.writeValue(out, data);
    } catch (Throwable e) {
      throw new SmartSpacesException("Could not serialize map as YAML output stream", e);
    }
  }

  @Override
  public void toFile(File file, Object data) throws SmartSpacesException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      toOutputStream(fos, data);
    } catch (FileNotFoundException e) {
      throw new SimpleSmartSpacesException("YAML file not found " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new SimpleSmartSpacesException(
          "IO exception while writing YAML file " + file.getAbsolutePath(), e);
    }
  }
}
