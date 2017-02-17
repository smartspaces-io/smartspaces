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

package io.smartspaces.master.api.master.internal

import io.smartspaces.master.api.master.MasterApiResourceManager
import io.smartspaces.master.api.messages.MasterApiMessageSupport
import io.smartspaces.master.api.messages.MasterApiMessages
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.resource.repository.ResourceRepositoryManager

import java.io.InputStream
import java.util.Map

/**
 * Standard Master API manager for resource operations.
 *
 * @author Keith M. Hughes
 */
class StandardMasterApiResourceManager extends BaseMasterApiManager with MasterApiResourceManager with IdempotentManagedResource {
  
  /**
   * The repository manager for resources.
   */
  private var resourceRepositoryManager: ResourceRepositoryManager = null

  override def saveResource(fileName: String, resourceContentStream: InputStream): Map[String, Object] = {
    try {
      resourceRepositoryManager.addBundleResource(resourceContentStream)

      return MasterApiMessageSupport.getSuccessResponse()
    } catch {
      case e: Throwable => 
        val response =
          MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
            e)

        logResponseError("Attempt to import resource failed", response)

        return response
    }
  }
  
  /**
   * Set the resource repository manager.
   * 
   * @param resourceRepositoryManager
   *           the resource repository manager
   */
  def setResourceRepositoryManager(resourceRepositoryManager: ResourceRepositoryManager): Unit = {
    this.resourceRepositoryManager = resourceRepositoryManager
  }
}