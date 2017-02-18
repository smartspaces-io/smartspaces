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

package io.smartspaces.domain.basic.pojo

import io.smartspaces.domain.basic.Resource
import io.smartspaces.domain.pojo.SimpleObject

import java.util.Date

/**
 * A POJO resource, such as a dependency for an activity.
 * 
 * @author Keith M. Hughes
 */
class SimpleResource extends SimpleObject with Resource {
  
  /**
   * The identifying name for the resource.
   */
  private var identifyingName:String = null
  
  /**
   * The version of the resource.
   */
  private var version: String = null
  
  /**
   * The last upload date of the resource.
   */
  private var lastUploadDate: Date = null
  
  /**
   * Set the hash value of the resource bundle.
   */
  private var bundleContentHash: String = null
  
  override def getIdentifyingName(): String = {
    identifyingName
  }

  override def setIdentifyingName(identifyingName: String ): Unit = {
    this.identifyingName = identifyingName
  }

  override def getVersion(): String = {
    version
  }

  override def setVersion(version: String): Unit = {
    this.version = version
  }

  override def getLastUploadDate(): Date = {
    lastUploadDate
  }

  override def setLastUploadDate(lastUploadDate: Date): Unit = {
    this.lastUploadDate = lastUploadDate
  }
  
  override def getBundleContentHash(): String = {
    bundleContentHash
  }

  override def setBundleContentHash(bundleContentHash: String): Unit = {
    this.bundleContentHash = bundleContentHash
  }
}