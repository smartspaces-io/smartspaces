/*
 * Copyright (C) 2015 Keith M. Hughes
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
package io.smartspaces.service.action;

import io.smartspaces.resource.VersionRange;

import java.util.Collections;
import java.util.Map;

/**
 * A basic implementation for action references.
 * 
 * @author Keith M. Hughes
 */
@SuppressWarnings("unchecked")
public class BasicActionReference implements ActionReference {

  /**
   * The no data map.
   */
  public static final Map<String, Object> NO_DATA;

  static {
    NO_DATA = (Map<String, Object>) Collections.unmodifiableMap(Collections.EMPTY_MAP);
  }

  /**
   * Name of the reference.
   */
  private String name;

  /**
   * Description of the reference, can be {@code null}.
   */
  private String description;

  /**
   * The name of the action source.
   */
  private String actionSource;

  /**
   * The name of the action.
   */
  private String actionName;

  /**
   * The version range for the action source.
   */
  private VersionRange actionSourceVersionRange;

  /**
   * The data for the reference.
   */
  private Map<String, Object> data;

  /**
   * Construct a new base action.
   * 
   * @param actionName
   *          the action name
   * @param data
   *          the data
   */
  public BasicActionReference(String name, String description, String actionSource,
      VersionRange actionSourceVersionRange, String actionName,
      Map<String, Object> data) {
    this.name = name;
    this.description = description;
    this.data = (data != null) ? data : NO_DATA;

    this.actionSource = actionSource;
    this.actionName = actionName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ActionReference setName(String name) {
    this.name = name;

    return this;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public ActionReference setDescription(String description) {
    this.description = description;

    return this;
  }

  @Override
  public String getActionSource() {
    return actionSource;
  }

  @Override
  public VersionRange getActionSourceVersionRange() {
    return actionSourceVersionRange;
  }

  @Override
  public String getActionName() {
    return actionName;
  }

  @Override
  public Map<String, Object> getData() {
    return data;
  }
}
