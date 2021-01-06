/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.service.database.document.orientdb3;

import java.io.File

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.service.SupportedService;

object OrientDb3DocumentDatabaseService {

  /**
   * The name of the service.
   */
  val SERVICE_NAME = "database.orientdb3";

}

/**
 * A database service for OrientDB.
 *
 * @author Keith M. Hughes
 */
trait OrientDb3DocumentDatabaseService extends SupportedService {

  /**
   * Get the maximum poolsize for database connections.
   *
   * @return the maximum pool size
   */
  def maxPoolSize: Int

  /**
   * Set the maximum poolsize for database connections.
   *
   * @param newSize
   *        the new maximum pool size
   */
  def maxPoolSize_=(newSize: Int): Unit

  /**
   * Creates new endpoint for a database with the given name.
   *
   * The database will be a plocal database in the smartspaces data directory.
   *
   * @param databaseName
   *            OrientDB database name
   * @param log
   *            logger
   *
   * @return new endpoint for accessing the database
   */
  def getOrientDbDocumentDatabaseEndpoint(databaseName: String, log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint

  /**
   * Creates new endpoint for a database with at the given location.
   *
   * The database will be a plocal database.
   *
   * @param dbDirectory
   *            OrientDB database storage
   * @param log
   *            logger
   *
   * @return new endpoint for accessing the database
   */
  def getOrientDbDocumentDatabaseEndpoint(dbDirectory: File, log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint

  /**
   * Creates new endpoint for a database with the given URL and credentials.
   *
   * @param databaseUrl
   *            OrientDB database URL
   * @param login
   *            login for the database access
   * @param password
   *            password for the database access
   * @param log
   *            logger
   *
   * @return new endpoint for accessing the database
   */
  def getOrientDbDocumentDatabaseEndpoint(databaseUrl: String, login: String,
    password: String, log: ExtendedLog): OrientDb3DocumentDatabaseEndpoint
}