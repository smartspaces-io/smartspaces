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
import java.io.FileOutputStream
import java.util.concurrent.Callable

import com.orientechnologies.orient.core.command.OCommandOutputListener
import com.orientechnologies.orient.core.db.ODatabasePool
import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.util.OURLHelper
import io.smartspaces.SmartSpacesException
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.IdempotentManagedResource
import io.smartspaces.service.database.document.orientdb3.OrientDb3DatabaseCreator
import io.smartspaces.service.database.document.orientdb3.OrientDb3DocumentDatabaseEndpoint
import io.smartspaces.service.database.document.orientdb3.OrientDb3EndpointInitializer

object StandardOrientDb3DocumentDatabaseEndpoint {

  /**
    * The role the admin will have for the OrientDB database.
    */
  val ORIENTDB_ADMIN_ROLE = "admin"
}

/**
  * Endpoint for an OrientDB document database.
  *
  * @author Oleksandr Kelepko
  * @author Keith M. Hughes
  */
class StandardOrientDb3DocumentDatabaseEndpoint(
  service: StandardOrientDb3DocumentDatabaseService,
  databaseUrl: String,
  username: String,
  password: String,
  log: ExtendedLog) extends OrientDb3DocumentDatabaseEndpoint with IdempotentManagedResource {

  /**
    * The creator for the OrientDB database if the database didn't exist and is now being created.
    */
  override var creator: Option[OrientDb3DatabaseCreator] = None

  /**
    * The initializer for the database session.
    */
  override var initializer: Option[OrientDb3EndpointInitializer] = None

  /**
    * The pool of database connections.
    */
  private var pool: ODatabasePool = null

  override def onStartup(): Unit = {
    checkDataBaseExists()

    pool = new ODatabasePool(databaseUrl, username, password)

    initializer.foreach((i) => {
      doVoidOperation(i.onDatabaseInit)
    })
  }

  override def onShutdown(): Unit = {
    pool.close()
    pool = null
  }

  override def createConnection(): ODatabaseSession = {
    pool.acquire()
  }

  override def doVoidOperation(operation: (ODatabaseSession) => Unit): Unit = {
    val connection = createConnection
    try {
      operation(connection)
    } catch {
      case e: Throwable => {
        connection.rollback

        throw e
      }
    } finally {
      connection.close
    }
  }

  override def doOperation[T](operation: (ODatabaseSession) => T): T = {
    val connection = createConnection
    try {
      operation(connection)
    } catch {
      case e: Throwable => {
        connection.rollback

        throw e
      }
    } finally {
      connection.close
    }
  }

  override def backup(outputLocation: File): Unit = {
    var out: FileOutputStream = null
    try {
      val connection = createConnection
      try {
        connection.backup(out, null,
          new Callable[Object]() {
            override def call(): Object = {
              null
            }
          },
          new OCommandOutputListener() {
            override def onMessage(text: String): Unit = {
              log.info(s"OrientDB Database backup for database ${databaseUrl} update: ${text}")
            }
          },
          1, 4096)
      } finally {
        connection.close
      }
    } catch {
      case e: Throwable =>
        log.error(s"OrientDB Database backup for database ${databaseUrl} to ${outputLocation} failed", e)
    } finally {
      if (out != null) {
        out.flush
        out.close
      }
    }
  }

  /**
    * Check to see if the database exists. If it doesn't, create it.
    */
  private def checkDataBaseExists(): Unit = {
    log.info(s"Creating OrientDB database connection to ${databaseUrl}")

    val connection = OURLHelper.parseNew(databaseUrl)
    val dbenv = new OrientDB(s"${connection.getType}:${connection.getPath}", username, password, OrientDBConfig.defaultConfig())

    try {
      // Can only test existence of databases if they are not remote
      val exists = dbenv.exists(connection.getDbName)
      if (!exists) {
        if (!connection.getDbType.isPresent) {
          throw new SmartSpacesException(s"Sould not create database ${databaseUrl}. Not creatable with orientDB API")
        }

        dbenv.create(connection.getDbName, connection.getDbType.get())
      }

      val session = dbenv.open(connection.getDbName, username, password)
      try {
        creator.foreach((c) => {
          val schema = session.getMetadata().getSchema()

          c.onDatabaseCreate(session, schema)
        })
      } finally {
        session.close
      }
    } finally {
      dbenv.close
    }
  }
}
