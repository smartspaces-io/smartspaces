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

import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A basic builder for dynamic objects.
 *
 * @author Keith M. Hughes
 */
public class StandardDynamicObjectBuilder implements DynamicObjectBuilder {

  /**
   * The mapper to and from JSON.
   */
  private static final JsonMapper JSON_MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The root object.
   */
  private final Map<String, Object> root = new HashMap<>();

  /**
   * A stack of objects as we walk the graph.
   */
  private final Stack<Object> nav = new Stack<>();

  /**
   * Type of the current object.
   */
  private DynamicObjectType currentType;

  /**
   * The current object, if it is a map.
   */
  private Map<String, Object> currentObject;

  /**
   * The current object, if it is an array.
   */
  private List<Object> currentArray;

  /**
   * Construct a new builder.
   */
  public StandardDynamicObjectBuilder() {
    currentObject = root;
    currentType = DynamicObjectType.OBJECT;
  }

  @Override
  public DynamicObjectBuilder setProperty(String name, Object value) {
    if (currentType == DynamicObjectType.OBJECT) {
      currentObject.put(name, value);
    } else {
      // Must be an object

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an object");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder setProperties(Map<String, Object> data)
      throws DynamicObjectSmartSpacesException {
    if (currentType == DynamicObjectType.OBJECT) {
      currentObject.putAll(data);
    } else {
      // Must be an object

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an object");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder setProperties(DynamicObject navigator)
      throws DynamicObjectSmartSpacesException {
    if (currentType == DynamicObjectType.OBJECT) {
      currentObject.putAll(navigator.asMap());
    } else {
      // Must be an object

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an object");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder add(Object value) {
    if (currentType == DynamicObjectType.ARRAY) {
      currentArray.add(value);
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder add(Object... values) {
    if (currentType == DynamicObjectType.ARRAY) {
      if (values != null) {
        for (Object value : values) {
          currentArray.add(value);
        }
      }
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder add(Iterable<Object> values) {
    if (currentType == DynamicObjectType.ARRAY) {
      if (values != null) {
        for (Object value : values) {
          currentArray.add(value);
        }
      }
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder add(DynamicObject navigator) {
    if (currentType == DynamicObjectType.ARRAY) {
      List<Object> values = navigator.asList();
      currentArray.add(values);
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException(
          "The builder is not currently building an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder newObject(String name) {
    if (currentType == DynamicObjectType.OBJECT) {
      Map<String, Object> newObject = Maps.newHashMap();

      currentObject.put(name, newObject);

      nav.push(currentObject);

      currentObject = newObject;
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException("Cannot put named item into an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder newArray(String name) {
    if (currentType == DynamicObjectType.OBJECT) {
      List<Object> newObject = Lists.newArrayList();

      currentObject.put(name, newObject);

      nav.push(currentObject);

      currentArray = newObject;
      currentType = DynamicObjectType.ARRAY;
    } else {
      // Must be an array

      throw new DynamicObjectSmartSpacesException("Cannot put named item into an array");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder newArray() {
    if (currentType == DynamicObjectType.ARRAY) {
      List<Object> newObject = Lists.newArrayList();

      currentArray.add(newObject);

      nav.push(currentArray);

      currentArray = newObject;
    } else {
      // Must be an object

      throw new DynamicObjectSmartSpacesException("Cannot put unnamed item into an object");
    }

    return this;
  }

  @Override
  public DynamicObjectBuilder newObject() {
    if (currentType == DynamicObjectType.ARRAY) {
      Map<String, Object> newObject = Maps.newHashMap();

      currentArray.add(newObject);

      nav.push(currentArray);

      currentObject = newObject;
      currentType = DynamicObjectType.OBJECT;
    } else {
      // Must be an object

      throw new DynamicObjectSmartSpacesException("Cannot put unnamed item into an object");
    }

    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public DynamicObjectBuilder up() {
    if (!nav.isEmpty()) {
      Object newObject = nav.pop();

      if (newObject instanceof Map) {
        currentArray = null;
        currentObject = (Map<String, Object>) newObject;
        currentType = DynamicObjectType.OBJECT;
      } else {
        currentObject = null;
        currentArray = (List<Object>) newObject;
        currentType = DynamicObjectType.ARRAY;
      }
    } else {
      throw new DynamicObjectSmartSpacesException("Cannot move up in builder, nothing left");
    }

    return this;
  }

  @Override
  public Map<String, Object> buildAsMap() {
    return root;
  }

  @Override
  public String toJson() {
    return JSON_MAPPER.toString(root);
  }

  @Override
  public String toString() {
    return root.toString();
  }
}
