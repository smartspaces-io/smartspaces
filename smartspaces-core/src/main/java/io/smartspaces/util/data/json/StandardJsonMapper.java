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

package io.smartspaces.util.data.json;

import io.smartspaces.SmartSpacesException;

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
public class StandardJsonMapper implements JsonMapper {

  /**
   * A global mapper everyone can use.
   */
  public static final JsonMapper INSTANCE = new StandardJsonMapper();

  /**
   * The JSON mapper.
   */
  private static final ObjectMapper MAPPER;

  static {
    MAPPER = new ObjectMapper();
    MAPPER.getFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
  }

  @Override
  public Map<String, Object> parseObject(String object) throws SmartSpacesException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = MAPPER.readValue(object, Map.class);
      return map;
    } catch (Throwable e) {
      throw new JsonSmartSpacesException("Could not parse JSON string", e);
    }
  }

  @Override
  public String toString(Object data) throws SmartSpacesException {
    try {
      return MAPPER.writeValueAsString(data);
    } catch (Throwable e) {
      throw new JsonSmartSpacesException("Could not serialize JSON object as string", e);
    }
  }
}
