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

import io.smartspaces.system.SmartSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * A standard implementation of an execution context.
 * 
 * @author Keith M. Hughes
 */
public class StandardExecutionContext implements ExecutionContext {

  /**
   * The space environment for the context.
   */
  private final SmartSpacesEnvironment spaceEnvironment;

  /**
   * The logger for this context.
   */
  private final Log log;

  /**
   * The map of values indexed by their names.
   */
  private final Map<String, Object> values = new HashMap<>();

  /**
   * Construct a new context.
   * 
   * @param spaceEnvironment
   *          the space environment
   * @param log
   *          the logger to use
   */
  public StandardExecutionContext(SmartSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public SmartSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  @Override
  public Log getLog() {
    return log;
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> T getValue(String name) {
    return (T) values.get(name);
  }

  @Override
  public synchronized void setValue(String name, Object value) {
    values.put(name, value);
  }

  @Override
  public synchronized void setValues(Map<String, Object> values) {
    this.values.putAll(values);
  }
}
