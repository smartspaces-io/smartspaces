/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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
 * the LicControllerDense.
 */

package io.smartspaces.master.server.services.internal.comm;

import io.smartspaces.container.control.message.StandardMasterSpaceControllerCodec;
import io.smartspaces.container.control.message.container.resource.deployment.ControllerDataRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ControllerDataRequest.ControllerDataRequestTransferType;
import io.smartspaces.master.server.services.internal.StandardMasterDataBundleManager;
import io.smartspaces.master.server.services.model.ActiveSpaceController;

/**
 * TCP-based implementation of a master data bundle manager.
 * 
 * @author Keith M. Hughes
 */
public class TcpMasterDataBundleManager extends StandardMasterDataBundleManager {

  /**
   * Remote space controller client for communication.
   */
  private SimpleTcpRemoteSpaceControllerClient remoteSpaceControllerClient;

  @Override
  protected void sendControllerDataBundleCaptureRequest(ActiveSpaceController controller,
      String destinationUri) {
    ControllerDataRequest request = new ControllerDataRequest(
        ControllerDataRequestTransferType.CONTROLLER_DATA_PERMANENT, destinationUri);

    remoteSpaceControllerClient.sendSpaceControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_CAPTURE_DATA, request);
  }

  @Override
  protected void sendControllerDataBundleRestoreRequest(ActiveSpaceController controller,
      String sourceUri) {
    ControllerDataRequest request = new ControllerDataRequest(
        ControllerDataRequestTransferType.CONTROLLER_DATA_PERMANENT, sourceUri);

    remoteSpaceControllerClient.sendSpaceControllerRequest(controller,
        StandardMasterSpaceControllerCodec.OPERATION_CONTROLLER_RESTORE_DATA, request);
  }

  /**
   * @param remoteControllerClient
   *          the remote controller client to set
   */
  public void
      setRemoteSpaceControllerClient(SimpleTcpRemoteSpaceControllerClient remoteControllerClient) {
    this.remoteSpaceControllerClient = remoteControllerClient;
  }
}
