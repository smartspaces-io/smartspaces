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
 * An ID for a named and versioned resource.
 *
 * @author Keith M. Hughes
 */
public class SimpleNamedVersionedResource implements NamedVersionedResource {

  /**
   * The name of the bundle, the symbolic name in OSGi parlance.
   */
  private String name;

  /**
   * The version of the bundle.
   */
  private Version version;

  /**
   * Construct a new named versioned resource.
   */
  public SimpleNamedVersionedResource() {
  }

  /**
   * Construct a new named versioned resource.
   *
   * @param name
   *          name of the bundle
   * @param version
   *          version of the bundle
   */
  public SimpleNamedVersionedResource(String name, Version version) {
    this.name = name;
    this.version = version;
  }

  /**
   * Get the name of the resource.
   *
   * @return the name of the resource
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the resource.
   *
   * @param name
   *          the name of the resource
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the version of the resource.
   *
   * @return the version of the resource
   */
  @Override
  public Version getVersion() {
    return version;
  }

  /**
   * Set the version of the resource.
   *
   * @param version
   *          the version of the resource
   */
  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + version.hashCode();

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
    SimpleNamedVersionedResource other = (SimpleNamedVersionedResource) obj;
    if (!name.equals(other.name)) {
      return false;
    }
    return version.equals(other.version);
  }
}
