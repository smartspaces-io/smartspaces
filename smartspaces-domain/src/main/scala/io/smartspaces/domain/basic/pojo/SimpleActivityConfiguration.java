/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.domain.basic.pojo;

import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.pojo.SimpleObject;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A POJO implementation of a {@link ActivityConfiguration}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityConfiguration extends SimpleObject implements ActivityConfiguration {

  /**
   * The name of the configuration.
   */
  private String name;

  /**
   * The description of the configuration.
   */
  private String description;

  /**
   * The parameters in the configuration.
   */
  private Set<ConfigurationParameter> parameters = new HashSet<>();

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void addParameter(ConfigurationParameter parameter) {
    parameters.add(parameter);
  }

  @Override
  public void removeParameter(ConfigurationParameter parameter) {
    parameters.remove(parameter);
  }

  @Override
  public Set<ConfigurationParameter> getParameters() {
    return Sets.newHashSet(parameters);
  }

  @Override
  public Map<String, ConfigurationParameter> getParameterMap() {
    Map<String, ConfigurationParameter> map = new HashMap<>();

    for (ConfigurationParameter parameter : parameters) {
      map.put(parameter.getName(), parameter);
    }
    return map;
  }
}
