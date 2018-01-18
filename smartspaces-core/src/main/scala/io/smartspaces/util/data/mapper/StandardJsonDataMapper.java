/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The standard mapper to and from JSON objects.
 * 
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
 */
public class StandardJsonDataMapper implements JsonDataMapper {

  /**
   * A global mapper everyone can use.
   */
  public static final JsonDataMapper INSTANCE = new StandardJsonDataMapper();

  /**
   * The JSON mapper.
   */
  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.getFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
  }

  @Override
  public Map<String, Object> parseObject(InputStream stream) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> object = MAPPER.readValue(stream, Map.class);
      return object;
    } catch (Exception e) {
      throw new SmartSpacesException("Could not parse JSON input stream", e);
    }
  }

  @Override
  public Map<String, Object> parseObject(File file) throws SmartSpacesException {
    try (FileInputStream fos = new FileInputStream(file)) {
      return parseObject(fos);
    } catch (FileNotFoundException e) {
      throw new SimpleSmartSpacesException("JSON file not found " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new SimpleSmartSpacesException(
          "IO exception while reading JSON file " + file.getAbsolutePath(), e);
    }
  }

  @Override
  public Map<String, Object> parseObject(String object) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> value = MAPPER.readValue(object, Map.class);
      return value;
    } catch (Throwable e) {
      throw new DataMapperSmartSpacesException("Could not parse JSON string", e);
    }
  }

  @Override
  public Object parse(String object) throws SmartSpacesException {
    try {
      Object value = MAPPER.readValue(object, Object.class);
      return value;
    } catch (Throwable e) {
      throw new DataMapperSmartSpacesException("Could not parse JSON string", e);
    }
  }

  @Override
  public String toString(Object data) throws SmartSpacesException {
    try {
      return MAPPER.writeValueAsString(data);
    } catch (Throwable e) {
      throw new DataMapperSmartSpacesException("Could not serialize JSON object as string", e);
    }
  }

  @Override
  public void toOutputStream(OutputStream out,Object data)
      throws SmartSpacesException {
    try {
      MAPPER.writeValue(out, data);
    } catch (Throwable e) {
      throw new SmartSpacesException("Could not serialize map as JSON output stream", e);
    }
  }

  @Override
  public void toFile(File file, Object data) throws SmartSpacesException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      toOutputStream(fos, data);
    } catch (FileNotFoundException e) {
      throw new SimpleSmartSpacesException("JSON file not found " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new SimpleSmartSpacesException(
          "IO exception while writing JSON file " + file.getAbsolutePath(), e);
    }
  }
}
