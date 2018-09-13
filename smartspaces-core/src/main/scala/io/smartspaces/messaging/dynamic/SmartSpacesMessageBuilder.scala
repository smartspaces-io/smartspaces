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

package io.smartspaces.messaging.dynamic

import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder
import io.smartspaces.util.data.dynamic.DynamicObject

/**
 * A builder for smartspaces messages.
 * 
 * @author Keith M. Hughes
 */
class SmartSpacesMessageBuilder(messageType: String) {
  
  /**
   * The underlying builder.
   */
  private val builder = new StandardDynamicObjectBuilder
  
  builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, messageType)
  
  /**
   * Get the underlying object.
   */
  def build: DynamicObject = builder.toDynamicObject()
  
  /**
   * Get the underlying builder after moving to the data section.
   * 
   * @return the builder in the data section
   */
  def buildToData: DynamicObjectBuilder = {
    builder.newObject(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA)
    
    builder
  }
  
  /**
   * Add a sender to the message.
   * 
   * @param sender
   *        the sender
   *        
   * @return this builder
   */
  def sender(sender: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_SENDER, sender)
    
    this
  }
  
  /**
   * Add a destination to the message.
   * 
   * @param destination
   *        the destination
   *        
   * @return this builder
   */
  def destination(destination: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_DESTINATION, destination)
    
    this
  }
  
  /**
   * Add a result to the message.
   * 
   * @param result
   *        the result
   *        
   * @return this builder
   */
  def result(result: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT, result)
    
    this
  }
  
  /**
   * Add a reason to the message.
   * 
   * @param reason
   *        the reason
   *        
   * @return this builder
   */
  def reason(reason: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_REASON, reason)
    
    this
  }
  
  /**
   * Add a detail to the message.
   * 
   * @param detail
   *        the detail
   *        
   * @return this builder
   */
  def detail(detail: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_DETAIL, detail)
    
    this
  }
  
  /**
   * Add a request ID to the message.
   * 
   * @param requestId
   *        the request ID
   *        
   * @return this builder
   */
  def requestId(requestId: String): SmartSpacesMessageBuilder = {
    builder.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_REQUEST_ID, requestId)
    
    this
  }
}
