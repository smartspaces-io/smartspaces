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

package io.smartspaces.service.database.document.orientdb3

import java.io.File

import com.orientechnologies.orient.core.db.ODatabaseSession
import io.smartspaces.resource.managed.ManagedResource

/**
 * Endpoint for an OrientDB document database.
 *
 * @author Oleksandr Kelepko
 * @author Keith M. Hughes
 */
trait OrientDb3DocumentDatabaseEndpoint extends ManagedResource {
  
  /**
   * The creator for the database.
   * 
   * This is called on startup.
   */
  var creator: Option[OrientDb3DatabaseCreator]
  
  /**
   * The initializer for the database.
   * 
   * This is called on startup.
   */
  var initializer: Option[OrientDb3EndpointInitializer]

  /**
   * Creates a new connection to the database managed by this endpoint.
   * 
   * You must close this connection when through with it.
   *
   * @return OrientDB database connection
   */
  def createConnection(): ODatabaseSession
  
  /**
   * Perform a void operation.
   * 
   * Do not save the database connection. It is closed upon completion of the operation.
   * The operation is rolled back if an exception is thrown.
   *
   * @param operation
   *          the code to run
   */
  def doVoidOperation(operation: (ODatabaseSession) => Unit): Unit

  /**
   * Perform an operation.
   * 
   * Do not save the database connection. It is closed upon completion of the operation.
   * The operation is rolled back if an exception is thrown.
   *
   * @param operation
   *          the code to run
   *
   * @returns the result of the operation
   */
  def doOperation[T](operation: (ODatabaseSession) => T): T
  
  /**
   * Backup the database.
   * 
   * @param outputLocation
   *        the file where th backup should be stored
   */
  def backup(outputLocation: File): Unit
}
