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

/**
 * A reference to a resource dependency.
 *
 * @author Keith M. Hughes
 */
public class ResourceDependencyReference implements ResourceDependency {

  /**
   * Name of the resource.
   */
  private String name;

  /**
   * Version range for the dependency.
   */
  private VersionRange versionRange;

  /**
   * Construct a resource dependency reference.
   */
  public ResourceDependencyReference() {
  }

  /**
   * Construct a resource dependency reference.
   *
   * @param name
   *          name of the resource
   * @param versionRange
   *          range of versions of the resource required
   */
  public ResourceDependencyReference(String name, VersionRange versionRange) {
    this.name = name;
    this.versionRange = versionRange;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Set the name of the dependency.
   * 
   * @param name
   *          the name of the dependency
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public VersionRange getVersionRange() {
    return versionRange;
  }

  /**
   * Set the version range of the dependency.
   * 
   * @param versionRange
   *          the version range
   */
  public void setVersionRange(VersionRange versionRange) {
    this.versionRange = versionRange;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + versionRange.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    ResourceDependencyReference other = (ResourceDependencyReference) obj;
    if (!name.equals(other.name)) {
      return false;
    }

    return versionRange.equals(other.versionRange);
  }
}
