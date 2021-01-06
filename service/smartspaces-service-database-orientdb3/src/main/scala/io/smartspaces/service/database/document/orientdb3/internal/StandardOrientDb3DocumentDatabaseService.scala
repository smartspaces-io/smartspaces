/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.service.database.document.orientdb3.internal

import java.io.File

import com.orientechnologies.orient.core.Orient
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.BaseManagedResource
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.database.document.orientdb3.OrientDb3DocumentDatabaseEndpoint
import io.smartspaces.service.database.document.orientdb3.OrientDb3DocumentDatabaseService
import io.smartspaces.service.BaseSupportedService

object StandardOrientDb3DocumentDatabaseService {

  /**
   * Username of the database user.
   */
  val DEFAULT_LOGIN = "root"

  /**
   * Password of the database user.
   */
  val DEFAULT_PASSWORD = "admin"

  /**
   * Protocol string to use for local databases.
   */
  val DATABASE_URL_PROTOCOL_PLOCAL = "plocal:"

}

/**
 * The standard implementation of the orientDB Database Service.
 *
 * @author Oleksandr Kelepko
 * @author Keith M. Hughes
 */
class StandardOrientDb3DocumentDatabaseService extends BaseSupportedService
  with OrientDb3DocumentDatabaseService with IdempotentManagedResource {

  var _maxPoolSize: Int = 32
  
  override def getName(): String = {
    return OrientDb3DocumentDatabaseService.SERVICE_NAME
  }

  override def onStartup(): Unit = {
    // Make sure when SmartSpaces shuts down that orientDB is shut down.
    // Other code may be using OrientDB so we want this at container shutdown.
    getSpaceEnvironment().getContainerManagedScope().managedResources.addResource(new BaseManagedResource() {
      override def shutdown(): Unit = {
        new Thread() {
          override def run(): Unit = {
            Orient.instance().shutdown()
          }
        }.start()
      }
    })
  }
  
  override def maxPoolSize: Int = _maxPoolSize
  
  def maxPoolSize_=(newSize: Int): Unit = {
    _maxPoolSize = newSize
  }

  override def getOrientDbDocumentDatabaseEndpoint(databaseName: String, log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint = {
    val databaseUrl = StandardOrientDb3DocumentDatabaseService.DATABASE_URL_PROTOCOL_PLOCAL +
      getSpaceEnvironment().getFilesystem().getDataDirectory(getName()).getAbsolutePath() +
      File.separator + databaseName

    getOrientDbDocumentDatabaseEndpoint(
      databaseUrl,
      StandardOrientDb3DocumentDatabaseService.DEFAULT_LOGIN, StandardOrientDb3DocumentDatabaseService.DEFAULT_PASSWORD, log)
  }

  override def getOrientDbDocumentDatabaseEndpoint(
    dbDirectory: File,
    log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint = {
    val databaseUrl = StandardOrientDb3DocumentDatabaseService.DATABASE_URL_PROTOCOL_PLOCAL + dbDirectory.getAbsolutePath()

    getOrientDbDocumentDatabaseEndpoint(
      databaseUrl,
      StandardOrientDb3DocumentDatabaseService.DEFAULT_LOGIN, StandardOrientDb3DocumentDatabaseService.DEFAULT_PASSWORD, log)
  }

  override def getOrientDbDocumentDatabaseEndpoint(
    databaseUrl: String,
    login: String, password: String, log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint = {

    new StandardOrientDb3DocumentDatabaseEndpoint(this, databaseUrl, login, password, log)
  }
}
