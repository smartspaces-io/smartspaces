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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.system.SmartSpacesEnvironment;

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
   * The managed scope for the context.
   */
  private final ManagedScope managedScope;

  /**
   * The logger for this context.
   */
  private final ExtendedLog log;

  /**
   * The map of values indexed by their names.
   */
  private final Map<String, Object> values = new HashMap<>();

  /**
   * The potential parent of this context.
   */
  private StandardExecutionContext parent = null;

  /**
   * Construct a new context.
   * 
   * @param managedScope
   *          the managed scope for this context
   * @param spaceEnvironment
   *          the space environment
   * @param log
   *          the logger to use
   */
  public StandardExecutionContext(ManagedScope managedScope,
      SmartSpacesEnvironment spaceEnvironment, ExtendedLog log) {
    this.managedScope = managedScope;
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public ExecutionContext getParent() {
    return parent;
  }

  @Override
  public ManagedScope getManagedScope() {
    return managedScope;
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
  public synchronized <T> T getValueLocally(String name) {
    return (T) values.get(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> T getValue(String name) {
    StandardExecutionContext current = this;
    do {
      T value = current.getValueLocally(name);
      if (value != null) {
        return value;
      }

      current = current.parent;
    } while (current != null);

    return (T) null;
  }

  @Override
  public synchronized void setValue(String name, Object value) {
    values.put(name, value);
  }

  @Override
  public synchronized void setValues(Map<String, Object> values) {
    this.values.putAll(values);
  }

  @Override
  public synchronized ExecutionContext push() {
    StandardExecutionContext newContext =
        new StandardExecutionContext(managedScope, spaceEnvironment, log);
    newContext.parent = this;

    return newContext;
  }
}
