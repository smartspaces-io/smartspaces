/**
 * Copyright (C) 2014 Keith M. Hughes.
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

package io.smartspaces.util.data.dynamic;

import java.util.Map;

/**
 * A basic builder for dynamic objects.
 *
 * @author Keith M. Hughes
 */
public interface DynamicObjectBuilder {

  /**
   * Set the property of an object.
   *
   * @param name
   *          name of the property
   * @param value
   *          the value, which should be a primitive (e.g. Integer)
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  DynamicObjectBuilder setProperty(String name, Object value)
      throws DynamicObjectSmartSpacesException;

  /**
   * Put in a collection of properties into an object.
   *
   * @param data
   *          map where keys are property names and values are property values
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  DynamicObjectBuilder setProperties(Map<String, Object> data)
      throws DynamicObjectSmartSpacesException;

  /**
   * Put the current object of the navigator into the object being built.
   *
   * <p>
   * This is a shallow copy. Any changes made to internal structure will be
   * reflected in the navigator.
   *
   * @param navigator
   *          the navigator
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level of the builder is not an object, or the
   *           navigator is not at an object
   */
  DynamicObjectBuilder setProperties(DynamicObject navigator)
      throws DynamicObjectSmartSpacesException;

  /**
   * Append a value into an array.
   *
   * @param value
   *          the value, which should be a primitive (e.g. Integer)
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  DynamicObjectBuilder add(Object value) throws DynamicObjectSmartSpacesException;

  /**
   * Append a value into an array.
   *
   * @param value
   *          the value, which should be a primitive (e.g. Integer)
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  DynamicObjectBuilder add(Object... values) throws DynamicObjectSmartSpacesException;

  /**
   * Append a collection of values into an array.
   *
   * @param values
   *          the values, which should be a primitive (e.g. Integer)
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  DynamicObjectBuilder add(Iterable<Object> value) throws DynamicObjectSmartSpacesException;

  /**
   * Put the current array of the navigator into the array being built.
   *
   * <p>
   * This is a shallow copy. Any changes made to internal structure will be
   * reflected in the navigator.
   *
   * @param navigator
   *          the navigator
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level of the builder is not an array, or the
   *           navigator is not at an array
   */
  DynamicObjectBuilder add(DynamicObject navigator) throws DynamicObjectSmartSpacesException;

  /**
   * Add a new object into the current object.
   * 
   * <p>
   * If the object already exists, the existing object will become the current level.
   *
   * @param name
   *          name of the new object
   *
   * @return the builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  DynamicObjectBuilder newObject(String name) throws DynamicObjectSmartSpacesException;

  /**
   * Add a new array into the current object.
   * 
   * <p>
   * If the array already exists, the existing array will become the current level.
   *
   * @param name
   *          name of the new array
   *
   * @return the builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  DynamicObjectBuilder newArray(String name) throws DynamicObjectSmartSpacesException;

  /**
   * Add a new array into the current array.
   *
   * @return the builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  DynamicObjectBuilder newArray();

  /**
   * Add a new object into the current array.
   *
   * @return the builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  DynamicObjectBuilder newObject() throws DynamicObjectSmartSpacesException;

  /**
   * Move up a level.
   *
   * @return this builder
   *
   * @throws DynamicObjectSmartSpacesException
   *           the builder is at the top level
   */
  DynamicObjectBuilder up() throws DynamicObjectSmartSpacesException;

  /**
   * Push a mark. This prevents the user from going beyond a point.
   */
  DynamicObjectBuilder pushMark();

  /**
   * Go back to the most recent mark.
   *
   * @param remove
   *          if {code true} remove the mark
   */
  DynamicObjectBuilder resetToMark(boolean remove);

  /**
   * Build the final object as a dynamic object.
   *
   * @return the fully built object as a dynamic object
   */
  DynamicObject toDynamicObject();

  /**
   * Build the final object as a map.
   *
   * @return the fully built object as a map
   */
  Map<String, Object> toMap();

  /**
   * Render the final object as a JSON string.
   * 
   * @return the final object as a JSON string
   */
  String toJson();
}
