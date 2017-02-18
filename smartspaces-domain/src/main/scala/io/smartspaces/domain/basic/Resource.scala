/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.domain.basic

import io.smartspaces.domain.PersistedObject

import java.io.Serializable
import java.util.Date

/**
 * A resource, such as a dependency for an activity.
 * 
 * @author Keith M. Hughes
 */
trait Resource extends PersistedObject with Serializable {
  
  /**
   * Get the identifying name for the resource.
   *
   * @return the identifying name
   */
  def getIdentifyingName(): String

  /**
   * Set the identifying name for the resource.
   *
   * @param name
   *          the identifying name
   */
  def setIdentifyingName(name: String ): Unit

  /**
   * Get the version for the resource.
   *
   * @return the version
   */
  def getVersion(): String

  /**
   * Set the version for the resource.
   *
   * @param version
   *          the version
   */
  def setVersion(version: String): Unit

  /**
   * Get when the resource was last uploaded into the master.
   *
   * @return the date the resource was last loaded.
   */
  def getLastUploadDate(): Date

  /**
   * Set when the resource was last uploaded into the master.
   *
   * @param lastUploadDate
   *          the last upload date, can be {@code null}
   */
  def setLastUploadDate(lastUploadDate: Date): Unit
  
  /**
   * Get the hash value of the resource bundle.
   *
   * @return the date the resource was last loaded.
   */
  def getBundleContentHash(): String

  /**
   * Set the hash value of the resource bundle.
   *
   * @param bundleContentHash
   *          the hash value for the resource bundle.
   */
  def setBundleContentHash(bundleContentHash: String): Unit
}