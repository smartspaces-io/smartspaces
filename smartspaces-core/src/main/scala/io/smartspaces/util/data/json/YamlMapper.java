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

import java.io.InputStream;
import java.util.Map;

/**
 * A mapper from YAML objects.
 *
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
 */
public interface YamlMapper {

  /**
   * Parse an object input stream.
   *
   * @param stream
   *          the stream to parse
   *
   * @return the map, if it parsed corrected
   *
   * @throws SmartSpacesException
   *           the stream did not parse properly
   */
  Map<String, Object> parseObject(InputStream stream) throws SmartSpacesException;

  /**
   * Parse an object in a string.
   *
   * @param object
   *          the string to parse
   *
   * @return the map, if it parsed corrected
   *
   * @throws SmartSpacesException
   *           the string did not parse properly
   */
  Map<String, Object> parseObject(String object) throws SmartSpacesException;

}
