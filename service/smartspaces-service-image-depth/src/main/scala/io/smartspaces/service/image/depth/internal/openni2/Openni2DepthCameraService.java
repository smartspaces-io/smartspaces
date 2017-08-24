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

package io.smartspaces.service.image.depth.internal.openni2;

import java.util.ArrayList;
import java.util.List;

import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.image.depth.DepthCameraService;
import io.smartspaces.service.image.depth.UserTrackerDepthCameraEndpoint;
import io.smartspaces.service.image.depth.internal.openni2.libraries.NiTE2Library;
import io.smartspaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NiteStatus;
import io.smartspaces.service.image.depth.internal.openni2.libraries.OniDeviceInfo;
import io.smartspaces.service.image.depth.internal.openni2.libraries.OpenNI2Library;
import io.smartspaces.service.image.depth.internal.openni2.libraries.OpenNI2Library.OniStatus;

/**
 * A depth camera service using OpenNI2.
 *
 * @author Keith M. Hughes
 */
public class Openni2DepthCameraService extends BaseSupportedService implements DepthCameraService {

  @Override
  public String getName() {
    return DepthCameraService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    IntValuedEnum<OniStatus> openniStatus =
        OpenNI2Library.oniInitialize(OpenNI2Library.ONI_API_VERSION);
    if (openniStatus != OniStatus.ONI_STATUS_OK) {
      OpenNi2Support.throwExtendedOpenNIError("OpenNI2 would not initialize", openniStatus);
    }

    IntValuedEnum<NiteStatus> niteStatus = NiTE2Library.niteInitialize();
    if (niteStatus != NiteStatus.NITE_STATUS_OK) {
      OpenNi2Support.throwExtendedNiteError("NiTE would not initialize", niteStatus);
    }
  }

  @Override
  public List<String> getDepthCamerasAvailable() {
    Pointer<Integer> numDevices = Pointer.allocateInt();
    Pointer<Pointer<OniDeviceInfo>> devices = Pointer.allocatePointer(OniDeviceInfo.class);

    try {
      List<String> cameras = new ArrayList<>();

      OpenNI2Library.oniGetDeviceList(devices, numDevices);

      for (int i = 0; i < numDevices.getInt(); i++) {
        OniDeviceInfo info = devices.get(i).get();
        cameras.add(info.uri().getCString());
      }
      return cameras;
    } finally {
      devices.release();
      numDevices.release();
    }
  }

  @Override
  public UserTrackerDepthCameraEndpoint newUserTrackerDepthCameraEndpoint(ExtendedLog log) {
    List<String> cameraIds = getDepthCamerasAvailable();
    if (cameraIds.isEmpty()) {
      throw new SimpleSmartSpacesException("No depth cameras available");
    }

    return newUserTrackerDepthCameraEndpoint(cameraIds.get(0), log);
  }

  @Override
  public UserTrackerDepthCameraEndpoint newUserTrackerDepthCameraEndpoint(String cameraId, ExtendedLog log) {
    return new Openni2UserTrackerDepthCameraEndpoint(cameraId, getSpaceEnvironment()
        .getExecutorService(), log);
  }
}
