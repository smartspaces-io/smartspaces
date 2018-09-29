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

package io.smartspaces.evaluation;

import io.smartspaces.scope.ManagedScope;
import io.smartspaces.system.SmartSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * A context for executing actions or things.
 * 
 * @author Keith M. Hughes
 */
public interface ExecutionContext {

  /**
   * Get the space environment for the context.
   * 
   * @return the space environment
   */
  SmartSpacesEnvironment getSpaceEnvironment();
  
  /**
   * Get the managed scope for the context.
   * 
   * @return the managed scope
   */
  ManagedScope getManagedScope();

  /**
   * Get the logger for the context.
   * 
   * @return the logger
   */
  Log getLog();

  /**
   * Get a value from the current level of the context.
   * 
   * @param name
   *          the name of the value
   * 
   * @return the value associated with the name, or {@code null} if not there
   */
  <T> T getValueLocally(String name);

  /**
   * Get a value from the context.
   * 
   * @param name
   *          the name of the value
   * 
   * @return the value associated with the name, or {@code null} if not there
   */
  <T> T getValue(String name);

  /**
   * Set a value in the context at the current lavel.
   * 
   * @param name
   *          the name of the value
   * @param value
   *          the value associated with the name
   */
  void setValue(String name, Object value);

  /**
   * Set all values into the context at the current level..
   * 
   * <p>
   * The name will be the key in the supplied map and the value will be the
   * associated value.
   * 
   * @param values
   *          the values to add
   */
  void setValues(Map<String, Object> values);
  
  /**
   * Get the parent of this context, if any.
   * 
   * @return the parent, or {@code null} when none
   */
  ExecutionContext getParent();
  
  /**
   * Create a new execution context that has this context as its parent.
   * 
   * @return the new context
   */
  ExecutionContext push();
}
