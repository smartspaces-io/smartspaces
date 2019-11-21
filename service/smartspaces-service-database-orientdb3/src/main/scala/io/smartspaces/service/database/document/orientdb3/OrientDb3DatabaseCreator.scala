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

package io.smartspaces.service.database.document.orientdb3

import com.orientechnologies.orient.core.db.ODatabaseSession
import com.orientechnologies.orient.core.metadata.schema.OSchema

/**
 * A creator for an OrientDB database.
 *
 * @author Keith M. Hughes
 */
trait OrientDb3DatabaseCreator {

  /**
   * The database has just been created from scratch.
   *
   * These is no need to call close on the database. This is done automatically.
   *
   * @param db
   *        the database
   * @param schema
   *        the schema object for creating schemas
   */
  def onDatabaseCreate(db: ODatabaseSession, schema: OSchema): Unit
}