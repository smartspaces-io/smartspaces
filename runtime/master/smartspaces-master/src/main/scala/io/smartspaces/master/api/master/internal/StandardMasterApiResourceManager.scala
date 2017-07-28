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

import io.smartspaces.domain.basic.Resource
import io.smartspaces.master.api.master.MasterApiResourceManager
import io.smartspaces.master.api.master.MasterApiUtilities
import io.smartspaces.master.api.messages.MasterApiMessages
import io.smartspaces.master.server.services.ResourceRepository
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.resource.repository.ResourceRepositoryManager

import java.io.InputStream
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map

import scala.collection.JavaConverters._

/**
 * Standard Master API manager for resource operations.
 *
 * @author Keith M. Hughes
 */
class StandardMasterApiResourceManager extends BaseMasterApiManager with MasterApiResourceManager with IdempotentManagedResource {

  /**
   * The repository for resources.
   */
  private var resourceRepository: ResourceRepository = null

  /**
   * The repository manager for resources.
   */
  private var resourceRepositoryManager: ResourceRepositoryManager = null

  override def getResourcesByFilter(filter: String): Map[String, Object] = {
    val responseData: List[Map[String, Object]] = new ArrayList()

    try {
      val filterExpression = expressionFactory.getFilterExpression(filter)

      val resources = resourceRepository.getResources(filterExpression)
      Collections.sort(resources, MasterApiUtilities.RESOURCE_BY_NAME_AND_VERSION_COMPARATOR)
      resources.asScala.foreach { resource =>
        responseData.add(extractBasicResourceApiData(resource))
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch {
      case e: Throwable =>
        val response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
            e)

        logResponseError("Attempt to get resource data failed", response)

        return response
    }
  }

  override def saveResource(resource: Resource, resourceContentStream: InputStream): Map[String, Object] = {
    try {
      val resource = resourceRepositoryManager.addBundleResource(resourceContentStream)
      
      spaceEnvironment.getLog.info(s"Successfully saved resource ${resource}")

      return SmartSpacesMessagesSupport.getSuccessResponse()
    } catch {
      case e: Throwable =>
        val response =
          SmartSpacesMessagesSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE,
            e)

        logResponseError("Attempt to import resource failed", response)

        return response
    }
  }

  /**
   * Get basic information about a resource.
   *
   * @param resource
   *          the resource
   *
   * @return a Master API coded object giving the basic information
   */
  private def extractBasicResourceApiData(resource: Resource): Map[String, Object] = {
    val data: Map[String, Object] = new HashMap()

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, resource.getId())
    data.put("identifyingName", resource.getIdentifyingName())
    data.put("version", resource.getVersion())
    data.put("lastUploadDate", resource.getLastUploadDate())
    data.put("bundleContentHash", resource.getBundleContentHash())

    return data
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

  /**
   * Set the resource repository.
   *
   * @param resourceRepository
   *           the resource repository
   */
  def setResourceRepository(resourceRepository: ResourceRepository): Unit = {
    this.resourceRepository = resourceRepository
  }
}