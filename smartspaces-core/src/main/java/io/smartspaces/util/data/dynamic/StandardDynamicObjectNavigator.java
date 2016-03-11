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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * A navigation object for working through a dynamic object.
 *
 * <p>
 * The view of the object is read-only.
 *
 * @author Keith M. Hughes
 */
public class StandardDynamicObjectNavigator implements DynamicObject {

  /**
   * The root object.
   */
  private Map<String, ? extends Object> root;

  /**
   * A stack of objects as we walk the graph.
   */
  private final Stack<Object> nav = new Stack<Object>();

  /**
   * Places where we cannot move up any further.
   */
  private final Stack<Integer> blocks = new Stack<Integer>();

  /**
   * Type of the current object.
   */
  private DynamicObjectType currentType;

  /**
   * The current object, if it is a object.
   */
  private Map<String, ? extends Object> currentObject;

  /**
   * The current list, if it is a list.
   */
  private List<Object> currentArray;

  /**
   * Current size of array if in an array
   */
  private int currentArraySize;

  /**
   * Construct a basic dynamic object with a Map.
   *
   * @param root
   *          the root map
   */
  public StandardDynamicObjectNavigator(Map<String, ? extends Object> root) {
    this.root = root;
    currentType = DynamicObjectType.OBJECT;
    currentObject = root;
  }

  /**
   * Construct a basic dynamic object.
   *
   * @param root
   *          the root object, must be a map
   *
   * @throws DynamicObjectSmartSpacesException
   *           the root was not a map
   */
  @SuppressWarnings("unchecked")
  public StandardDynamicObjectNavigator(Object root) throws DynamicObjectSmartSpacesException {
    if (root instanceof Map) {
      this.root = (Map<String, Object>) root;
      currentType = DynamicObjectType.OBJECT;
      currentObject = this.root;
    } else {
      throw new DynamicObjectSmartSpacesException("The root must be a map.");
    }
  }

  @Override
  public Map<String, ? extends Object> getRoot() {
    return root;
  }

  @Override
  public DynamicObjectType getType() {
    return currentType;
  }

  @Override
  public String getString(String propertyName) {
    return (String) getObjectProperty(propertyName);
  }

  @Override
  public String getRequiredString(String propertyName) throws DynamicObjectSmartSpacesException {
    String value = getString(propertyName);
    if (value != null) {
      return value;
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "No property with name %s at the current level in the dynamic object", propertyName));
    }
  }

  @Override
  public Integer getInteger(String propertyName) {
    return (Integer) getObjectProperty(propertyName);
  }

  @Override
  public Double getDouble(String propertyName) {
    Object value = getObjectProperty(propertyName);
    if (value != null) {
      return ((Number) value).doubleValue();
    } else {
      return null;
    }
  }

  @Override
  public Boolean getBoolean(String propertyName) {
    return (Boolean) getObjectProperty(propertyName);
  }

  @Override
  public boolean containsProperty(String name) {
    if (currentType == DynamicObjectType.OBJECT) {
      return currentObject.containsKey(name);
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "Current level is not a object when checking for property name %s", name));
    }
  }

  @Override
  public Set<String> getProperties() {
    if (currentType == DynamicObjectType.OBJECT) {
      return currentObject.keySet();
    } else {
      throw new DynamicObjectSmartSpacesException("Current level is not a object");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getItem(String propertyName) {
    return (T) getObjectProperty(propertyName);
  }

  @Override
  public String getString(int pos) throws DynamicObjectSmartSpacesException {
    return (String) getArrayIndex(pos);
  }

  @Override
  public Integer getInteger(int pos) throws DynamicObjectSmartSpacesException {
    return (Integer) getArrayIndex(pos);
  }

  @Override
  public Double getDouble(int pos) throws DynamicObjectSmartSpacesException {
    Object value = getArrayIndex(pos);

    if (value != null) {
      return ((Number) value).doubleValue();
    } else {
      return null;
    }
  }

  @Override
  public Boolean getBoolean(int pos) throws DynamicObjectSmartSpacesException {
    return (Boolean) getArrayIndex(pos);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getItem(int pos) throws DynamicObjectSmartSpacesException {
    return (T) getArrayIndex(pos);
  }

  @Override
  public int getSize() throws DynamicObjectSmartSpacesException {
    if (currentType == DynamicObjectType.ARRAY) {
      return currentArraySize;
    } else {
      throw new DynamicObjectSmartSpacesException("Current level is not array");
    }
  }

  /**
   * Get the named property from the object.
   *
   * @param name
   *          name of the property
   *
   * @return the value of the property, or {@code null} if none
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an object
   */
  private Object getObjectProperty(String propertyName) {
    if (currentType == DynamicObjectType.OBJECT) {
      return currentObject.get(propertyName);
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "Accessing object property %s, current level is not a object", propertyName));
    }
  }

  /**
   * Get the value at the given index from the array.
   *
   * @param index
   *          index of the desired value
   *
   * @return the value of the property, or {@code null} if none
   *
   * @throws DynamicObjectSmartSpacesException
   *           the current level is not an array
   */
  private Object getArrayIndex(int index) {
    if (currentType == DynamicObjectType.ARRAY) {
      return currentArray.get(index);
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "Accessing array index %d, current level is not an array", index));
    }
  }

  @Override
  public Map<String, Object> asMap() throws DynamicObjectSmartSpacesException {
    if (currentType == DynamicObjectType.OBJECT) {
      return Collections.unmodifiableMap(currentObject);
    } else {
      throw new DynamicObjectSmartSpacesException("Current level is not a object");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> asList() throws DynamicObjectSmartSpacesException {
    if (currentType == DynamicObjectType.ARRAY) {
      return Collections.unmodifiableList((List<T>) currentArray);
    } else {
      throw new DynamicObjectSmartSpacesException("Current level is not a object");
    }
  }

  @Override
  public DynamicObject down(String propertyName) {
    Object value = getObjectProperty(propertyName);

    if (value instanceof Map) {
      nav.push(currentObject);

      setCurrentAsObject(value);
    } else if (value instanceof List) {
      nav.push(currentObject);

      setCurrentAsArray(value);
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "The object property %s is neither an object or an array", propertyName));
    }

    return this;
  }

  @Override
  public DynamicObject down(int pos) throws DynamicObjectSmartSpacesException {
    Object value = getArrayIndex(pos);

    if (value instanceof Map) {
      nav.push(currentArray);

      setCurrentAsObject(value);
    } else if (value instanceof List) {
      nav.push(currentArray);

      setCurrentAsArray(value);
    } else {
      throw new DynamicObjectSmartSpacesException(String.format(
          "The array position %d is neither an object or an array", pos));
    }

    return this;
  }

  @Override
  public DynamicObject up() {
    if (!nav.isEmpty() && (blocks.isEmpty() || blocks.peek() < nav.size())) {
      Object value = nav.pop();

      if (value instanceof Map) {
        setCurrentAsObject(value);
      } else if (value instanceof List) {
        setCurrentAsArray(value);
      }

      return this;
    } else {
      throw new DynamicObjectSmartSpacesException("Could not go up, was at root or blocked");
    }
  }

  /**
   * Set the current item for the navigator as an object.
   *
   * @param value
   *          what should become the new current position
   */
  @SuppressWarnings("unchecked")
  private void setCurrentAsObject(Object value) {
    currentType = DynamicObjectType.OBJECT;
    currentObject = (Map<String, Object>) value;

    currentArray = null;
  }

  /**
   * Set the current item for the navigator as a map.
   *
   * @param value
   *          what should become the new current position
   */
  @SuppressWarnings("unchecked")
  private void setCurrentAsArray(Object value) {
    currentType = DynamicObjectType.ARRAY;
    currentArray = (List<Object>) value;
    currentArraySize = currentArray.size();

    currentObject = null;
  }

  @Override
  public Iterable<ObjectDynamicObjectEntry> getObjectEntries() {
    if (currentType == DynamicObjectType.OBJECT) {
      return new Iterable<ObjectDynamicObjectEntry>() {
        @Override
        public Iterator<ObjectDynamicObjectEntry> iterator() {
          return new BasicDynamicObjectObjectIterator();
        }
      };
    } else {
      throw new DynamicObjectSmartSpacesException(
          "Want an object iterator but the current level is not an object");
    }
  }

  @Override
  public Iterable<ArrayDynamicObjectEntry> getArrayEntries() {
    if (currentType == DynamicObjectType.ARRAY) {
      return new Iterable<ArrayDynamicObjectEntry>() {
        @Override
        public Iterator<ArrayDynamicObjectEntry> iterator() {
          return new BasicDynamicObjectArrayIterator();
        }
      };
    } else {
      throw new DynamicObjectSmartSpacesException(
          "Want an array iterator but the current level is not an array");
    }
  }

  /**
   * Traverse a string-based path to a position in the object.
   *
   * <p>
   * Not quite ready for prime time.
   *
   * @param path
   *          the path string
   *
   * @return the final object
   */
  @SuppressWarnings("unchecked")
  Object traversePath(String path) {
    Object curObject = null;

    if (currentType == DynamicObjectType.OBJECT) {
      curObject = currentObject;
    } else {
      curObject = currentArray;
    }

    String[] elements = path.split("\\.");

    for (int i = 0; i < elements.length; i++) {
      String element = elements[i].trim();

      if (element.isEmpty()) {
        throw new DynamicObjectSmartSpacesException(String.format("Empty element in path %s", path));
      }

      if (element.equals("$")) {
        curObject = root;
      } else if (element.startsWith("[")) {
        if (curObject instanceof List) {
          if (element.endsWith("]")) {
            int index = Integer.parseInt(element.substring(1, element.length() - 1));

            curObject = ((List<Object>) curObject).get(index);
          } else {
            throw new DynamicObjectSmartSpacesException(String.format(
                "Path element %s does not end in a ]", element));
          }
        } else if (curObject instanceof Map) {
          throw new DynamicObjectSmartSpacesException("Attempt to use an array index in an object");
        } else if (i < elements.length) {
          throw new DynamicObjectSmartSpacesException("Non array or object in the middle of a path");
        }
      } else {
        // Have a result name
        if (curObject instanceof Map) {
          curObject = ((Map<String, ? extends Object>) curObject).get(element);
        } else if (curObject instanceof List) {
          throw new DynamicObjectSmartSpacesException("Attempt to use an name index in an array");
        } else if (i < elements.length) {
          throw new DynamicObjectSmartSpacesException("Non array or object in the middle of a path");
        }
      }
    }

    return curObject;
  }

  @Override
  public String toString() {
    return "BasicDynamicObject [root=" + root + "]";
  }

  /**
   * Push a block. This prevents the user from going beyond a point.
   */
  private void pushBlock() {
    blocks.push(nav.size());
  }

  /**
   * Pop a block and reset the nav to that point.
   *
   * @param pop
   *          if {code true} pop the block off
   */
  private void resetToBlock(boolean pop) {
    int pos;
    if (pop) {
      pos = blocks.pop();
    } else {
      pos = blocks.peek();
    }

    if (pos < nav.size()) {
      Object value = nav.get(pos);
      if (value instanceof Map) {
        setCurrentAsObject(value);
      } else if (value instanceof List) {
        setCurrentAsArray(value);
      }

      nav.setSize(pos);
    }
  }

  /**
   * An {@link Iterator} wandering over an object.
   *
   * @author Keith M. Hughes
   */
  private class BasicDynamicObjectObjectIterator implements Iterator<ObjectDynamicObjectEntry> {

    /**
     * The entry that everyone will use.
     */
    private ObjectDynamicObjectEntry entry;

    /**
     * The current property we are on.
     */
    private String currentProperty;

    /**
     * The properties of the current level.
     */
    private Iterator<String> properties;

    /**
     * {@code true} if the iterator has unblocked the object
     */
    private boolean hasUnblocked = false;

    BasicDynamicObjectObjectIterator() {
      entry = new ObjectDynamicObjectEntry() {

        @Override
        public String getProperty() {
          return currentProperty;
        }

        @Override
        public DynamicObject getValue() {
          return StandardDynamicObjectNavigator.this;
        }

        @Override
        public DynamicObject down() {
          StandardDynamicObjectNavigator.this.down(getProperty());

          return StandardDynamicObjectNavigator.this;
        }
      };

      properties = currentObject.keySet().iterator();

      pushBlock();
    }

    @Override
    public boolean hasNext() {
      if (properties.hasNext()) {
        return true;
      } else {
        if (!hasUnblocked) {
          resetToBlock(true);
          hasUnblocked = true;
        }

        return false;
      }
    }

    @Override
    public ObjectDynamicObjectEntry next() {
      if (properties.hasNext()) {
        currentProperty = properties.next();

        resetToBlock(false);

        return entry;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is unsupported");
    }
  }

  /**
   * An {@link Iterator} wandering over an array.
   *
   * @author Keith M. Hughes
   */
  private class BasicDynamicObjectArrayIterator implements Iterator<ArrayDynamicObjectEntry> {

    /**
     * The entry that everyone will use.
     */
    private ArrayDynamicObjectEntry entry;

    /**
     * The current index we are on.
     */
    private int currentIndex;

    /**
     * {@code true} if the iterator has unblocked the object
     */
    private boolean hasUnblocked = false;

    /**
     * The total number of elements in the array.
     */
    private int maxIndex;

    BasicDynamicObjectArrayIterator() {
      entry = new ArrayDynamicObjectEntry() {

        @Override
        public int getIndex() {
          // currentIndex will always be bumped when we are here
          return currentIndex - 1;
        }

        @Override
        public DynamicObject getValue() {
          return StandardDynamicObjectNavigator.this;
        }

        @Override
        public DynamicObject down() {
          StandardDynamicObjectNavigator.this.down(getIndex());

          return StandardDynamicObjectNavigator.this;
        }
      };

      maxIndex = currentArray.size();

      pushBlock();
    }

    @Override
    public boolean hasNext() {
      if (currentIndex < maxIndex) {
        return true;
      } else {
        if (!hasUnblocked) {
          resetToBlock(true);
          hasUnblocked = true;
        }

        return false;
      }
    }

    @Override
    public ArrayDynamicObjectEntry next() {
      if (hasNext()) {
        currentIndex++;

        resetToBlock(false);

        return entry;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is unsupported");
    }
  }
}
