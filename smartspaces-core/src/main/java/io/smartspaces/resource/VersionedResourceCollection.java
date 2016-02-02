/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.resource;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.common.collect.Lists;

/**
 * A collection of versioned resources.
 *
 * <p>
 * This does not mean that everything in the collection implements
 * {@link VersionedResource}. This allows the class to be used for anything.
 *
 * <p>
 * This class is thread safe.
 *
 * @param <T>
 *          type of data in the collection
 *
 * @author Keith M. Hughes
 */
public class VersionedResourceCollection<T> {

  /**
   * Create a new versioned resource collection in a generic way.
   *
   * @param <T>
   *          the type of elements stored in the collection
   *
   * @return an empty resource collection
   */
  public static <T> VersionedResourceCollection<T> newVersionedResourceCollection() {
    return new VersionedResourceCollection<T>();
  }

  /**
   * The resources in the collection.
   */
  private final ConcurrentSkipListMap<Version, T> resources =
      new ConcurrentSkipListMap<Version, T>();

  /**
   * Add in a new resource for a given version.
   *
   * @param version
   *          the version of the resource
   * @param resource
   *          the resource
   *
   * @return the old resource with that version, or {@code null} if none
   */
  public T addResource(Version version, T resource) {
    return resources.put(version, resource);
  }

  /**
   * Get the resource with the given version, if any.
   *
   * @param version
   *          the version
   *
   * @return the resource with the version or {@code null} if none
   */
  public T getResource(Version version) {
    return resources.get(version);
  }

  /**
   * Get the highest version of the collection that fits in the specified range.
   *
   * @param range
   *          the range
   *
   * @return the highest version in the range or {@code null} if there are no
   *         elements in the range
   */
  public T getResource(VersionRange range) {
    Entry<Version, T> result;
    Version maximum = range.getMaximum();
    if (maximum != null) {
      // If inclusive, get the largest maximum that can also equal the maximum.
      // Otherwise is exclusive so get the largest maximum that is less than the
      // maximum.
      result = range.isInclusive() ? resources.floorEntry(maximum) : resources.lowerEntry(maximum);
    } else {
      // Hard to be equal to infinity, so would always be the last entry.
      result = resources.lastEntry();
    }

    if (result != null) {
      if (range.getMinimum().lessThanOrEqual(result.getKey())) {
        return result.getValue();
      }
    }

    return null;
  }

  /**
   * Get the highest version from the collection.
   *
   * @return the highest version or {@code null} if the collection is empty
   */
  public T getHighestEntry() {
    return resources.lastEntry().getValue();
  }

  /**
   * Remove the resource for a given version.
   *
   * @param version
   *          the version of the resource
   *
   * @return the old resource with that version, or {@code null} if none
   */
  public T removeResource(Version version) {
    return resources.remove(version);
  }

  /**
   * Get a list of all resources, ordered by version number.
   *
   * @return a newly constructed list
   */
  public List<T> getAllResources() {
    return Lists.newArrayList(resources.values());
  }
}
