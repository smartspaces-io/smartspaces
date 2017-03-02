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

package io.smartspaces.master.server.services;

import io.smartspaces.resource.managed.ManagedResource;

/**
 * Manager for supporting master operations.
 *
 * @author Keith M. Hughes
 */
public interface MasterSupportManager extends ManagedResource {

  /**
   * Get a model of the entire master domain.
   *
   * @return the entire domain model
   */
  String getMasterDomainModel();

  /**
   * Import a master domain model.
   *
   * @param model
   *          the model to import
   */
  void importMasterDomainModel(String model);
  
  /**
   * Hard restart the master.
   */
  void hardRestartMaster();
  
  /**
   * Soft restart the master.
   */
  void softRestartMaster();
}
