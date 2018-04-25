/*
 * Copyright (C) 2018 Keith M. Hughes
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

package io.smartspaces.service.web.server


trait HttpSession {

  /**
   * The session token that will come from the
   */
  def sessionToken: String

  /**
   * The time the session was created.
   */
  def sessionCreationTime: Long

  /**
   * The time the session was last accessed.
   */
  var sessionLastUsedTime: Long
  
  /**
   * Get a value from the session.
   * 
   * @param valueName
   *        the name of the value
   *        
   * @return the value, if found
   */
  def getValue[T](valueName: String): Option[T] 
  
  /**
   * Set a value in the session.
   * 
   * @param valueName
   *        the name of the value
   * @param valueValue
   *        the value of the value
   *        
   * @return this session
   */
  def setValue(valueName: String, valueValue: Any): HttpSession
  
  
  /**
   * [[true]] if the session is authenticated.
   */
  var authenticated: Boolean

  /**
   * Invalidate the session
   */
  def invalidate(): Unit
  
}

class SimpleHttpSession(
  override val sessionToken: String,
  override val sessionCreationTime: Long) extends HttpSession {
  
  private var values = Map[String, Any]()
  
  @volatile override var sessionLastUsedTime: Long = sessionCreationTime
  
  @volatile override var authenticated: Boolean = false

  override def getValue[T](valueName: String): Option[T] = {
    values.get(valueName).asInstanceOf[Option[T]]
  }
  
  override def setValue(valueName: String, valueValue: Any): HttpSession = {
    values = values + (valueName -> valueValue)
    
    this
  }
  
  override def invalidate(): Unit = {
    authenticated = false
    values = Map()
  }
}

trait HttpSessionManager {

  /**
   * Create a new session.
   */
  def newSession(): HttpSession

  /**
   * Get the session associated with a specific token,
   * 
   * @param sessionToken
   *        the session token
   */
  def getSession(sessionToken: String): Option[HttpSession]

  /**
   * Delete a session from the manager.
   * 
   * @param sessionToken
   *        the session token
   */
  def deleteSession(sessionToken: String): Unit
}
