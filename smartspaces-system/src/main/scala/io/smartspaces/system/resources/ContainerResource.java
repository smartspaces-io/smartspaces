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

package io.smartspaces.system.resources;

import io.smartspaces.resource.SimpleNamedVersionedResource;
import io.smartspaces.resource.Version;

/**
 * A resource in a container, e.g. a bundle.
 *
 * @author Keith M. Hughes
 */
public class ContainerResource extends SimpleNamedVersionedResource {

  /**
   * The type of the resource.
   */
  private ContainerResourceType type;

  /**
   * Location of the resource in a container.
   */
  private ContainerResourceLocation location;

  /**
   * The signature of the resource.
   */
  private String signature;

  /**
   * The source name of the resource, e.g. the original filename.
   */
  private String sourceName;

  /**
   * Construct a new container resource.
   */
  public ContainerResource() {
  }

  /**
   * Construct a new container resource.
   *
   * @param name
   *          name of the resource
   * @param version
   *          version of the resource
   * @param type
   *          the type of the resource
   * @param location
   *          location of the resource
   * @param signatureInitial
   *          the initial signature of the resource
   * @param sourceName
   *          the source name of the resource, e.g. the original filename
   */
  public ContainerResource(String name, Version version, ContainerResourceType type,
      ContainerResourceLocation location, String signatureInitial, String sourceName) {
    super(name, version);

    this.type = type;
    this.location = location;
    this.signature = signatureInitial;
    this.sourceName = sourceName;
  }

  /**
   * Get the type of the container resource.
   *
   * @return the type of the container resource
   */
  public ContainerResourceType getType() {
    return type;
  }

  /**
   * Get the type of the container resource.
   *
   * @return the type of the container resource
   */
  public void setType(ContainerResourceType type) {
    this.type = type;
  }

  /**
   * Get the location of the container resource.
   *
   * @return the location of the container resource
   */
  public ContainerResourceLocation getLocation() {
    return location;
  }

  /**
   * Set the location of the container resource.
   *
   * @param location
   *          the location of the container resource
   */
  public void setLocation(ContainerResourceLocation location) {
    this.location = location;
  }

  /**
   * Get the signature of the resource.
   *
   * @return the signature of the resource
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Update the signature for the resource.
   *
   * @param signature
   *          the signature
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * Get the source name of the resource, e.g. the original filename.
   * 
   * @return the source name
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Set the source name of the resource, e.g. the original filename.
   * 
   * @param sourceName
   *          the source name
   */
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + location.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ContainerResource other = (ContainerResource) obj;
    if (location != other.location) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ContainerResource [type=" + type + ", location=" + location + ", signature=" + signature
        + ", sourceName=" + sourceName + "]";
  }
}
