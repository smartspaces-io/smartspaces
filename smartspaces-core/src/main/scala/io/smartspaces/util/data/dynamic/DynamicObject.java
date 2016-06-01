/*
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An object for processing the contents of a dynamic object. This object allows
 * you to navigate to various locations in the dynamic object and extract
 * information from those locations.
 *
 * <p>
 * These navigation objects are stateful, meaning that a single instance is
 * aware of a current position in its navigation through the object. Usage of
 * this class is not thread safe as you cannot be in two places at the same time
 * with a given instance.
 *
 * <p>
 * The view of the object is read-only.
 *
 * @author Keith M. Hughes
 */
public interface DynamicObject {

  /**
   * Get the root object of the navigator.
   *
   * @return the root object
   */
  Map<String, ? extends Object> getRoot();

  /**
   * Get the current type of the current navigation point.
   *
   * @return the current type
   */
  DynamicObjectType getType();

  /**
   * If the current level is a object, get a string field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  String getString(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get a string field from the object. The
   * field is required.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object or the field does not exist
   */
  String getRequiredString(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get an integer field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  Integer getInteger(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get a double field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  Double getDouble(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get a boolean field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  Boolean getBoolean(String name) throws DynamicObjectSmartSpacesException;

  /**
   * Does the current object contain a property with the given name?
   *
   * @param name
   *          the name of the property to check for
   *
   * @return {@code true} if the property exists
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level was not an object
   */
  boolean containsProperty(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get the names of all properties for that
   * object.
   *
   * @return names of all properties for the object
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  Set<String> getProperties() throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @param <T>
   *          expected type of the result
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  <T> T getItem(String name) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get a string field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  String getString(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get an integer field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  Integer getInteger(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get a double field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  Double getDouble(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get a boolean field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  Boolean getBoolean(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get an object field from the object.
   *
   * @param <T>
   *          the expected type of the result
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  <T> T getItem(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * If the current level is an array, get the size of the array.
   *
   * @return size of the current array
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  int getSize() throws DynamicObjectSmartSpacesException;
  
  /**
   * Is the current level an object?
   * 
   * @return {@code true} if the current level is an object
   */
  boolean isObject();

  /**
   * If the current level is a object, get that object as a map.
   *
   * <p>
   * The map returned is not protected. If modified, the navigator will have a
   * different structure to walk through.
   *
   * @return the current object
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level was not an object
   */
  Map<String, ? extends Object> asMap() throws DynamicObjectSmartSpacesException;
  
  /**
   * Is the current level an array?
   * 
   * @return {@code true} if the current level is an array
   */
  boolean isArray();

  /**
   * If the current level is an array, get that array.
   *
   * <p>
   * This method makes no guarantee that the list actually contains all the same
   * element type.
   *
   * <p>
   * The array returned is not protected. If modified, the navigator will have a
   * different structure to walk through.
   *
   * @param <T>
   *          the type that the array should be
   *
   * @return the current array
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level was not an array
   */
  <T> List<T> asList() throws DynamicObjectSmartSpacesException;

  /**
   * Move into the object to the object at a given name.
   *
   * <p>
   * The next level must be a collection, not a primitive.
   *
   * @param name
   *          the name to move down to
   *
   * @return this dynamic object
   *
   * @throws DynamicObjectSmartSpacesException
   *           current level not an object or there is no property with the name
   */
  DynamicObject down(String name) throws DynamicObjectSmartSpacesException;

  /**
   * Move into the array to the item at a given position.
   *
   * <p>
   * The next level must be a collection, not a primitive.
   *
   * @param pos
   *          the position in the array
   *
   * @return this dynamic object
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  DynamicObject down(int pos) throws DynamicObjectSmartSpacesException;

  /**
   * Move up one level in the navigation.
   *
   * @return this dynamic object
   *
   * @throws DynamicObjectSmartSpacesException
   *           can't move up because either blocked or no up to go to
   */
  DynamicObject up() throws DynamicObjectSmartSpacesException;

  /**
   * Get an iterator for walking through the current object.
   *
   * <p>
   * There is no guarantee that the iterators provided are using new objects or
   * old objects, so do not walk multiple iterators from the same root navigator
   *
   * <p>
   * The object will not allow you to use {@link #up()} above the position you
   * are in the object at the point this method is called.
   *
   * @return an iterable for the current object
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an object
   */
  Iterable<ObjectDynamicObjectEntry> getObjectEntries() throws DynamicObjectSmartSpacesException;

  /**
   * Get an iterator for walking through the current array.
   *
   * <p>
   * There is no guarantee that the iterators provided are using new objects or
   * old objects, so do not walk multiple iterators from the same root navigator
   *
   * <p>
   * The object will not allow you to use {@link #up()} above the position you
   * are in the object at the point this method is called.
   *
   * @return an iterable for the current array
   *
   * @throws DynamicObjectSmartSpacesException
   *           not an array
   */
  Iterable<ArrayDynamicObjectEntry> getArrayEntries() throws DynamicObjectSmartSpacesException;

  /**
   * An entry in an object in a dynamic object.
   *
   * @author Keith M. Hughes
   */
  public interface ObjectDynamicObjectEntry {

    /**
     * Get the property name for this entry.
     *
     * @return the property name
     */
    String getProperty();

    /**
     * Get the {@link DynamicObject} positioned at the point referenced by
     * {@link #getProperty()}.
     *
     * <p>
     * There is no guarantee that this object is new or a reused one. You should
     * NOT You should not store this reference or process it in another thread.
     *
     * @return a dynamic object positioned at the object whose properties are
     *         being iterated over
     */
    DynamicObject getValue();

    /**
     * Move down into the current property of the object.
     *
     * <p>
     * Only valid if the next stop is an object or array.
     *
     * <p>
     * There is no guarantee that this object is new or a reused one. You should
     * NOT store this reference or process it in another thread.
     *
     * @return a dynamic object repositioned to the current property
     */
    DynamicObject down();
  }

  /**
   * An entry in an array in a dynamic object.
   *
   * @author Keith M. Hughes
   */
  public interface ArrayDynamicObjectEntry {

    /**
     * Get the index for this entry.
     *
     * @return the index
     */
    int getIndex();

    /**
     * Get the {@link DynamicObject} positioned at the point referenced by
     * {@link #getIndex()}.
     *
     * <p>
     * There is no guarantee that this object is new or a reused one. You should
     * NOT You should not store this reference or process it in another thread.
     *
     * @return a dynamic object positioned at the array whose indices are being
     *         iterated over
     */
    DynamicObject getValue();

    /**
     * Move down into the current index of the array.
     *
     * <p>
     * Only valid if the next stop is an object or array.
     *
     * <p>
     * There is no guarantee that this object is new or a reused one. You should
     * NOT You should not store this reference or process it in another thread.
     *
     * @return a dynamic object repositioned to the current index
     */
    DynamicObject down();
  }
}
