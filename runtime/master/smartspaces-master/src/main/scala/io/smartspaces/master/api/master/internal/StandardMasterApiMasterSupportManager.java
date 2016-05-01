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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.api.messages.MasterApiMessageSupport;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.server.services.MasterSupportManager;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * The standard implementation of the {@link MasterApiMasterSupportManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterApiMasterSupportManager extends BaseMasterApiManager implements
    MasterApiMasterSupportManager {

  /**
   * The master support manager.
   */
  private MasterSupportManager masterSupportManager;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public Map<String, Object> exportMasterDomainModel() {
    try {
      String model = masterSupportManager.getMasterDomainModel();

      Map<String, Object> data = new HashMap<>();
      data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_MODEL, model);

      return MasterApiMessageSupport.getSuccessResponse(data);
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while exporting master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> importMasterDomainModel(String model) {
    if (model == null || model.trim().isEmpty()) {
      return MasterApiMessageSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_MISSING,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_FAILURE_MISSING_MODEL);
    }

    try {
      masterSupportManager.importMasterDomainModel(model);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while importing master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> exportToFileSystemMasterDomainModel() {
    try {
      String model = masterSupportManager.getMasterDomainModel();

      fileSupport.writeFile(fileSupport.newFile(MASTER_DOMAIN_FILE), model);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while exporting master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> importFromFileSystemMasterDomainModel() {
    try {
      String model = fileSupport.readFile(fileSupport.newFile(MASTER_DOMAIN_FILE));

      masterSupportManager.importMasterDomainModel(model);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while importing master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> getSmartSpacesVersion() {
    Map<String, Object> data = new HashMap<>();
    data.put(
        MasterApiMessages.MASTER_API_PARAMETER_NAME_SMART_SPACES_VERSION,
        spaceEnvironment.getSystemConfiguration().getPropertyString(
            SmartSpacesEnvironment.CONFIGURATION_SMARTSPACES_VERSION,
            MasterApiMessages.MASTER_API_PARAMETER_VALUE_SMART_SPACES_VERSION_UNKNOWN));

    return MasterApiMessageSupport.getSuccessResponse(data);
  }

  /**
   * set the master support manager.
   *
   * @param masterSupportManager
   *          the master support manager
   */
  public void setMasterSupportManager(MasterSupportManager masterSupportManager) {
    this.masterSupportManager = masterSupportManager;
  }
}
