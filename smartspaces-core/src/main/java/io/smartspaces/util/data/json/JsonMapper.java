/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

/**
 * A mapper to and from JSON objects.
 *
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
 */
public interface JsonMapper {

  /**
   * Parse an object string.
   *
   * @param object
   *          the JSON string to parse
   *
   * @return the map, if it parsed corrected
   *
   * @throws SmartSpacesException
   *           the string did not parse properly
   */
  Map<String, Object> parseObject(String object) throws SmartSpacesException;

  /**
   * Take a map and write it as a string.
   *
   * <p>
   * Non 7-but ASCII characters will be escaped.
   *
   * @param data
   *          the object to serialize as JSON
   *
   * @return the string
   *
   * @throws SmartSpacesException
   *           the serialization failed
   */
  String toString(Object data) throws SmartSpacesException;
}
