/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.activity;

/**
 * Configure a resource for an activity.
 *
 * @param <T>
 *          the type of the resource being configured
 *
 * @author Keith M. Hughes
 */
public interface ResourceConfigurator<T> {

  /**
   * Configure a resource.
   *
   * @param resourceName
   *          the name of the resource, should be appropriate as a section of a
   *          configuration parameter name
   * @param configuration
   *          the configuration
   * @param resource
   *          the resource being configured
   */
  void configure(String resourceName, Activity activity, T resource);
}
