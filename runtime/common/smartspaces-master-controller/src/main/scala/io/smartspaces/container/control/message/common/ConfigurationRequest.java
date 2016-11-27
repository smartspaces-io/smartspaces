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

import java.util.ArrayList;
import java.util.List;

/**
 * A configuration request.
 * 
 * @author Keith M. Hughes
 */
public class ConfigurationRequest {

  /**
   * The configuration parameters in the request.
   */
  private List<ConfigurationParameterRequest> parameters = new ArrayList<>();

  /**
   * Get the parameters.
   * 
   * @return the parameters
   */
  public List<ConfigurationParameterRequest> getParameters() {
    return parameters;
  }

  /**
   * Set the parameters.
   * 
   * @param parameters
   *          the parameters
   */
  public void setParameters(List<ConfigurationParameterRequest> parameters) {
    this.parameters = parameters;
  }

  /**
   * Add a parameter.
   * 
   * @param parameter
   *          the parameter to add
   */
  public void addParameter(ConfigurationParameterRequest parameter) {
    parameters.add(parameter);
  }

}
