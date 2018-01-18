/*
 * Copyright (C) 2018 Keith M. Hughes
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import io.smartspaces.SmartSpacesException;

/**
 * A mapper to and from serialized objects.
 *
 * <p>
 * These objects are considered thread-safe and can handle multiple conversions
 * simultaneously.
 *
 * @author Keith M. Hughes
*/
public interface DataMapper {

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
   * Parse an object file.
   *
   * @param file
   *          the file to parse
   *
   * @return the map, if it parsed corrected
   *
   * @throws SmartSpacesException
   *           the stream did not parse properly
   */
  Map<String, Object> parseObject(File file) throws SmartSpacesException;

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
   * Parse JSON.
   *
   * @param object
   *          the serialized string to parse
   *
   * @return whatever the string parses as
   *
   * @throws SmartSpacesException
   *           the string did not parse properly
   */
  Object parse(String object) throws SmartSpacesException;

  /**
   * Take a map and write it as a string.
   *
   * <p>
   * Non 7-but ASCII characters will be escaped.
   *
   * @param data
   *          the object to serialize 
   *
   * @return the string
   *
   * @throws SmartSpacesException
   *           the serialization failed
   */
  String toString(Object data) throws SmartSpacesException;

  /**
   * Take a map and write it to an output stream.
   *
   * <p>
   * Non 7-but ASCII characters will be escaped.
   *
   * @param out
   *          the stream to write to
   * @param data
   *          the object to serialize
   *
   * @throws SmartSpacesException
   *           the serialization failed
   */
  void toOutputStream(OutputStream out, Object data) throws SmartSpacesException;

  /**
   * Take a map and write it to a file.
   *
   * <p>
   * Non 7-but ASCII characters will be escaped.
   *
   * @param file
   *          the file to write to
   * @param data
   *          the object to serialize
   *
   * @throws SmartSpacesException
   *           the serialization failed
   */
  void toFile(File file, Object data) throws SmartSpacesException;
}
