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

package io.smartspaces.resource.repository;

/**
 * The categories for resources.
 * 
 * @author Keith M. Hughes
 */
public enum ResourceCategory {

  /**
   * Resource category for bundle resources.
   */
  RESOURCE_CATEGORY_CONTAINER_BUNDLE("bundle"),

  /**
   * Resource category for activities.
   */
  RESOURCE_CATEGORY_ACTIVITY("activity"),

  /**
   * Resource category for data.
   */
  RESOURCE_CATEGORY_DATA("data");

  /**
   * The string component name for the category.
   */
  private String component;

  /**
   * Construct a new category.
   * 
   * @param component
   *          the component name for the category
   */
  ResourceCategory(String component) {
    this.component = component;
  }

  /**
   * Get the string component name for the category.
   * 
   * @return the string component name
   */
  public String getComponent() {
    return component;
  }

  /**
   * Get the resource category from a component name.
   * 
   * @param component
   *          the component name
   * 
   * @return the category, or {@code null} if no matches
   */
  public static ResourceCategory getCategoryFromComponent(String component) {
    if (RESOURCE_CATEGORY_CONTAINER_BUNDLE.getComponent().equals(component)) {
      return RESOURCE_CATEGORY_CONTAINER_BUNDLE;
    } else if (RESOURCE_CATEGORY_ACTIVITY.getComponent().equals(component)) {
      return RESOURCE_CATEGORY_ACTIVITY;
    } else if (RESOURCE_CATEGORY_DATA.getComponent().equals(component)) {
      return RESOURCE_CATEGORY_DATA;
    } else {
      return null;
    }
  }
}