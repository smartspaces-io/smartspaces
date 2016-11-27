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

package io.smartspaces.container.control.message.common;

/**
 * A configuration parameter in a configuration request.
 * 
 * @author Keith M. Hughes
 */
public class ConfigurationParameterRequest {

  /**
   * The name of the configuration parameter.
   */
  private String name;

  /**
   * The stringified value of the configuration parameter.
   */
  private String value;

  /**
   * The operation for this parameter.
   */
  private ConfigurationParameterRequestOperation operation;

  /**
   * Get the name of the configuration parameter.
   * 
   * @return the name of the configuration parameter
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the configuration parameter.
   * 
   * @param name
   *          the name of the configuration parameter
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the value for the configuration parameter.
   * 
   * @return the value for the configuration parameter
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the value for the configuration parameter.
   * 
   * @return the value for the configuration parameter
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Get the operation for this parameter.
   * 
   * @return the operation for this parameter
   */
  public ConfigurationParameterRequestOperation getOperation() {
    return operation;
  }

  /**
   * Set the operation for this parameter.
   * 
   * @param operation
   *          the operation for this parameter
   */
  public void setOperation(ConfigurationParameterRequestOperation operation) {
    this.operation = operation;
  }

  /**
   * the operations for a configuration parameter.
   * 
   * @author Keith M. Hughes
   */
  public enum ConfigurationParameterRequestOperation {
    
    /**
     * Add in the parameter.
     */
    ADD, 
    
    /**
     * Delete the parameter.
     */
    DELETE, 
    
    /**
     * Modify the parameter.
     */
    MODIFY
  }
}
