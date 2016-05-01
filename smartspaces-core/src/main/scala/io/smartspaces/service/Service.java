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

package io.smartspaces.service;

import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.Map;

/**
 * A Smart Spaces service.
 *
 * @author Keith M. Hughes
 */
public interface Service {

  /**
   * Get the name of the service.
   *
   * @return the name of the service
   */
  String getName();

  /**
   * Get the metadata for the service.
   *
   * @return the metadata
   */
  Map<String, Object> getMetadata();

  /**
   * Get the service description for this service.
   *
   * @return the service description for this service
   */
  ServiceDescription getServiceDescription();

  /**
   * Set the space environment for the service.
   *
   * @param spaceEnvironment
   *          the environment to use
   */
  void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment);
}
